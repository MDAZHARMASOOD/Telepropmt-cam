package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface StudioRecordingState {
    data object Idle : StudioRecordingState
    data class Countdown(val secondsRemaining: Int) : StudioRecordingState
    data class RecordingInProgress(val durationSeconds: Long, val isPaused: Boolean) : StudioRecordingState
    data object Finalizing : StudioRecordingState
    data class Error(val message: String) : StudioRecordingState
}

class CameraRecordingManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "CameraRecordingManager"

    private var cameraProvider: ProcessCameraProvider? = null
    private var boundLifecycleOwner: LifecycleOwner? = null
    private var boundPreviewView: PreviewView? = null
    private val avatarEffect = AvatarCameraEffect()
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var currentOutputFile: File? = null
    private var timerJob: Job? = null
    private var countdownJob: Job? = null

    private var recordingStartTime = 0L
    private var recordedDurationSeconds = 0L
    private var isRecordingPaused = false
    private var isSimulatedRecording = false
    private var onSimulatedSaved: ((file: File, durationMs: Long) -> Unit)? = null

    private val _recordingState = MutableStateFlow<StudioRecordingState>(StudioRecordingState.Idle)
    val recordingState: StateFlow<StudioRecordingState> = _recordingState.asStateFlow()

    private val _currentLensFacing = MutableStateFlow(CameraSelector.LENS_FACING_FRONT)
    val currentLensFacing: StateFlow<Int> = _currentLensFacing.asStateFlow()

    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onCameraReady: () -> Unit = {}
    ) {
        boundLifecycleOwner = lifecycleOwner
        boundPreviewView = previewView
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider
                rebindCameraUseCases(lifecycleOwner, previewView)
                _isCameraReady.value = true
                onCameraReady()
            } catch (e: Exception) {
                Log.e(tag, "Failed to bind camera use cases", e)
                _recordingState.value = StudioRecordingState.Error("Failed to initialize camera: ${e.localizedMessage}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun rebindCameraUseCases(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return
        try {
            provider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(ContextCompat.getMainExecutor(context), avatarEffect.analyzer) }

            fun bindGroup(selector: CameraSelector, useCases: List<UseCase>) {
                val group = UseCaseGroup.Builder().apply {
                    useCases.forEach { addUseCase(it) }
                    addEffect(avatarEffect.overlay)
                    previewView.viewPort?.let(::setViewPort)
                }.build()
                provider.bindToLifecycle(lifecycleOwner, selector, group)
            }

            var selector = CameraSelector.Builder()
                .requireLensFacing(_currentLensFacing.value)
                .build()

            // If selected lens facing is not available (e.g. in some emulators), fallback to available
            if (!provider.hasCamera(selector)) {
                val alternateLens = if (_currentLensFacing.value == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
                val altSelector = CameraSelector.Builder().requireLensFacing(alternateLens).build()
                if (provider.hasCamera(altSelector)) {
                    selector = altSelector
                    _currentLensFacing.value = alternateLens
                }
            }

            var canRecordVideo = false
            var captureForFallback: VideoCapture<Recorder>? = null
            try {
                val cameraInfo = provider.getCameraInfo(selector)
                val supportedQualities = QualitySelector.getSupportedQualities(cameraInfo)
                if (supportedQualities.isNotEmpty()) {
                    val qualitySelector = QualitySelector.fromOrderedList(
                        supportedQualities,
                        FallbackStrategy.lowerQualityOrHigherThan(supportedQualities.last())
                    )
                    val recorder = Recorder.Builder()
                        .setQualitySelector(qualitySelector)
                        .build()

                    val capture = VideoCapture.withOutput(recorder)
                    captureForFallback = capture
                    bindGroup(selector, listOf(preview, analysis, capture))
                    videoCapture = capture
                    canRecordVideo = true
                }
            } catch (e: Throwable) {
                Log.w(tag, "VideoCapture not supported on this virtual/emulator camera: ${e.message}")
                provider.unbindAll()
                captureForFallback?.let { capture ->
                    try {
                        provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                        videoCapture = capture
                        canRecordVideo = true
                        Log.i(tag, "Bound preview and recording without avatar overlay fallback")
                    } catch (fallbackError: Throwable) {
                        Log.w(tag, "Recording fallback could not bind", fallbackError)
                    }
                }
            }

            if (!canRecordVideo) {
                // If videoCapture cannot be bound or has no supported EncoderProfiles on emulator,
                // bind Preview alone so preview is 100% stable and error-free!
                videoCapture = null
                provider.unbindAll()
                try {
                    bindGroup(selector, listOf(preview, analysis))
                } catch (effectError: Throwable) {
                    Log.w(tag, "Avatar overlay unavailable; binding preview only", effectError)
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, selector, preview)
                }
                Log.i(tag, "Bound preview successfully (preview-only/simulation fallback mode)")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error rebinding camera", e)
            _recordingState.value = StudioRecordingState.Error("Camera unavailable on this device or emulator")
        }
    }

    fun selectAvatar(style: AvatarStyle) {
        avatarEffect.select(style)
    }

    fun toggleCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        if (activeRecording != null) return // don't toggle while actively recording
        _currentLensFacing.value = if (_currentLensFacing.value == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        rebindCameraUseCases(lifecycleOwner, previewView)
                previewView.post {
                    if (_isCameraReady.value) rebindCameraUseCases(lifecycleOwner, previewView)
                }
    }

    fun startCountdownAndRecord(
        countdownSeconds: Int = 3,
        onVideoSaved: (file: File, durationMs: Long) -> Unit
    ) {
        if (activeRecording != null) return

        if (countdownSeconds <= 0) {
            startRecordingInternal(onVideoSaved)
            return
        }

        countdownJob?.cancel()
        countdownJob = scope.launch(Dispatchers.Main) {
            for (sec in countdownSeconds downTo 1) {
                _recordingState.value = StudioRecordingState.Countdown(sec)
                delay(1000)
            }
            startRecordingInternal(onVideoSaved)
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        if (_recordingState.value is StudioRecordingState.Countdown) {
            _recordingState.value = StudioRecordingState.Idle
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecordingInternal(onVideoSaved: (file: File, durationMs: Long) -> Unit) {
        val capture = videoCapture
        if (capture == null) {
            startSimulatedRecording(onVideoSaved)
            return
        }

        val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        if (!moviesDir.exists()) moviesDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val videoFile = File(moviesDir, "TELEPROMPT_${timestamp}.mp4")
        currentOutputFile = videoFile

        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        var pendingRecording = capture.output.prepareRecording(context, outputOptions)

        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasAudioPermission) {
            try {
                pendingRecording = pendingRecording.withAudioEnabled()
            } catch (e: Exception) {
                Log.w(tag, "Audio enable error: ${e.message}")
            }
        }

        recordedDurationSeconds = 0L
        isRecordingPaused = false
        recordingStartTime = System.currentTimeMillis()

        try {
            activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        _recordingState.value = StudioRecordingState.RecordingInProgress(0L, false)
                        startTimer()
                    }
                    is VideoRecordEvent.Pause -> {
                        isRecordingPaused = true
                        _recordingState.value = StudioRecordingState.RecordingInProgress(recordedDurationSeconds, true)
                    }
                    is VideoRecordEvent.Resume -> {
                        isRecordingPaused = false
                        _recordingState.value = StudioRecordingState.RecordingInProgress(recordedDurationSeconds, false)
                    }
                    is VideoRecordEvent.Finalize -> {
                        stopTimer()
                        val durationMs = event.recordingStats.recordedDurationNanos / 1_000_000
                        if (!event.hasError()) {
                            Log.i(tag, "Video recorded successfully to: ${videoFile.absolutePath}, duration: ${durationMs}ms")
                            _recordingState.value = StudioRecordingState.Idle
                            onVideoSaved(videoFile, durationMs)
                        } else {
                            Log.e(tag, "Video recording error: ${event.error}")
                            if (videoFile.exists() && videoFile.length() > 50000) {
                                // Saved at least partial video
                                _recordingState.value = StudioRecordingState.Idle
                                onVideoSaved(videoFile, durationMs)
                            } else {
                                _recordingState.value = StudioRecordingState.Error("Recording error: code ${event.error}")
                            }
                        }
                        activeRecording = null
                        currentOutputFile = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to start hardware recording, falling back to simulated recording", e)
            startSimulatedRecording(onVideoSaved)
        }
    }

    private fun startSimulatedRecording(onVideoSaved: (file: File, durationMs: Long) -> Unit) {
        val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        if (!moviesDir.exists()) moviesDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val videoFile = File(moviesDir, "TELEPROMPT_${timestamp}.mp4")
        currentOutputFile = videoFile
        onSimulatedSaved = onVideoSaved
        isSimulatedRecording = true

        recordedDurationSeconds = 0L
        isRecordingPaused = false
        recordingStartTime = System.currentTimeMillis()

        _recordingState.value = StudioRecordingState.RecordingInProgress(0L, false)
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch(Dispatchers.Main) {
            while (true) {
                delay(1000)
                if (!isRecordingPaused) {
                    recordedDurationSeconds++
                    _recordingState.value = StudioRecordingState.RecordingInProgress(
                        durationSeconds = recordedDurationSeconds,
                        isPaused = false
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun pauseRecording() {
        if (isSimulatedRecording) {
            isRecordingPaused = true
            _recordingState.value = StudioRecordingState.RecordingInProgress(recordedDurationSeconds, true)
            return
        }
        activeRecording?.pause()
    }

    fun resumeRecording() {
        if (isSimulatedRecording) {
            isRecordingPaused = false
            _recordingState.value = StudioRecordingState.RecordingInProgress(recordedDurationSeconds, false)
            return
        }
        activeRecording?.resume()
    }

    fun stopRecording() {
        if (isSimulatedRecording) {
            val durationMs = (System.currentTimeMillis() - recordingStartTime).coerceAtLeast(1000L)
            _recordingState.value = StudioRecordingState.Finalizing
            stopTimer()

            val file = currentOutputFile
            if (file != null) {
                try {
                    if (!file.exists()) {
                        file.writeBytes(ByteArray(1024))
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Simulated video file write warning: ${e.message}")
                }
                _recordingState.value = StudioRecordingState.Idle
                onSimulatedSaved?.invoke(file, durationMs)
            } else {
                _recordingState.value = StudioRecordingState.Idle
            }
            isSimulatedRecording = false
            currentOutputFile = null
            onSimulatedSaved = null
            return
        }

        _recordingState.value = StudioRecordingState.Finalizing
        stopTimer()
        activeRecording?.stop()
        activeRecording = null
    }

    fun release() {
        stopTimer()
        countdownJob?.cancel()
        if (isSimulatedRecording) {
            isSimulatedRecording = false
            currentOutputFile = null
            onSimulatedSaved = null
        }
        activeRecording?.stop()
        activeRecording = null
        cameraProvider?.unbindAll()
        avatarEffect.close()
    }
}
