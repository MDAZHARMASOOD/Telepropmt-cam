package com.example.ui.journey

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TorqueBluePrimary
import com.example.ui.theme.TorqueBlueContainer
import com.example.ui.theme.TorqueBlueOnContainer
import com.example.ui.theme.TorqueBlueAccent
import com.example.ui.util.scaleOnPress

data class JourneyStage(
    val id: Int,
    val number: String,
    val stageName: String,
    val shortName: String,
    val icon: ImageVector,
    val emotionEmoji: String,
    val emotionRating: String,
    val touchpoint: String,
    val userAction: String,
    val frictionResolved: String,
    val valueDelivered: String,
    val actionLabel: String
)

val CREATOR_JOURNEY_STAGES = listOf(
    JourneyStage(
        id = 0,
        number = "01",
        stageName = "Discovery & Onboarding",
        shortName = "1. Onboarding",
        icon = Icons.Default.Videocam,
        emotionEmoji = "😊",
        emotionRating = "9/10 • Excited",
        touchpoint = "Interactive 3-Slide Carousel",
        userAction = "Opens app, reviews camera prompter capabilities, grants Camera & Audio permissions in 1 tap.",
        frictionResolved = "Unclear permission requests and overwhelming camera menus.",
        valueDelivered = "Immediate confidence that teleprompter and camera work harmoniously out-of-the-box.",
        actionLabel = "Replay Onboarding Guide"
    ),
    JourneyStage(
        id = 1,
        number = "02",
        stageName = "Script Creation & Import",
        shortName = "2. Scripts & Drive",
        icon = Icons.Default.Description,
        emotionEmoji = "💡",
        emotionRating = "8.5/10 • Focused",
        touchpoint = "Script Library & Google Drive Importer",
        userAction = "Pastes script, imports from Google Drive share link, or picks local .txt/.md files with live word count.",
        frictionResolved = "Manually retyping scripts or copying across notes apps.",
        valueDelivered = "Automated reading time estimates (~130 words/min) and instant script activation.",
        actionLabel = "Open Script Library"
    ),
    JourneyStage(
        id = 2,
        number = "03",
        stageName = "Studio & Mirror Rig Setup",
        shortName = "3. Mirror Rig",
        icon = Icons.Default.Flip,
        emotionEmoji = "🎯",
        emotionRating = "9/10 • Calibrated",
        touchpoint = "Teleprompter Settings Sheet",
        userAction = "Toggles Mirror Mode for beam-splitter prompter glass; configures overlay opacity & scroll speed.",
        frictionResolved = "Backward mirrored text on professional glass rigs requiring inverted tablets.",
        valueDelivered = "Pro-grade beam splitter glass support right from an Android device.",
        actionLabel = "Configure Studio Rig"
    ),
    JourneyStage(
        id = 3,
        number = "04",
        stageName = "Live Recording & Teleprompter",
        shortName = "4. Live Prompter",
        icon = Icons.Default.Speed,
        emotionEmoji = "🔥",
        emotionRating = "10/10 • Flow State",
        touchpoint = "Full Camera Studio Deck",
        userAction = "Hits Record with 3s countdown; reads scrolling text positioned directly beside camera lens for natural eye contact.",
        frictionResolved = "Looking away from the lens to read notes; losing train of thought midway through take.",
        valueDelivered = "Seamless one-take video delivery with zero off-camera eye darting.",
        actionLabel = "Launch Studio Prompter"
    ),
    JourneyStage(
        id = 4,
        number = "05",
        stageName = "Post-Take Review & Aesthetics",
        shortName = "5. Video Polish",
        icon = Icons.Default.Movie,
        emotionEmoji = "🤩",
        emotionRating = "9.5/10 • Delighted",
        touchpoint = "In-App Video Editor",
        userAction = "Reviews recorded take directly on-device; previews color filters (Sepia, B&W Noir, High-Contrast Vivid).",
        frictionResolved = "Having to leave the camera app and import video into expensive desktop editors.",
        valueDelivered = "Immediate video playback review, trim/split inspection, and real-time color matrix shader application.",
        actionLabel = "Review Recent Takes"
    ),
    JourneyStage(
        id = 5,
        number = "06",
        stageName = "Viral Export & Distribution",
        shortName = "6. Social Export",
        icon = Icons.Default.Share,
        emotionEmoji = "🚀",
        emotionRating = "10/10 • Accomplished",
        touchpoint = "Direct Social Sharesheet",
        userAction = "Exports video take directly to Instagram Reels, YouTube Shorts, and Facebook Reels in 1 tap.",
        frictionResolved = "Manual video downloads, file searching, and app-switching delays.",
        valueDelivered = "Instant Android direct-share intent targets for Instagram Reels & YouTube Shorts with cloud backup.",
        actionLabel = "Explore Export Channels"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerJourneyDialog(
    onDismiss: () -> Unit,
    onNavigateToStage: (stageId: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedStageIndex by remember { mutableIntStateOf(0) }
    val currentStage = CREATOR_JOURNEY_STAGES[selectedStageIndex]

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
                .testTag("customer_journey_sheet")
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Creator Journey Architecture",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .scaleOnPress(0.96f)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "End-to-end user experience mapping covering the 6 stages of short-form video creation.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Stage Tabs Selector (M3 Expressive Pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CREATOR_JOURNEY_STAGES.forEachIndexed { index, stage ->
                    val isSelected = selectedStageIndex == index
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .scaleOnPress(0.96f)
                            .clickable { selectedStageIndex = index }
                            .testTag("journey_stage_tab_$index")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = stage.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stage.shortName,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Detailed Stage Card in M3 Surface
            AnimatedContent(
                targetState = currentStage,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "stage_content"
            ) { stage ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Card Header with Stage Number & Emotion Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = stage.number,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = stage.stageName,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Touchpoint: ${stage.touchpoint}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Emotion Pill
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = stage.emotionEmoji, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stage.emotionRating,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Action section (16.dp inner container)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "CREATOR ACTION",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stage.userAction,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Friction & Value Grid (Concentric boxes)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Friction Resolved Box
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🛡️", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "FRICTION RESOLVED",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stage.frictionResolved,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // Value Delivered Box
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "✨", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "VALUE DELIVERED",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stage.valueDelivered,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stage Jump Action Button (M3 Primary Pill Button)
                        Button(
                            onClick = { onNavigateToStage(stage.id) },
                            shape = RoundedCornerShape(9999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .scaleOnPress(0.96f)
                                .testTag("jump_to_stage_${stage.id}_button")
                        ) {
                            Text(
                                text = "Take Action: ${stage.actionLabel}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
