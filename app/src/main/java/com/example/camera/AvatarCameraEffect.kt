package com.example.camera

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.os.Handler
import android.os.Looper
import androidx.camera.core.CameraEffect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.effects.OverlayEffect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.atomic.AtomicReference

enum class AvatarStyle(val label: String, val emoji: String) {
    OFF("Off", "○"),
    NOVA("Nova", "✨"),
    ORBIT("Orbit", "🪐"),
    LUMI("Lumi", "🐱")
}

/** Face tracking is performed on-device; matched timestamps keep the face effect in sync with video. */
class AvatarCameraEffect {
    private val latestFace = AtomicReference<TrackedFace?>(null)
    private val selectedStyle = AtomicReference(AvatarStyle.OFF)
    private val detector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .enableTracking()
            .build()
    )

    val overlay = OverlayEffect(
        CameraEffect.PREVIEW or CameraEffect.VIDEO_CAPTURE,
        4,
        Handler(Looper.getMainLooper()),
        { error -> Log.w("AvatarCameraEffect", "Camera avatar overlay error", error) }
    ).apply {
        setOnDrawListener { frame ->
            val tracked = latestFace.get()
            val style = selectedStyle.get()
            if (tracked != null && style != AvatarStyle.OFF) {
                drawAvatar(frame.overlayCanvas, frame.size.width.toFloat(), frame.size.height.toFloat(), tracked, style)
            }
            true
        }
    }

    val analyzer = ImageAnalysis.Analyzer { imageProxy -> analyze(imageProxy) }

    fun select(style: AvatarStyle) {
        selectedStyle.set(style)
        if (style == AvatarStyle.OFF) latestFace.set(null)
    }

    private fun analyze(proxy: ImageProxy) {
        val mediaImage = proxy.image
        if (mediaImage == null || selectedStyle.get() == AvatarStyle.OFF) {
            latestFace.set(null)
            proxy.close()
            return
        }
        val rotation = proxy.imageInfo.rotationDegrees
        val input = InputImage.fromMediaImage(mediaImage, rotation)
        val width = if (rotation == 90 || rotation == 270) proxy.height else proxy.width
        val height = if (rotation == 90 || rotation == 270) proxy.width else proxy.height
        detector.process(input)
            .addOnSuccessListener { faces ->
                latestFace.set(faces.firstOrNull()?.let { normalize(it, width, height) })
                overlay.drawFrameAsync(proxy.imageInfo.timestamp)
            }
            .addOnFailureListener { latestFace.set(null) }
            .addOnCompleteListener { proxy.close() }
    }

    private fun normalize(face: Face, width: Int, height: Int): TrackedFace? {
        if (width <= 0 || height <= 0) return null
        val bounds = face.boundingBox
        val left = (bounds.left.toFloat() / width).coerceIn(0f, 1f)
        val top = (bounds.top.toFloat() / height).coerceIn(0f, 1f)
        val right = (bounds.right.toFloat() / width).coerceIn(0f, 1f)
        val bottom = (bounds.bottom.toFloat() / height).coerceIn(0f, 1f)
        if (right <= left || bottom <= top) return null
        return TrackedFace(left, top, right, bottom, face.headEulerAngleZ)
    }

    private fun drawAvatar(canvas: Canvas, width: Float, height: Float, face: TrackedFace, style: AvatarStyle) {
        val left = face.left * width
        val top = face.top * height
        val right = face.right * width
        val bottom = face.bottom * height
        val faceWidth = right - left
        val faceHeight = bottom - top
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val colors = when (style) {
            AvatarStyle.NOVA -> intArrayOf(Color.rgb(71, 236, 255), Color.rgb(89, 70, 255))
            AvatarStyle.ORBIT -> intArrayOf(Color.rgb(255, 207, 105), Color.rgb(238, 105, 87))
            AvatarStyle.LUMI -> intArrayOf(Color.rgb(255, 165, 226), Color.rgb(133, 98, 255))
            AvatarStyle.OFF -> return
        }

        val save = canvas.save()
        canvas.rotate(face.roll, cx, cy)
        val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors[0]; style = Paint.Style.STROKE; strokeWidth = faceWidth * 0.055f }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors[1]; alpha = 224; style = Paint.Style.FILL }
        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; alpha = 220; style = Paint.Style.STROKE; strokeWidth = faceWidth * 0.025f }
        val feature = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
        val faceRect = RectF(left - faceWidth * .11f, top - faceHeight * .12f, right + faceWidth * .11f, bottom + faceHeight * .10f)

        // A stylized, translucent character face that follows the detected head.
        canvas.drawOval(faceRect, fill)
        canvas.drawOval(faceRect, outline)
        if (style == AvatarStyle.LUMI) {
            val ear = android.graphics.Path().apply {
                moveTo(left + faceWidth * .12f, top + faceHeight * .06f)
                lineTo(left + faceWidth * .20f, top - faceHeight * .30f)
                lineTo(left + faceWidth * .42f, top + faceHeight * .02f)
                close()
                moveTo(right - faceWidth * .12f, top + faceHeight * .06f)
                lineTo(right - faceWidth * .20f, top - faceHeight * .30f)
                lineTo(right - faceWidth * .42f, top + faceHeight * .02f)
                close()
            }
            canvas.drawPath(ear, fill)
            canvas.drawPath(ear, outline)
        } else {
            canvas.drawRoundRect(RectF(left + faceWidth * .05f, top - faceHeight * .19f, right - faceWidth * .05f, top + faceHeight * .10f), faceWidth * .18f, faceWidth * .18f, fill)
            canvas.drawRoundRect(RectF(left + faceWidth * .05f, top - faceHeight * .19f, right - faceWidth * .05f, top + faceHeight * .10f), faceWidth * .18f, faceWidth * .18f, outline)
        }

        val eyeY = top + faceHeight * .42f
        val eyeRadius = faceWidth * .075f
        canvas.drawCircle(left + faceWidth * .32f, eyeY, eyeRadius, feature)
        canvas.drawCircle(right - faceWidth * .32f, eyeY, eyeRadius, feature)
        canvas.drawArc(RectF(left + faceWidth * .34f, top + faceHeight * .53f, right - faceWidth * .34f, bottom - faceHeight * .10f), 15f, 150f, false, highlight)
        if (style == AvatarStyle.NOVA) {
            canvas.drawLine(left - faceWidth * .18f, cy, left + faceWidth * .02f, cy, outline)
            canvas.drawLine(right - faceWidth * .02f, cy, right + faceWidth * .18f, cy, outline)
        }
        canvas.restoreToCount(save)
    }

    fun close() {
        overlay.close()
        detector.close()
    }

    private data class TrackedFace(val left: Float, val top: Float, val right: Float, val bottom: Float, val roll: Float)
}
