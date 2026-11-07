package com.example.teleprompter

data class TeleprompterConfig(
    val scrollSpeed: Float = 35f, // pixels per second (approx 10 to 120)
    val overlayOpacity: Float = 0.48f, // 0.10 to 0.95 (default 48% black background)
    val fontSizeSp: Int = 22, // 16 to 36
    val isMirrored: Boolean = false,
    val textAlign: String = "CENTER",
    val isAutoScrollActive: Boolean = false,
    val heightFraction: Float = 0.42f, // height proportion of screen (e.g. 0.28 to 0.60)
    val countdownDurationSec: Int = 3,
    val showGuideLine: Boolean = true
)
