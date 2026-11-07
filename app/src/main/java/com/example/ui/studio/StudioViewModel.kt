package com.example.ui.studio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.CameraRecordingManager
import com.example.camera.StudioRecordingState
import com.example.data.db.AppDatabase
import com.example.data.model.Recording
import com.example.data.model.Script
import com.example.teleprompter.TeleprompterConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val scriptDao = database.scriptDao()
    private val recordingDao = database.recordingDao()

    val cameraManager = CameraRecordingManager(application, viewModelScope)

    val allScripts: StateFlow<List<Script>> = scriptDao.getAllScripts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecordings: StateFlow<List<Recording>> = recordingDao.getAllRecordings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedScript = MutableStateFlow<Script?>(null)
    val selectedScript: StateFlow<Script?> = _selectedScript.asStateFlow()

    private val _freestyleMode = MutableStateFlow(false)
    val freestyleMode: StateFlow<Boolean> = _freestyleMode.asStateFlow()

    private val _teleprompterConfig = MutableStateFlow(TeleprompterConfig())
    val teleprompterConfig: StateFlow<TeleprompterConfig> = _teleprompterConfig.asStateFlow()

    private val _lastSavedRecording = MutableStateFlow<Recording?>(null)
    val lastSavedRecording: StateFlow<Recording?> = _lastSavedRecording.asStateFlow()

    private val _showScriptPicker = MutableStateFlow(false)
    val showScriptPicker: StateFlow<Boolean> = _showScriptPicker.asStateFlow()

    private val _showSettingsSheet = MutableStateFlow(false)
    val showSettingsSheet: StateFlow<Boolean> = _showSettingsSheet.asStateFlow()

    private val _showSavedSuccessDialog = MutableStateFlow(false)
    val showSavedSuccessDialog: StateFlow<Boolean> = _showSavedSuccessDialog.asStateFlow()

    private val _selectedFilter = MutableStateFlow(com.example.filter.VideoFilterType.ORIGINAL)
    val selectedFilter: StateFlow<com.example.filter.VideoFilterType> = _selectedFilter.asStateFlow()

    private val _showFilterPicker = MutableStateFlow(false)
    val showFilterPicker: StateFlow<Boolean> = _showFilterPicker.asStateFlow()

    private val _makeupIntensity = MutableStateFlow(0.85f)
    val makeupIntensity: StateFlow<Float> = _makeupIntensity.asStateFlow()

    private val _selectedPlatformLimit = MutableStateFlow(com.example.camera.ShortFormPlatformLimit.SHORTS_60)
    val selectedPlatformLimit: StateFlow<com.example.camera.ShortFormPlatformLimit> = _selectedPlatformLimit.asStateFlow()

    private val _autoStopOnLimit = MutableStateFlow(true)
    val autoStopOnLimit: StateFlow<Boolean> = _autoStopOnLimit.asStateFlow()

    fun selectPlatformLimit(limit: com.example.camera.ShortFormPlatformLimit) {
        _selectedPlatformLimit.value = limit
    }

    fun toggleAutoStopOnLimit(enable: Boolean) {
        _autoStopOnLimit.value = enable
    }

    fun selectFilter(type: com.example.filter.VideoFilterType) {
        _selectedFilter.value = type
    }

    fun updateMakeupIntensity(intensity: Float) {
        _makeupIntensity.value = intensity.coerceIn(0.15f, 1.0f)
    }

    fun toggleMakeupFilter(): Boolean {
        return if (_selectedFilter.value == com.example.filter.VideoFilterType.MAKEUP) {
            _selectedFilter.value = com.example.filter.VideoFilterType.ORIGINAL
            false
        } else {
            _selectedFilter.value = com.example.filter.VideoFilterType.MAKEUP
            true
        }
    }

    fun toggleFilterPicker(show: Boolean) {
        _showFilterPicker.value = show
    }

    fun saveVideoToGallery(
        context: android.content.Context,
        filePath: String,
        title: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = com.example.util.SocialExportManager.saveVideoToGallery(context, filePath, title)
            result.onSuccess {
                onResult(true, "Saved to device Gallery (Movies/TelePrompt)!")
            }.onFailure {
                onResult(false, it.message ?: "Failed to save video")
            }
        }
    }

    fun selectScript(script: Script) {
        _selectedScript.value = script
        _freestyleMode.value = false
        _teleprompterConfig.value = _teleprompterConfig.value.copy(
            scrollSpeed = script.scrollSpeed,
            overlayOpacity = script.overlayOpacity,
            fontSizeSp = script.fontSizeSp,
            isMirrored = script.isMirrored,
            textAlign = script.textAlign,
            isAutoScrollActive = false
        )
        _showScriptPicker.value = false
    }

    fun continueWithoutScript() {
        _selectedScript.value = null
        _freestyleMode.value = true
        _showScriptPicker.value = false
    }

    fun updateTeleprompterConfig(newConfig: TeleprompterConfig) {
        _teleprompterConfig.value = newConfig
    }

    fun toggleScriptPicker(show: Boolean) {
        _showScriptPicker.value = show
    }

    fun toggleSettingsSheet(show: Boolean) {
        _showSettingsSheet.value = show
    }

    fun dismissSavedDialog() {
        _showSavedSuccessDialog.value = false
    }

    fun startRecording() {
        val countdown = _teleprompterConfig.value.countdownDurationSec
        // Auto start scrolling text when recording starts!
        cameraManager.startCountdownAndRecord(
            countdownSeconds = countdown
        ) { videoFile, durationMs ->
            saveRecordingToDb(videoFile, durationMs)
        }

        // When countdown finishes, set auto scroll active
        if (countdown > 0) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(countdown * 1000L)
                _teleprompterConfig.value = _teleprompterConfig.value.copy(isAutoScrollActive = true)
            }
        } else {
            _teleprompterConfig.value = _teleprompterConfig.value.copy(isAutoScrollActive = true)
        }
    }

    fun pauseRecording() {
        cameraManager.pauseRecording()
        _teleprompterConfig.value = _teleprompterConfig.value.copy(isAutoScrollActive = false)
    }

    fun resumeRecording() {
        cameraManager.resumeRecording()
        _teleprompterConfig.value = _teleprompterConfig.value.copy(isAutoScrollActive = true)
    }

    fun stopRecording() {
        cameraManager.stopRecording()
        _teleprompterConfig.value = _teleprompterConfig.value.copy(isAutoScrollActive = false)
    }

    private fun saveRecordingToDb(file: File, durationMs: Long) {
        viewModelScope.launch {
            val script = _selectedScript.value
            val title = if (script != null) {
                "${script.title} (Take)"
            } else {
                "Quick Short ${System.currentTimeMillis() % 10000}"
            }

            val newRecording = Recording(
                title = title,
                filePath = file.absolutePath,
                durationMs = durationMs,
                fileSizeBytes = file.length(),
                createdAt = System.currentTimeMillis(),
                scriptTitle = script?.title ?: "Freestyle"
            )
            val id = recordingDao.insertRecording(newRecording)
            val savedWithId = newRecording.copy(id = id)
            _lastSavedRecording.value = savedWithId
            _showSavedSuccessDialog.value = true
        }
    }

    fun saveScript(title: String, content: String, existingId: Long = 0) {
        viewModelScope.launch {
            val config = _teleprompterConfig.value
            val script = Script(
                id = existingId,
                title = title.ifBlank { "Untitled Script" },
                content = content,
                scrollSpeed = config.scrollSpeed,
                overlayOpacity = config.overlayOpacity,
                fontSizeSp = config.fontSizeSp,
                isMirrored = config.isMirrored,
                textAlign = config.textAlign,
                updatedAt = System.currentTimeMillis()
            )
            val insertedId = scriptDao.insertScript(script)
            selectScript(script.copy(id = if (existingId != 0L) existingId else insertedId))
        }
    }

    fun deleteScript(script: Script) {
        viewModelScope.launch {
            scriptDao.deleteScript(script)
            if (_selectedScript.value?.id == script.id) {
                _selectedScript.value = null
                _freestyleMode.value = false
            }
        }
    }

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importMessage = MutableStateFlow<String?>(null)
    val importMessage: StateFlow<String?> = _importMessage.asStateFlow()

    fun clearImportMessage() {
        _importMessage.value = null
    }

    fun importScriptFromUri(
        context: android.content.Context,
        uri: android.net.Uri,
        onSuccess: (Script) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isImporting.value = true
            val result = com.example.util.ScriptImporter.importFromUri(context, uri)
            _isImporting.value = false
            result.onSuccess { (title, content) ->
                val config = _teleprompterConfig.value
                val newScript = Script(
                    title = title,
                    content = content,
                    scrollSpeed = config.scrollSpeed,
                    overlayOpacity = config.overlayOpacity,
                    fontSizeSp = config.fontSizeSp,
                    textAlign = config.textAlign
                )
                val id = scriptDao.insertScript(newScript)
                val saved = newScript.copy(id = id)
                selectScript(saved)
                _importMessage.value = "Imported \"$title\" successfully!"
                onSuccess(saved)
            }.onFailure { error ->
                _importMessage.value = error.message ?: "Failed to import file"
            }
        }
    }

    fun importScriptFromGoogleDriveUrl(
        url: String,
        onSuccess: (Script) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isImporting.value = true
            val result = com.example.util.ScriptImporter.importFromGoogleDriveLink(url)
            _isImporting.value = false
            result.onSuccess { (title, content) ->
                val config = _teleprompterConfig.value
                val newScript = Script(
                    title = title,
                    content = content,
                    scrollSpeed = config.scrollSpeed,
                    overlayOpacity = config.overlayOpacity,
                    fontSizeSp = config.fontSizeSp,
                    textAlign = config.textAlign
                )
                val id = scriptDao.insertScript(newScript)
                val saved = newScript.copy(id = id)
                selectScript(saved)
                _importMessage.value = "Imported \"$title\" from Google Drive!"
                onSuccess(saved)
            }.onFailure { error ->
                _importMessage.value = error.message ?: "Failed to import from Google Drive"
            }
        }
    }

    fun deleteRecording(recording: Recording) {
        viewModelScope.launch {
            recordingDao.deleteRecording(recording)
            try {
                val file = File(recording.filePath)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.release()
    }
}
