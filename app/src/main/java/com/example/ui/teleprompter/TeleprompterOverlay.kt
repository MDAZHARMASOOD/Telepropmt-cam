package com.example.ui.teleprompter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teleprompter.TeleprompterConfig
import com.example.ui.theme.ChationYellow
import com.example.ui.theme.WiseDisplayFontFamily
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Full-width, two-line Teleprompter Overlay.
 * Features an edge-to-edge deep black background infused with a radiant yellow sunrise gradient.
 * Strictly displays two lines of text to keep the creator's eye line locked on the camera lens.
 * Tapping anywhere opens the studio settings popup!
 */
@Composable
fun TeleprompterOverlay(
    config: TeleprompterConfig,
    scriptTitle: String,
    scriptText: String,
    onConfigChange: (TeleprompterConfig) -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Smooth frame-based auto-scroll loop
    LaunchedEffect(config.isAutoScrollActive, config.scrollSpeed) {
        if (config.isAutoScrollActive) {
            var lastNanos = System.nanoTime()
            var accumulated = 0f
            while (isActive && config.isAutoScrollActive) {
                withFrameNanos { nowNanos ->
                    val deltaSec = (nowNanos - lastNanos) / 1_000_000_000f
                    lastNanos = nowNanos
                    accumulated += config.scrollSpeed * deltaSec
                    val pixels = accumulated.toInt()
                    if (pixels > 0) {
                        accumulated -= pixels
                        scope.launch {
                            scrollState.scrollBy(pixels.toFloat())
                        }
                    }
                }
            }
        }
    }

    // Precise height calculation to display exactly two lines of text:
    // (2 * lineHeight) + vertical breathing room
    val lineHeightMultiplier = 1.45f
    val lineHeightDp = config.fontSizeSp * lineHeightMultiplier
    val verticalPaddingDp = 18f
    val twoLinesWindowHeight = (lineHeightDp * 2f + verticalPaddingDp).dp

    val baseAlpha = config.overlayOpacity.coerceIn(0.65f, 0.98f)

    // Full-Width Container with Black Background + Yellow Sunrise Gradient
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(twoLinesWindowHeight)
            // Layer 1: Solid black backdrop
            .background(Color.Black.copy(alpha = baseAlpha))
            // Layer 2: Yellow Sunrise Gradient (night black fading into glowing sunrise gold & yellow)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.94f),
                        Color(0xFF0F0B01).copy(alpha = 0.90f),
                        Color(0xFF261901).copy(alpha = 0.78f),
                        Color(0xFF6B4700).copy(alpha = 0.55f),
                        ChationYellow.copy(alpha = 0.32f)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping anywhere on the full-width overlay opens studio controls!
                onOpenSettings?.invoke()
            }
            .testTag("teleprompter_overlay_box")
    ) {
        // Subtle Reading Focus Guide in center
        if (config.showGuideLine) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(lineHeightDp.dp)
                    .background(ChationYellow.copy(alpha = 0.07f))
            )
        }

        // Two-Line Scrolling Text Content Area (Inset horizontally for reading comfort)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 9.dp)
                .graphicsLayer {
                    // Mirror text horizontally if beam-splitter glass mode enabled
                    scaleX = if (config.isMirrored) -1f else 1f
                }
        ) {
            Text(
                text = scriptText.ifBlank { "Tap anywhere to select a script or adjust prompter speed & recording settings." },
                color = Color.White,
                fontSize = config.fontSizeSp.sp,
                lineHeight = (config.fontSizeSp * lineHeightMultiplier).sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = WiseDisplayFontFamily,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        blurRadius = 6f
                    )
                ),
                textAlign = when (config.textAlign) {
                    "LEFT" -> TextAlign.Left
                    "RIGHT" -> TextAlign.Right
                    else -> TextAlign.Center
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teleprompter_text_content")
            )

            // Lead-out spacer to allow scrolling completely past the final lines
            Spacer(modifier = Modifier.height(twoLinesWindowHeight * 2))
        }

        // Top subtle soft edge fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.90f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Sunrise Ambient Glow along the bottom horizon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ChationYellow.copy(alpha = 0.22f)
                        )
                    )
                )
        )

        // Glowing Sunrise Horizon Edge Beam along the very bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ChationYellow.copy(alpha = 0.45f),
                            ChationYellow,
                            Color(0xFFFFEA75),
                            ChationYellow,
                            ChationYellow.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
