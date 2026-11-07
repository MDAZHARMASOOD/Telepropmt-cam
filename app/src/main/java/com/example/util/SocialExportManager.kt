package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

object SocialExportManager {

    enum class SocialPlatform(
        val appName: String,
        val packageName: String,
        val targetFeature: String,
        val hexColor: Long
    ) {
        INSTAGRAM("Instagram", "com.instagram.android", "Reels / Story", 0xFFE1306C),
        FACEBOOK("Facebook", "com.facebook.katana", "Reels / Feed", 0xFF1877F2),
        YOUTUBE("YouTube", "com.google.android.youtube", "Shorts", 0xFFFF0000)
    }

    /**
     * Copies video from private app storage into the device's public Movies/TelePrompt gallery
     */
    suspend fun saveVideoToGallery(
        context: Context,
        sourceFilePath: String,
        videoTitle: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(sourceFilePath)
            if (!sourceFile.exists()) {
                return@withContext Result.failure(Exception("Source video file not found"))
            }

            val sanitizedTitle = videoTitle.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val fileName = "TelePrompt_${sanitizedTitle}_${System.currentTimeMillis()}.mp4"

            val resolver = context.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/TelePrompt")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val collectionUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collectionUri, contentValues)
                    ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

                resolver.openOutputStream(itemUri)?.use { out ->
                    FileInputStream(sourceFile).use { input ->
                        input.copyTo(out)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)

                Result.success(itemUri)
            } else {
                @Suppress("DEPRECATION")
                val moviesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "TelePrompt"
                )
                if (!moviesDir.exists()) moviesDir.mkdirs()
                val destFile = File(moviesDir, fileName)

                sourceFile.copyTo(destFile, overwrite = true)

                val uri = Uri.fromFile(destFile)
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                    data = uri
                }
                context.sendBroadcast(mediaScanIntent)

                Result.success(uri)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save to gallery: ${e.localizedMessage}"))
        }
    }

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun shareToSocialPlatform(
        context: Context,
        videoFilePath: String,
        platform: SocialPlatform,
        caption: String = "#TelePrompt #Shorts #Reels"
    ) {
        val file = File(videoFilePath)
        if (!file.exists()) {
            Toast.makeText(context, "Video file not found", Toast.LENGTH_SHORT).show()
            return
        }

        val videoUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val isInstalled = isAppInstalled(context, platform.packageName)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, videoUri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (isInstalled) {
                setPackage(platform.packageName)
            }
        }

        try {
            if (isInstalled) {
                // If Instagram, also try direct Reels share action
                if (platform == SocialPlatform.INSTAGRAM) {
                    val reelIntent = Intent("com.instagram.share.ADD_TO_REEL").apply {
                        setDataAndType(videoUri, "video/mp4")
                        putExtra(Intent.EXTRA_STREAM, videoUri)
                        putExtra("interactive_asset_uri", videoUri)
                        setPackage(platform.packageName)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    if (reelIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(reelIntent)
                        return
                    }
                }
                context.startActivity(shareIntent)
            } else {
                Toast.makeText(
                    context,
                    "${platform.appName} is not installed. Opening share options...",
                    Toast.LENGTH_SHORT
                ).show()
                val chooser = Intent.createChooser(shareIntent, "Share Video Take to ${platform.appName}")
                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            // Fallback to general chooser
            val chooser = Intent.createChooser(shareIntent, "Share Video Take")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(chooser)
        }
    }
}
