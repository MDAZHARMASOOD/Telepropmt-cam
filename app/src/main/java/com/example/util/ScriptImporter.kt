package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

object ScriptImporter {
    private const val TAG = "ScriptImporter"

    /**
     * Reads script title and body from a document Uri (Local Storage, Downloads, or Google Drive)
     */
    suspend fun importFromUri(context: Context, uri: Uri): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            var fileName = "Imported Script"

            // Query file name
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) {
                                fileName = name.substringBeforeLast(".")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not query display name from uri", e)
            }

            // Attempt to read text content
            var content: String? = null

            // 1. Try typed asset for Google Docs / cloud docs that convert to plain text
            try {
                context.contentResolver.openTypedAssetFileDescriptor(uri, "text/plain", null)?.use { afd ->
                    afd.createInputStream().use { stream ->
                        content = stream.bufferedReader().use { it.readText() }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "openTypedAssetFileDescriptor not applicable, trying standard stream: ${e.message}")
            }

            // 2. Fallback to standard input stream
            if (content.isNullOrBlank()) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    content = inputStream.bufferedReader().use { it.readText() }
                }
            }

            if (content.isNullOrBlank()) {
                return@withContext Result.failure(Exception("The selected file is empty or could not be read."))
            }

            Result.success(Pair(fileName, content!!.trim()))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import script from uri", e)
            Result.failure(Exception("Failed to read file: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    /**
     * Imports script text from a Google Drive or Google Docs shared URL
     */
    suspend fun importFromGoogleDriveLink(rawUrl: String): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val trimmed = rawUrl.trim()
            if (trimmed.isBlank()) {
                return@withContext Result.failure(Exception("URL cannot be empty"))
            }

            // Extract Google Doc ID: https://docs.google.com/document/d/([a-zA-Z0-9_-]+)
            val docPattern = Pattern.compile("docs\\.google\\.com/document/d/([a-zA-Z0-9_-]+)")
            val docMatcher = docPattern.matcher(trimmed)

            // Extract Google Drive File ID: https://drive.google.com/file/d/([a-zA-Z0-9_-]+) or id=([a-zA-Z0-9_-]+)
            val drivePattern = Pattern.compile("drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)")
            val driveMatcher = drivePattern.matcher(trimmed)
            val driveIdParamPattern = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)")
            val driveIdMatcher = driveIdParamPattern.matcher(trimmed)

            val downloadUrl = when {
                docMatcher.find() -> {
                    val docId = docMatcher.group(1)
                    "https://docs.google.com/document/d/$docId/export?format=txt"
                }
                driveMatcher.find() -> {
                    val fileId = driveMatcher.group(1)
                    "https://drive.google.com/uc?export=download&id=$fileId"
                }
                driveIdMatcher.find() -> {
                    val fileId = driveIdMatcher.group(1)
                    "https://drive.google.com/uc?export=download&id=$fileId"
                }
                else -> {
                    // Try direct URL download
                    trimmed
                }
            }

            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 12000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "TelePrompt-App/1.0")
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext Result.failure(
                    Exception("Failed to download script (HTTP $responseCode). Please check that the Google Doc or Drive link is set to 'Anyone with the link can view'.")
                )
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }.trim()

            if (responseText.isBlank()) {
                return@withContext Result.failure(Exception("Downloaded document is empty."))
            }

            // Check if returned text looks like Google login HTML page instead of document text
            if (responseText.contains("<html", ignoreCase = true) && responseText.contains("accounts.google.com", ignoreCase = true)) {
                return@withContext Result.failure(
                    Exception("Access restricted. Please ensure the Google Doc/Drive share permissions are set to 'Anyone with the link can view'.")
                )
            }

            // Determine title
            val title = when {
                docMatcher.find(0) -> "Google Doc Script"
                driveMatcher.find(0) -> "Google Drive Script"
                else -> "Cloud Script"
            }

            Result.success(Pair(title, responseText))
        } catch (e: Exception) {
            Log.e(TAG, "Error importing from URL", e)
            Result.failure(Exception("Failed to download script from Google Drive: ${e.localizedMessage}"))
        }
    }
}
