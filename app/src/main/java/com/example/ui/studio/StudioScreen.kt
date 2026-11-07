package com.example.ui.studio

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.CheckCircle
import com.example.ui.ai.AnimatePhotoToVideoDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSaveLayer
import com.example.camera.ShortFormPlatformLimit
import com.example.camera.StudioRecordingState
import com.example.filter.VideoFilter
import com.example.filter.VideoFilterType
import com.example.ui.scripts.UploadScriptDialog
import com.example.ui.teleprompter.TeleprompterOverlay
import com.example.ui.theme.FantaGreen
import com.example.ui.theme.FantaOrange
import com.example.ui.theme.FantaOrangeActive
import com.example.ui.theme.OnFantaGreen
import com.example.ui.theme.OnFantaOrange
import com.example.ui.theme.ChationYellow
import com.example.ui.theme.ChationDark
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioIndigoLight
import com.example.ui.theme.StudioRecordRed
import com.example.ui.theme.StudioRecordRedDark
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.journey.CustomerJourneyDialog
import com.example.ui.util.scaleOnPress
import com.example.ui.theme.WiseBodyFontFamily
import com.example.ui.theme.WiseDisplayFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    onNavigateToScripts: () -> Unit,
    onNavigateToRecordings: () -> Unit,
    onNavigateToEditor: (filePath: String, title: String) -> Unit,
    onPlayRecording: (String) -> Unit,
    onShareRecording: (String) -> Unit,
    authManager: com.example.auth.AuthManager? = null,
    onNavigateToAuth: () -> Unit = {},
    onReplayOnboarding: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val recordingState by viewModel.cameraManager.recordingState.collectAsState()
    val lensFacing by viewModel.cameraManager.currentLensFacing.collectAsState()
    val selectedScript by viewModel.selectedScript.collectAsState()
    val config by viewModel.teleprompterConfig.collectAsState()
    val allScripts by viewModel.allScripts.collectAsState()
    val showScriptPicker by viewModel.showScriptPicker.collectAsState()
    val showSettingsSheet by viewModel.showSettingsSheet.collectAsState()
    val showSavedSuccess by viewModel.showSavedSuccessDialog.collectAsState()
    val lastSaved by viewModel.lastSavedRecording.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val showFilterPicker by viewModel.showFilterPicker.collectAsState()
    val selectedPlatformLimit by viewModel.selectedPlatformLimit.collectAsState()
    val autoStopOnLimit by viewModel.autoStopOnLimit.collectAsState()
    var showUploadDialog by remember { mutableStateOf(false) }

    val makeupIntensity by viewModel.makeupIntensity.collectAsState()

    val activeFilter = remember(selectedFilter, makeupIntensity) {
        VideoFilter.getFilter(selectedFilter, makeupIntensity)
    }

    var swipeHudMessage by remember { mutableStateOf<String?>(null) }
    var showSwipeHud by remember { mutableStateOf(false) }
    var totalDragDeltaX by remember { mutableFloatStateOf(0f) }
    var isDraggingGesture by remember { mutableStateOf(false) }

    LaunchedEffect(showSwipeHud, isDraggingGesture, swipeHudMessage) {
        if (showSwipeHud && !isDraggingGesture) {
            kotlinx.coroutines.delay(1800L)
            showSwipeHud = false
        }
    }

    val currentUser = authManager?.currentUser?.collectAsState()?.value
    var showProfileDialog by remember { mutableStateOf(false) }
    var showJourneyDialog by remember { mutableStateOf(false) }
    var showAnimatePhotoDialog by remember { mutableStateOf(false) }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("studio_screen")
    ) {
        // CameraX Live Preview View with Real-time Filter
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewViewRef = this
                    viewModel.cameraManager.bindCamera(lifecycleOwner, this)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    if (selectedFilter != VideoFilterType.ORIGINAL) {
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
                .testTag("camera_preview_view")
        )

        // Subtle gradient overlay at top and bottom for UI contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.55f),
                        0.15f to Color.Transparent,
                        0.80f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.75f)
                    )
                )
        )

        // Horizontal Swipe Feature Layer for Makeup Filter (Swipe left/right to toggle or adjust glow)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedFilter, makeupIntensity) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            totalDragDeltaX = 0f
                            isDraggingGesture = true
                        },
                        onDragEnd = {
                            if (kotlin.math.abs(totalDragDeltaX) > 40f) {
                                if (selectedFilter != VideoFilterType.MAKEUP) {
                                    viewModel.selectFilter(VideoFilterType.MAKEUP)
                                    swipeHudMessage = "💄 Makeup Filter: ON"
                                    showSwipeHud = true
                                } else {
                                    if (totalDragDeltaX > 80f) {
                                        viewModel.selectFilter(VideoFilterType.ORIGINAL)
                                        swipeHudMessage = "✨ Natural Mode"
                                        showSwipeHud = true
                                    } else if (totalDragDeltaX < -80f) {
                                        val newIntensity = (makeupIntensity + 0.15f).coerceAtMost(1.0f)
                                        viewModel.updateMakeupIntensity(newIntensity)
                                        swipeHudMessage = "💄 Makeup Glow: ${(newIntensity * 100).toInt()}%"
                                        showSwipeHud = true
                                    }
                                }
                            }
                            isDraggingGesture = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDragDeltaX += dragAmount
                            if (selectedFilter == VideoFilterType.MAKEUP) {
                                val delta = dragAmount / 350f
                                val updated = (makeupIntensity + delta).coerceIn(0.15f, 1.0f)
                                viewModel.updateMakeupIntensity(updated)
                                swipeHudMessage = "💄 Makeup Glow: ${(updated * 100).toInt()}%"
                                showSwipeHud = true
                            }
                        }
                    )
                }
                .testTag("makeup_swipe_surface")
        )

        // Top Header Bar: Reference Grid Menu, Brand Mascot Logo, Recording Pill, Camera & Journey Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarPadding + 8.dp, start = 14.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Group: 4-Square Grid Menu & User Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 4-Square Grid Menu button (matching reference image top-left grid icon)
                IconButton(
                    onClick = onNavigateToScripts,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .scaleOnPress(0.96f)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .testTag("scripts_library_nav_button")
                ) {
                    // 4-square minimalist grid icon
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                            Box(modifier = Modifier.size(7.5.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                            Box(modifier = Modifier.size(7.5.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                            Box(modifier = Modifier.size(7.5.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                            Box(modifier = Modifier.size(7.5.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                        }
                    }
                }

                // User Profile Button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .scaleOnPress(0.96f)
                        .clickable {
                            if (currentUser != null) {
                                showProfileDialog = true
                            } else {
                                onNavigateToAuth()
                            }
                        }
                        .testTag("user_profile_top_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (currentUser?.provider) {
                                com.example.auth.AuthProviderType.GOOGLE -> "🌐"
                                com.example.auth.AuthProviderType.APPLE -> "🍏"
                                com.example.auth.AuthProviderType.PHONE -> "📱"
                                com.example.auth.AuthProviderType.EMAIL -> "✉️"
                                else -> "👤"
                            },
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Center: Brand Mascot Title (Idle) or Live Recording Pill (Recording)
            if (recordingState is StudioRecordingState.RecordingInProgress) {
                val inProgress = recordingState as StudioRecordingState.RecordingInProgress
                val minutes = inProgress.durationSeconds / 60
                val seconds = inProgress.durationSeconds % 60
                val formattedTime = "%02d:%02d".format(minutes, seconds)

                val pulseTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by pulseTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "recording_dot"
                )

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = StudioRecordRedDark.copy(alpha = 0.90f),
                    shadowElevation = 8.dp,
                    modifier = Modifier.testTag("recording_status_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .scale(if (inProgress.isPaused) 1f else pulseAlpha)
                                .background(if (inProgress.isPaused) Color.Yellow else Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val remainingSec = if (selectedPlatformLimit.isLimited) {
                            (selectedPlatformLimit.durationSeconds - inProgress.durationSeconds).coerceAtLeast(0)
                        } else null
                        val timeLabel = if (remainingSec != null) {
                            val remMin = remainingSec / 60
                            val remSec = remainingSec % 60
                            val remFormatted = "%02d:%02d".format(remMin, remSec)
                            if (inProgress.isPaused) "PAUSED $formattedTime ($remFormatted left)" else "REC $formattedTime ($remFormatted left)"
                        } else {
                            if (inProgress.isPaused) "PAUSED $formattedTime" else "REC $formattedTime"
                        }

                        Text(
                            text = timeLabel,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily
                        )
                    }
                }
            } else {
                // Centered Robot Mascot & Brand Title matching reference image!
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(text = "🤖", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TelePrompt",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WiseDisplayFontFamily,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            // Right Group: Filters, Switch Camera, Journey Bell Notification & Recordings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Real-time Filters button
                IconButton(
                    onClick = { viewModel.toggleFilterPicker(!showFilterPicker) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .scaleOnPress(0.96f)
                        .background(
                            if (selectedFilter != VideoFilterType.ORIGINAL) StudioCyan.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.65f)
                        )
                        .testTag("toggle_filters_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterVintage,
                        contentDescription = "Real-time filters",
                        tint = if (selectedFilter != VideoFilterType.ORIGINAL) StudioCyan else Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Switch Lens (Front / Back)
                IconButton(
                    onClick = {
                        previewViewRef?.let { preview ->
                            viewModel.cameraManager.toggleCamera(lifecycleOwner, preview)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .scaleOnPress(0.96f)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .testTag("switch_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch front or rear camera",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Notification Bell with Red Badge Dot (matching reference image top-right notification bell!)
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = { showJourneyDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .scaleOnPress(0.96f)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .testTag("top_journey_map_badge")
                    ) {
                        Text(text = "🔔", fontSize = 16.sp)
                    }
                    // Unread Red Indicator Dot
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp, end = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                }

                // Gallery / Recorded Videos button
                IconButton(
                    onClick = onNavigateToRecordings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .scaleOnPress(0.96f)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .testTag("view_recordings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Saved Recordings",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Real-Time Filter Selector Bar (Floating below top bar)
        AnimatedVisibility(
            visible = showFilterPicker,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = statusBarPadding + 52.dp, start = 14.dp, end = 14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VideoFilter.ALL_FILTERS.forEach { filter ->
                        val isSelected = selectedFilter == filter.type
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) StudioCyan.copy(alpha = 0.3f) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) StudioCyan else Color.Transparent
                            ),
                            modifier = Modifier
                                .clickable { viewModel.selectFilter(filter.type) }
                                .testTag("camera_filter_chip_${filter.type.name}")
                        ) {
                            Text(
                                text = "${filter.type.emoji} ${filter.type.displayName}",
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Countdown Timer Overlay on Camera Preview (Short-Form Remaining Recording Time)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarPadding + if (showFilterPicker) 104.dp else 48.dp)
        ) {
            RecordingCountdownOverlay(
                recordingState = recordingState,
                selectedLimit = selectedPlatformLimit,
                onSelectLimit = { limit ->
                    viewModel.selectPlatformLimit(limit)
                    swipeHudMessage = "🎯 Target: ${limit.platformName} (${limit.title})"
                    showSwipeHud = true
                },
                onLimitReached = {
                    if (autoStopOnLimit) {
                        viewModel.cameraManager.stopRecording()
                        swipeHudMessage = "⏱️ ${selectedPlatformLimit.platformName} limit reached (${selectedPlatformLimit.title})! Recording saved."
                        showSwipeHud = true
                    }
                }
            )
        }

        // Floating Teleprompter Overlay positioned at upper area (Full Width)
        // (Positioned right below front camera for optimal eye contact!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = statusBarPadding + if (showFilterPicker) 152.dp else if (recordingState is StudioRecordingState.RecordingInProgress && selectedPlatformLimit.isLimited) 116.dp else 90.dp
                )
        ) {
            TeleprompterOverlay(
                config = config,
                scriptTitle = selectedScript?.title ?: "Freestyle Prompter",
                scriptText = selectedScript?.content ?: "Add or select a script below to begin reading while recording!",
                onConfigChange = { viewModel.updateTeleprompterConfig(it) },
                onOpenSettings = { viewModel.toggleSettingsSheet(true) }
            )
        }

        // Pre-roll Round Countdown Overlay (Circular Ring & 3, 2, 1 Action!)
        if (recordingState is StudioRecordingState.Countdown) {
            val countdown = (recordingState as StudioRecordingState.Countdown).secondsRemaining
            val countdownTotal = config.countdownDurationSec.takeIf { it > 0 } ?: 3
            val progressFraction = (countdown.toFloat() / countdownTotal.toFloat()).coerceIn(0.1f, 1f)

            val pulseTransition = rememberInfiniteTransition(label = "countdown_pulse")
            val pulseScale by pulseTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "countdown_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f))
                    .clickable { viewModel.cameraManager.cancelCountdown() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Outer Circular Ring with Glowing Stroke
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(190.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(3.5.dp, com.example.ui.theme.TorqueBlueAccent.copy(alpha = 0.4f), CircleShape)
                            .testTag("countdown_round_ring")
                    ) {
                        // Inner Accent Halo Ring
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .border(2.dp, com.example.ui.theme.TorqueBluePrimary, CircleShape)
                        )

                        // Countdown Number
                        Text(
                            text = "$countdown",
                            color = Color.White,
                            fontSize = 84.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = WiseDisplayFontFamily,
                            modifier = Modifier.testTag("countdown_number")
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Text(
                        text = "Get ready to look into lens...",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = WiseBodyFontFamily
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Recording starts automatically",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontFamily = WiseBodyFontFamily
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = { viewModel.cameraManager.cancelCountdown() },
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .scaleOnPress(0.96f)
                            .testTag("countdown_cancel_button")
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = WiseBodyFontFamily
                        )
                    }
                }
            }
        }

        // Finalizing Video Spinner Overlay
        if (recordingState is StudioRecordingState.Finalizing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Saving Take...",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Processing video to library",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Floating Makeup Swipe HUD (shows on swipe or toggle)
        AnimatedVisibility(
            visible = showSwipeHud,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Black.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (selectedFilter == VideoFilterType.MAKEUP) Color(0xFFFF2D75) else StudioCyan
                ),
                shadowElevation = 10.dp,
                modifier = Modifier.testTag("makeup_swipe_hud")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (selectedFilter == VideoFilterType.MAKEUP) "💄" else "✨",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = swipeHudMessage ?: "Makeup Filter",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (selectedFilter == VideoFilterType.MAKEUP) {
                        Spacer(modifier = Modifier.height(10.dp))
                        // Visual Intensity glowing progress track
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF334155))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(makeupIntensity)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFFF80AB), Color(0xFFFF2D75))
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⇄ Swipe ← / → to adjust glow",
                            color = Color(0xFFFF80AB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Bottom Studio Control Deck
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = navBarPadding + 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Swipe Feature guidance chip
            AnimatedVisibility(
                visible = selectedFilter == VideoFilterType.MAKEUP,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2D75).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("makeup_swipe_hint_pill")
                ) {
                    Text(
                        text = "⇄ Swipe screen to adjust Glow (${(makeupIntensity * 100).toInt()}%)",
                        color = Color(0xFFFF80AB),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Quick Script Selector Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Script Selector Pill (Document)
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color.Black.copy(alpha = 0.70f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier
                        .scaleOnPress(0.96f)
                        .clickable { viewModel.toggleScriptPicker(true) }
                        .testTag("select_script_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Script Document",
                            tint = ChationYellow,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedScript?.title ?: "Select or Write Script",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = WiseBodyFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 180.dp)
                        )
                    }
                }

                // Makeup Filter Pill right beside Document
                val isMakeupActive = selectedFilter == VideoFilterType.MAKEUP
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (isMakeupActive) Color(0xFFFF2D75).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.70f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isMakeupActive) Color(0xFFFF2D75) else StudioBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .scaleOnPress(0.96f)
                        .clickable {
                            val nowActive = viewModel.toggleMakeupFilter()
                            swipeHudMessage = if (nowActive) "💄 Makeup: ${(makeupIntensity * 100).toInt()}%" else "✨ Natural Mode"
                            showSwipeHud = true
                        }
                        .testTag("makeup_filter_by_document_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Makeup Filter",
                            tint = if (isMakeupActive) Color(0xFFFF2D75) else Color(0xFFFF80AB),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMakeupActive) "${(makeupIntensity * 100).toInt()}%" else "Glow",
                            color = if (isMakeupActive) Color.White else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Reference-Style Floating Dark Capsule Navigation & Record Bar
            val isRecording = recordingState is StudioRecordingState.RecordingInProgress
            val isPaused = (recordingState as? StudioRecordingState.RecordingInProgress)?.isPaused == true

            Surface(
                shape = RoundedCornerShape(36.dp),
                color = Color(0xFF111315),
                shadowElevation = 16.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22252E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Script Library Document Icon
                    IconButton(
                        onClick = onNavigateToScripts,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .scaleOnPress(0.95f)
                            .testTag("scripts_library_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "All Scripts Library",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 2. Makeup / Beauty Filter Icon
                    IconButton(
                        onClick = {
                            val nowActive = viewModel.toggleMakeupFilter()
                            swipeHudMessage = if (nowActive) "💄 Makeup: ${(makeupIntensity * 100).toInt()}%" else "✨ Natural Mode"
                            showSwipeHud = true
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .scaleOnPress(0.95f)
                            .background(
                                if (selectedFilter == VideoFilterType.MAKEUP) Color(0xFFFF2D75).copy(alpha = 0.3f) else Color.Transparent
                            )
                            .testTag("makeup_filter_dock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Makeup Filter",
                            tint = if (selectedFilter == VideoFilterType.MAKEUP) Color(0xFFFF2D75) else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 3. CENTER: Primary Record Shutter Button (Bright Yellow Circle matching reference image active tab)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(74.dp)
                            .scaleOnPress(0.94f)
                            .testTag("record_button_container")
                    ) {
                        // Outer Active Ring
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) StudioRecordRed else ChationYellow)
                                .clickable {
                                    if (isRecording) {
                                        viewModel.stopRecording()
                                    } else {
                                        viewModel.startRecording()
                                    }
                                }
                                .testTag("primary_record_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRecording) {
                                // Recording active: white square stop indicator
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                )
                            } else {
                                // Idle state: Red camera shutter dot inside yellow circle
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDC2626))
                                )
                            }
                        }
                    }

                    // 4. If recording: Pause/Resume. If idle: AI Animate Photo
                    if (isRecording) {
                        IconButton(
                            onClick = {
                                if (isPaused) {
                                    viewModel.resumeRecording()
                                } else {
                                    viewModel.pauseRecording()
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .scaleOnPress(0.95f)
                                .testTag("pause_resume_recording_button")
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (isPaused) "Resume take" else "Pause take",
                                tint = if (isPaused) ChationYellow else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showAnimatePhotoDialog = true },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .scaleOnPress(0.95f)
                                .testTag("animate_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = "Animate photo into video",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // 5. Studio Settings Sheet Button (Gear / Tune)
                    IconButton(
                        onClick = { viewModel.toggleSettingsSheet(true) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .scaleOnPress(0.95f)
                            .testTag("studio_settings_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Studio Settings",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Script Picker
    if (showScriptPicker) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleScriptPicker(false) },
            sheetState = sheetState,
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Script",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { showUploadDialog = true },
                            modifier = Modifier.testTag("upload_script_picker_button")
                        ) {
                            Text("📁 Upload", color = com.example.ui.theme.TorqueBluePrimary, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = {
                            viewModel.toggleScriptPicker(false)
                            onNavigateToScripts()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Script")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    items(allScripts) { script ->
                        val isSelected = selectedScript?.id == script.id
                        Card(
                            onClick = { viewModel.selectScript(script) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = script.title,
                                        color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${script.wordCount} words • ~${script.estimatedDurationSec}s estimated",
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Bottom Sheet: Full Script & Recording Settings Popup
    if (showSettingsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleSettingsSheet(false) },
            sheetState = sheetState,
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        ) {
            var settingsTab by remember { mutableIntStateOf(0) } // 0 = Script Settings, 1 = Recording & Video

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Header Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Script & Recording Settings",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseDisplayFontFamily
                        )
                        Text(
                            text = "Tap on prompter overlay opens these studio controls",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontFamily = WiseBodyFontFamily
                        )
                    }

                    IconButton(onClick = { viewModel.toggleSettingsSheet(false) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Expressive Segmented Tabs: Script Settings vs Recording Settings (Yellow Active Pill matching reference image)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFF1E2129))
                        .padding(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (settingsTab == 0) ChationYellow else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { settingsTab = 0 }
                            .testTag("tab_script_settings")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📜", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Script Reader",
                                color = if (settingsTab == 0) ChationDark else Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = WiseBodyFontFamily
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (settingsTab == 1) ChationYellow else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { settingsTab = 1 }
                            .testTag("tab_recording_settings")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎥", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Recording Limits",
                                color = if (settingsTab == 1) ChationDark else Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = WiseBodyFontFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (settingsTab == 0) {
                    // ==========================================
                    // TAB 0: SCRIPT READER SETTINGS
                    // ==========================================

                    // Quick Transport Controls (Instant Play/Pause & Mirror controls since user tapped text to open)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play / Pause Scroll
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = if (config.isAutoScrollActive) ChationYellow else Color(0xFF1E2129),
                            modifier = Modifier
                                .weight(1.3f)
                                .scaleOnPress(0.96f)
                                .clickable {
                                    viewModel.updateTeleprompterConfig(config.copy(isAutoScrollActive = !config.isAutoScrollActive))
                                }
                                .testTag("popup_play_pause_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (config.isAutoScrollActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (config.isAutoScrollActive) ChationDark else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (config.isAutoScrollActive) "Pause Scroll" else "Start Scroll",
                                    color = if (config.isAutoScrollActive) ChationDark else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = WiseBodyFontFamily
                                )
                            }
                        }

                        // Mirror Text Toggle
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = if (config.isMirrored) ChationYellow else Color(0xFF1E2129),
                            modifier = Modifier
                                .weight(1f)
                                .scaleOnPress(0.96f)
                                .clickable {
                                    viewModel.updateTeleprompterConfig(config.copy(isMirrored = !config.isMirrored))
                                }
                                .testTag("popup_mirror_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🪞", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (config.isMirrored) "Mirrored" else "Mirror",
                                    color = if (config.isMirrored) ChationDark else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = WiseBodyFontFamily
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    // ==========================================
                    // TAB 0: SCRIPT READER SETTINGS
                    // ==========================================

                    // Current Active Script Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current Script:",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontFamily = WiseBodyFontFamily
                                )
                                Text(
                                    text = selectedScript?.title ?: "Freestyle Prompter",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = WiseBodyFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${selectedScript?.wordCount ?: 0} words • ~${selectedScript?.estimatedDurationSec ?: 0}s estimated",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.toggleSettingsSheet(false)
                                    viewModel.toggleScriptPicker(true)
                                },
                                shape = RoundedCornerShape(9999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Switch Script", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scroll Speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-Scroll Speed",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${config.scrollSpeed.toInt()} px/sec",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = config.scrollSpeed,
                        onValueChange = { viewModel.updateTeleprompterConfig(config.copy(scrollSpeed = it)) },
                        valueRange = 10f..110f,
                        colors = SliderDefaults.colors(
                            thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            activeTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "🐢 Slow (25)" to 25f,
                            "⚡ Normal (45)" to 45f,
                            "🚀 Fast (75)" to 75f
                        ).forEach { (label, value) ->
                            val isSelected = kotlin.math.abs(config.scrollSpeed - value) < 6f
                            OutlinedButton(
                                onClick = { viewModel.updateTeleprompterConfig(config.copy(scrollSpeed = value)) },
                                shape = RoundedCornerShape(9999.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Font Size Slider & Value
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Font Size",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${config.fontSizeSp} sp",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = config.fontSizeSp.toFloat(),
                        onValueChange = { viewModel.updateTeleprompterConfig(config.copy(fontSizeSp = it.toInt())) },
                        valueRange = 16f..36f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            activeTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    )

                    // Text Alignment Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Text Alignment",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("LEFT" to "Left", "CENTER" to "Center", "RIGHT" to "Right").forEach { (alignKey, label) ->
                                val isSelected = config.textAlign == alignKey
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.updateTeleprompterConfig(config.copy(textAlign = alignKey))
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Overlay Opacity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overlay Opacity",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(config.overlayOpacity * 100).toInt()}%",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = config.overlayOpacity,
                        onValueChange = { viewModel.updateTeleprompterConfig(config.copy(overlayOpacity = it)) },
                        valueRange = 0.15f..0.95f,
                        colors = SliderDefaults.colors(
                            thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            activeTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Light (25%)" to 0.25f,
                            "Standard (48%)" to 0.48f,
                            "Solid (80%)" to 0.80f
                        ).forEach { (label, value) ->
                            val isSelected = kotlin.math.abs(config.overlayOpacity - value) < 0.08f
                            OutlinedButton(
                                onClick = { viewModel.updateTeleprompterConfig(config.copy(overlayOpacity = value)) },
                                shape = RoundedCornerShape(9999.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prompter Height Proportion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prompter Window Height",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.30f to "Compact", 0.45f to "Balanced", 0.60f to "Expanded").forEach { (hFrac, label) ->
                                val isSelected = kotlin.math.abs(config.heightFraction - hFrac) < 0.08f
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.updateTeleprompterConfig(config.copy(heightFraction = hFrac))
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mirror Mode toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mirror Mode", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Horizontal flip for teleprompter glass reflection", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.updateTeleprompterConfig(config.copy(isMirrored = !config.isMirrored)) },
                            shape = RoundedCornerShape(9999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (config.isMirrored) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (config.isMirrored) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(
                                text = if (config.isMirrored) "Enabled" else "Disabled",
                                color = if (config.isMirrored) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // TAB 1: RECORDING & STUDIO SETTINGS
                    // ==========================================

                    // Short-Form Content Platform Duration Limits
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Short-Form Platform Limit",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Overlay counts down remaining take time",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = selectedPlatformLimit.badgeLabel,
                                color = StudioCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ShortFormPlatformLimit.entries.forEach { limit ->
                                val isSelected = selectedPlatformLimit == limit
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.selectPlatformLimit(limit)
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                    ) {
                                        Text(limit.iconEmoji, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${limit.title} (${limit.platformName.substringBefore("/")})",
                                            color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedPlatformLimit.isLimited) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Auto-stop when limit reached",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                Button(
                                    onClick = { viewModel.toggleAutoStopOnLimit(!autoStopOnLimit) },
                                    shape = RoundedCornerShape(9999.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (autoStopOnLimit) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (autoStopOnLimit) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Text(
                                        text = if (autoStopOnLimit) "Auto-Stop ON" else "Manual Stop",
                                        color = if (autoStopOnLimit) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pre-roll Countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Countdown Before Recording",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0, 3, 5).forEach { sec ->
                                val isSelected = config.countdownDurationSec == sec
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.updateTeleprompterConfig(config.copy(countdownDurationSec = sec))
                                    }
                                ) {
                                    Text(
                                        text = if (sec == 0) "Off" else "${sec}s",
                                        color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Camera Lens Switch Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Camera Lens", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Switch front selfie prompter or rear camera", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                previewViewRef?.let { preview ->
                                    viewModel.cameraManager.toggleCamera(lifecycleOwner, preview)
                                }
                            },
                            shape = RoundedCornerShape(9999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Switch Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real-time Filters Shortcut
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Live Beauty Shaders", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Filter: ${selectedFilter.displayName}", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.toggleSettingsSheet(false)
                                viewModel.toggleFilterPicker(true)
                            },
                            shape = RoundedCornerShape(9999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow),
                            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text("Open Shaders", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account & Cloud Sync Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.displayName ?: "Guest Creator",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (!currentUser?.email.isNullOrBlank()) currentUser.email else (currentUser?.phoneNumber ?: "Local session"),
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.toggleSettingsSheet(false)
                                    if (currentUser != null) {
                                        showProfileDialog = true
                                    } else {
                                        onNavigateToAuth()
                                    }
                                },
                                shape = RoundedCornerShape(9999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Done Button
                Button(
                    onClick = { viewModel.toggleSettingsSheet(false) },
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Done", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Interactive Customer Journey Map Dialog
    if (showJourneyDialog) {
        CustomerJourneyDialog(
            onDismiss = { showJourneyDialog = false },
            onNavigateToStage = { stageId ->
                showJourneyDialog = false
                when (stageId) {
                    0 -> onReplayOnboarding()
                    1 -> onNavigateToScripts()
                    2 -> viewModel.toggleSettingsSheet(true)
                    3 -> { /* Already in live prompter */ }
                    4 -> {
                        lastSaved?.let { saved ->
                            onNavigateToEditor(saved.filePath, saved.title)
                        } ?: run {
                            onNavigateToRecordings()
                        }
                    }
                    5 -> onNavigateToRecordings()
                }
            }
        )
    }

    // User Profile Dialog
    if (showProfileDialog && currentUser != null && authManager != null) {
        com.example.ui.auth.UserProfileDialog(
            user = currentUser,
            onDismiss = { showProfileDialog = false },
            onSignOut = {
                authManager.signOut {
                    onNavigateToAuth()
                }
            },
            onNavigateToAuth = onNavigateToAuth
        )
    }

    // Saved Take Success Dialog
    if (showSavedSuccess && lastSaved != null) {
        val recording = lastSaved!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissSavedDialog() },
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = com.example.ui.theme.TorqueBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Take Recorded Successfully!",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = recording.title,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Duration: ${recording.formattedDuration} • Size: ${recording.formattedSize}",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Script: ${recording.scriptTitle}",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.dismissSavedDialog()
                            onNavigateToEditor(recording.filePath, recording.title)
                        },
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("dialog_edit_upload_button")
                    ) {
                        Text(
                            "✨ Edit & Direct Upload",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.dismissSavedDialog()
                            onPlayRecording(recording.filePath)
                        },
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text("Watch", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSavedDialog() }) {
                    Text("Done", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Upload Script Dialog (Local Storage & Google Drive)
    if (showUploadDialog) {
        UploadScriptDialog(
            viewModel = viewModel,
            onDismiss = { showUploadDialog = false },
            onScriptUploaded = { script ->
                viewModel.selectScript(script)
                viewModel.toggleScriptPicker(false)
            }
        )
    }

    // Animate Photo into Video Dialog (Gemini Omni 1.2 Flash via Interactions API)
    if (showAnimatePhotoDialog) {
        AnimatePhotoToVideoDialog(
            onDismiss = { showAnimatePhotoDialog = false },
            onVideoGenerated = { aspectRatio ->
                swipeHudMessage = "🎬 Video Ready: ${aspectRatio.label}"
                showSwipeHud = true
            }
        )
    }
}
