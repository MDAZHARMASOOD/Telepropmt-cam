package com.example.ui.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.ShortFormPlatformLimit
import com.example.camera.StudioRecordingState
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioRecordRed
import com.example.ui.theme.StudioRecordRedDark
import com.example.ui.theme.WiseBodyFontFamily
import com.example.ui.theme.WiseDisplayFontFamily
import com.example.ui.util.scaleOnPress

/**
 * Countdown timer overlay that displays remaining recording time on the camera preview
 * to ensure creators keep short-form videos within platform constraints (Shorts, Reels, TikTok).
 */
@Composable
fun RecordingCountdownOverlay(
    recordingState: StudioRecordingState,
    selectedLimit: ShortFormPlatformLimit,
    onSelectLimit: (ShortFormPlatformLimit) -> Unit,
    onLimitReached: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecording = recordingState is StudioRecordingState.RecordingInProgress
    val durationSeconds = (recordingState as? StudioRecordingState.RecordingInProgress)?.durationSeconds ?: 0L
    val isPaused = (recordingState as? StudioRecordingState.RecordingInProgress)?.isPaused == true
    var isExpanded by remember { mutableStateOf(false) }

    val hasLimit = selectedLimit.isLimited
    val totalLimitSec = selectedLimit.durationSeconds
    val remainingSeconds = if (hasLimit) {
        (totalLimitSec - durationSeconds).coerceAtLeast(0)
    } else {
        0L
    }

    // Trigger auto-stop when 0 seconds remain
    LaunchedEffect(isRecording, remainingSeconds, hasLimit) {
        if (isRecording && hasLimit && remainingSeconds == 0L) {
            onLimitReached()
        }
    }

    // Dynamic warning stages for short-form limit
    val isUrgent = hasLimit && remainingSeconds in 1..5
    val isWarning = hasLimit && remainingSeconds in 6..15
    val isExpired = hasLimit && remainingSeconds == 0L

    val infiniteTransition = rememberInfiniteTransition(label = "countdown_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isUrgent) 1.08f else if (isWarning) 1.04f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isUrgent) 300 else 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val targetAccentColor = when {
        isExpired -> Color(0xFFEF4444)
        isUrgent -> Color(0xFFFF2A4A)
        isWarning -> Color(0xFFF59E0B)
        else -> StudioCyan
    }

    val animatedColor by animateColorAsState(
        targetValue = targetAccentColor,
        animationSpec = tween(300),
        label = "countdown_accent_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recording_countdown_overlay"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. ACTIVE RECORDING: Full remaining countdown bar & badge on preview
        if (isRecording && hasLimit) {
            val progressFraction = (durationSeconds.toFloat() / totalLimitSec.toFloat()).coerceIn(0f, 1f)
            val remainingFormatted = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60)

            // Top Progress Bar directly hugging the camera preview top
            LinearProgressIndicator(
                progress = { progressFraction },
                color = animatedColor,
                trackColor = Color.Black.copy(alpha = 0.45f),
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .testTag("countdown_progress_bar")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Floating Countdown Pill with Remaining Time & Dynamic Platform Warning
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color.Black.copy(alpha = 0.82f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .scale(pulseScale)
                    .testTag("countdown_timer_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    // Circular Countdown Ring indicator
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(24.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f - progressFraction },
                            color = animatedColor,
                            trackColor = Color.White.copy(alpha = 0.15f),
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Icon(
                            imageVector = if (isUrgent || isWarning) Icons.Default.HourglassBottom else Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = animatedColor,
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isExpired) "00:00" else remainingFormatted,
                                color = if (isUrgent) Color(0xFFFF4D6D) else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = WiseDisplayFontFamily,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LEFT",
                                color = animatedColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = WiseBodyFontFamily
                            )
                        }

                        // Context message: Platform name + warning status
                        Text(
                            text = when {
                                isExpired -> "🛑 ${selectedLimit.platformName} limit reached!"
                                isUrgent -> "🚨 Wrap up take soon!"
                                isWarning -> "⚠️ 15s remaining (${selectedLimit.title} limit)"
                                else -> "${selectedLimit.iconEmoji} ${selectedLimit.platformName} (${selectedLimit.title})"
                            },
                            color = if (isUrgent || isWarning) animatedColor else Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = WiseBodyFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Small indicator badge
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = animatedColor.copy(alpha = 0.20f)
                    ) {
                        Text(
                            text = "${durationSeconds}s / ${totalLimitSec}s",
                            color = animatedColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        } else if (!isRecording) {
            // 2. IDLE STATE: Floating Platform Short-Form Target Selector on Preview
            // Starts as a compact icon button; tapping it expands into the full limits selector.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        (fadeIn(tween(220)) + expandHorizontally(tween(220)))
                            .togetherWith(fadeOut(tween(180)) + shrinkHorizontally(tween(180)))
                    },
                    label = "timer_limit_expansion"
                ) { expanded ->
                    if (!expanded) {
                        // COLLAPSED: Compact Timer Limit Icon Button on main screen
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .scaleOnPress(0.94f)
                                .clip(RoundedCornerShape(9999.dp))
                                .clickable { isExpanded = true }
                                .testTag("timer_limit_icon_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(StudioCyan.copy(alpha = 0.22f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Timer Limit",
                                        tint = StudioCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "${selectedLimit.iconEmoji} ${selectedLimit.title}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = WiseDisplayFontFamily
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand timer limits",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // EXPANDED: Full options row with auto-collapse on select and explicit close button
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = Color.Black.copy(alpha = 0.88f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyan.copy(alpha = 0.5f)),
                            shadowElevation = 8.dp,
                            modifier = Modifier.testTag("timer_limit_expanded_row")
                        ) {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Header / Collapse trigger
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .clickable { isExpanded = false }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Collapse timer limits",
                                        tint = StudioCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Limit:",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = WiseBodyFontFamily
                                    )
                                }

                                ShortFormPlatformLimit.entries.forEach { limit ->
                                    val isSelected = selectedLimit == limit
                                    Surface(
                                        shape = RoundedCornerShape(9999.dp),
                                        color = if (isSelected) StudioCyan else Color.White.copy(alpha = 0.12f),
                                        modifier = Modifier
                                            .scaleOnPress(0.94f)
                                            .clickable {
                                                onSelectLimit(limit)
                                                isExpanded = false
                                            }
                                            .testTag("select_limit_${limit.title}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = limit.iconEmoji,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = limit.title,
                                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.95f),
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                                fontFamily = WiseBodyFontFamily
                                            )
                                        }
                                    }
                                }

                                // Close / collapse button at the end
                                IconButton(
                                    onClick = { isExpanded = false },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .testTag("timer_limit_close_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close limits menu",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
