package com.example.filter

import androidx.compose.ui.graphics.ColorMatrix

enum class VideoFilterType(val displayName: String, val emoji: String) {
    ORIGINAL("Normal", "✨"),
    MAKEUP("Makeup", "💄"),
    SEPIA("Sepia", "🎞️"),
    GRAYSCALE("B&W", "🖤"),
    HIGH_CONTRAST("Contrast", "⚡"),
    WARM_SUNSET("Warm", "🌅"),
    COOL_CYBER("Cool", "❄️"),
    VIVID("Vivid", "🎨"),
    NOIR("Noir", "🎬")
}

data class VideoFilter(
    val type: VideoFilterType,
    val colorMatrix: ColorMatrix
) {
    companion object {
        val ALL_FILTERS = listOf(
            VideoFilter(
                type = VideoFilterType.ORIGINAL,
                colorMatrix = ColorMatrix()
            ),
            VideoFilter(
                type = VideoFilterType.MAKEUP,
                colorMatrix = ColorMatrix(
                    floatArrayOf(
                        1.14f, 0.05f, 0.02f, 0f, 16f,
                        0.02f, 1.04f, 0.02f, 0f, 12f,
                        0.02f, 0.02f, 0.96f, 0f, 8f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            VideoFilter(
                type = VideoFilterType.SEPIA,
                colorMatrix = ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            VideoFilter(
                type = VideoFilterType.GRAYSCALE,
                colorMatrix = ColorMatrix().apply { setToSaturation(0f) }
            ),
            VideoFilter(
                type = VideoFilterType.HIGH_CONTRAST,
                colorMatrix = ColorMatrix(
                    floatArrayOf(
                        1.45f, 0f, 0f, 0f, -35f,
                        0f, 1.45f, 0f, 0f, -35f,
                        0f, 0f, 1.45f, 0f, -35f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            VideoFilter(
                type = VideoFilterType.WARM_SUNSET,
                colorMatrix = ColorMatrix(
                    floatArrayOf(
                        1.25f, 0f, 0f, 0f, 15f,
                        0f, 1.05f, 0f, 0f, 5f,
                        0f, 0f, 0.82f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            VideoFilter(
                type = VideoFilterType.COOL_CYBER,
                colorMatrix = ColorMatrix(
                    floatArrayOf(
                        0.85f, 0f, 0f, 0f, -10f,
                        0f, 1.05f, 0f, 0f, 10f,
                        0f, 0f, 1.30f, 0f, 25f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            VideoFilter(
                type = VideoFilterType.VIVID,
                colorMatrix = ColorMatrix().apply { setToSaturation(1.65f) }
            ),
            VideoFilter(
                type = VideoFilterType.NOIR,
                colorMatrix = ColorMatrix(
                    floatArrayOf(
                        0.299f * 1.5f, 0.587f * 1.5f, 0.114f * 1.5f, 0f, -30f,
                        0.299f * 1.5f, 0.587f * 1.5f, 0.114f * 1.5f, 0f, -30f,
                        0.299f * 1.5f, 0.587f * 1.5f, 0.114f * 1.5f, 0f, -30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        )

        fun getMakeupColorMatrix(intensity: Float = 0.85f): ColorMatrix {
            val clamped = intensity.coerceIn(0f, 1f)
            val rScale = 1.0f + 0.16f * clamped
            val gScale = 1.0f + 0.05f * clamped
            val bScale = 1.0f - 0.03f * clamped
            val rOffset = 18f * clamped
            val gOffset = 12f * clamped
            val bOffset = 8f * clamped
            val cross = 0.02f * clamped

            return ColorMatrix(
                floatArrayOf(
                    rScale, cross, cross, 0f, rOffset,
                    cross, gScale, cross, 0f, gOffset,
                    cross, cross, bScale, 0f, bOffset,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }

        fun getFilter(type: VideoFilterType, makeupIntensity: Float = 0.85f): VideoFilter {
            if (type == VideoFilterType.MAKEUP) {
                return VideoFilter(
                    type = VideoFilterType.MAKEUP,
                    colorMatrix = getMakeupColorMatrix(makeupIntensity)
                )
            }
            return ALL_FILTERS.firstOrNull { it.type == type } ?: ALL_FILTERS.first()
        }
    }
}
