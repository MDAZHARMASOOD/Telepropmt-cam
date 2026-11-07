package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class Script(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val scrollSpeed: Float = 35f, // speed in pixels per second
    val overlayOpacity: Float = 0.65f, // opacity 0.1 to 0.95
    val fontSizeSp: Int = 22,
    val isMirrored: Boolean = false,
    val textAlign: String = "CENTER", // "CENTER", "LEFT", "RIGHT"
    val updatedAt: Long = System.currentTimeMillis()
) {
    val wordCount: Int
        get() = content.trim().split("\\s+".toRegex()).count { it.isNotBlank() }

    // Estimated duration in seconds assuming average 130 words per minute
    val estimatedDurationSec: Int
        get() = if (wordCount == 0) 0 else ((wordCount / 130f) * 60).toInt().coerceAtLeast(1)
}
