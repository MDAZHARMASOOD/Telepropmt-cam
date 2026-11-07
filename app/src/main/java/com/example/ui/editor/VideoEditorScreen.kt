package com.example.ui.editor

import android.content.Context
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.filter.VideoFilter
import com.example.filter.VideoFilterType
import com.example.ui.studio.StudioViewModel
import com.example.ui.theme.TorqueBluePrimary
import com.example.ui.theme.TorqueBlueContainer
import com.example.ui.theme.TorqueBlueOnContainer
import com.example.ui.theme.TorqueBlueAccent
import com.example.ui.util.scaleOnPress
import com.example.util.SocialExportManager
import java.io.File

@Composable
fun VideoEditorScreen(
    videoFilePath: String,
    videoTitle: String,
    viewModel: StudioViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val videoFile = remember(videoFilePath) { File(videoFilePath) }

    var selectedFilterType by remember { mutableStateOf(VideoFilterType.ORIGINAL) }
    var captionText by remember { mutableStateOf("#TelePrompt #Creator #Studio #Shorts") }
    var isSavedToGallery by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val activeFilter = remember(selectedFilterType) {
        VideoFilter.getFilter(selectedFilterType)
    }

    val verticalScroll = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(verticalScroll)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("video_editor_screen")
        ) {
            // Header: Back, Title, Save to Gallery
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .scaleOnPress(0.96f)
                            .testTag("video_editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Video Polish & Export",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Color grade & social export",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Quick Save to Device Local Storage / Gallery
                Button(
                    onClick = {
                        isSaving = true
                        viewModel.saveVideoToGallery(context, videoFilePath, videoTitle) { success, msg ->
                            isSaving = false
                            isSavedToGallery = success
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSavedToGallery) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                        contentColor = if (isSavedToGallery) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .scaleOnPress(0.96f)
                        .testTag("save_to_device_gallery_button")
                ) {
                    Icon(
                        imageVector = if (isSavedToGallery) Icons.Default.Check else Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSavedToGallery) "Saved" else "Save Phone",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 9:16 Video Player Container with Real-Time Video Filter Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                if (videoFile.exists()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                if (selectedFilterType != VideoFilterType.ORIGINAL) {
                                    val paint = Paint().apply {
                                        colorFilter = ColorFilter.colorMatrix(activeFilter.colorMatrix)
                                    }
                                    drawIntoCanvas { canvas ->
                                        canvas.withSaveLayer(size.toRect(), paint) {
                                            drawContent()
                                        }
                                    }
                                } else {
                                    drawContent()
                                }
                            }
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    val mediaController = MediaController(ctx)
                                    mediaController.setAnchorView(this)
                                    setMediaController(mediaController)
                                    setVideoPath(videoFile.absolutePath)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Active Filter Badge at top right
                    if (selectedFilterType != VideoFilterType.ORIGINAL) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color.Black.copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, TorqueBlueAccent),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${activeFilter.type.emoji} ${activeFilter.type.displayName}",
                                color = TorqueBlueAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                } else {
                    Text("Video file not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Real-Time Video Filter Selector Carousel
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FilterVintage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Video Filters (${selectedFilterType.displayName})",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Filter Chips (M3 Filter Chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VideoFilter.ALL_FILTERS.forEach { filter ->
                    val isSelected = selectedFilterType == filter.type
                    Card(
                        onClick = { selectedFilterType = filter.type },
                        shape = RoundedCornerShape(9999.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .scaleOnPress(0.96f)
                            .testTag("filter_chip_${filter.type.name}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(text = filter.type.emoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = filter.type.displayName,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Post Caption / Hashtags input
            Text(
                text = "Social Caption & Hashtags",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = captionText,
                onValueChange = { captionText = it },
                label = { Text("Caption for Reels/Shorts") },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("caption_input_field")
            )

            Spacer(modifier = Modifier.height(22.dp))

            // DIRECT SOCIAL MEDIA UPLOAD SECTION
            Text(
                text = "🚀 Direct Upload to Social Platforms",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Launch directly into creator tools with caption preloaded",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Instagram Reels Button
            Button(
                onClick = {
                    SocialExportManager.shareToSocialPlatform(
                        context = context,
                        videoFilePath = videoFilePath,
                        platform = SocialExportManager.SocialPlatform.INSTAGRAM,
                        caption = captionText
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE1306C)
                ),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .scaleOnPress(0.96f)
                    .testTag("upload_instagram_reels_button")
            ) {
                Text(
                    text = "📸 Upload to Instagram Reels",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // YouTube Shorts Button
            Button(
                onClick = {
                    SocialExportManager.shareToSocialPlatform(
                        context = context,
                        videoFilePath = videoFilePath,
                        platform = SocialExportManager.SocialPlatform.YOUTUBE,
                        caption = captionText
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF0000)
                ),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .scaleOnPress(0.96f)
                    .testTag("upload_youtube_shorts_button")
            ) {
                Text(
                    text = "▶️ Upload to YouTube Shorts",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Facebook Reels Button
            Button(
                onClick = {
                    SocialExportManager.shareToSocialPlatform(
                        context = context,
                        videoFilePath = videoFilePath,
                        platform = SocialExportManager.SocialPlatform.FACEBOOK,
                        caption = captionText
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1877F2)
                ),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .scaleOnPress(0.96f)
                    .testTag("upload_facebook_reels_button")
            ) {
                Text(
                    text = "👥 Upload to Facebook Reels",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // General System Share (M3 Outlined Pill Button)
            OutlinedButton(
                onClick = {
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "video/mp4"
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            videoFile
                        )
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        putExtra(android.content.Intent.EXTRA_TEXT, captionText)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Take"))
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .scaleOnPress(0.96f)
                    .testTag("general_share_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("More Share Options (TikTok, WhatsApp, etc.)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
