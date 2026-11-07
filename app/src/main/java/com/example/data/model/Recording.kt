package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class Recording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val scriptTitle: String = ""
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    val formattedSize: String
        get() {
            val mb = fileSizeBytes / (1024f * 1024f)
            return if (mb >= 1f) {
                "%.1f MB".format(mb)
            } else {
                "%.0f KB".format(fileSizeBytes / 1024f)
            }
        }
}
