package com.example.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.WiseBodyFontFamily
import com.example.ui.theme.WiseDisplayFontFamily
import com.example.ui.util.scaleOnPress
import kotlinx.coroutines.launch

// Electric Neon Cyber Palette
private val CyberBg = Color(0xFF090D1A)
private val CyberGridColor = Color(0xFF1E293B)
private val NeonCyan = Color(0xFF00E5FF)
private val NeonMagenta = Color(0xFFE040FB)
private val NeonPurple = Color(0xFF7C4DFF)
private val CyberCardBg = Color(0xFF101726)

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val glazeDrawableResId: Int,
    val badgeText: String,
    val highlights: List<String>
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "LOOK INTO THE LENS",
                subtitle = "Natural Eye Contact Studio",
                description = "Keep your gaze locked directly into the front camera. Translucent prompter floats your script right beneath the lens with adjustable speed and mirror mode.",
                glazeDrawableResId = R.drawable.img_glaze_camera_1793986790903,
                badgeText = "CAMERA & GAZE FOCUS",
                highlights = listOf("Front-lens eye line", "Smart scroll tap", "Mirror mode glass")
            ),
            OnboardingSlide(
                title = "CYBER GLAM SHADERS",
                subtitle = "Interactive Makeup Swipe",
                description = "Swipe horizontally across the live camera to glide into radiant beauty filters. Fine-tune glow from subtle glam to full vibrance with Noir, Sepia, and High-Contrast.",
                glazeDrawableResId = R.drawable.img_glaze_sparkle_1793986809684,
                badgeText = "💄 LIVE MAKEUP SHADERS",
                highlights = listOf("Horizontal viewfinder swipe", "15% - 100% beauty glow", "Real-time shader matrix")
            ),
            OnboardingSlide(
                title = "EXPORT REELS & SHORTS",
                subtitle = "Viral Creator Constellation",
                description = "Import scripts directly from Google Docs & local files. Edit takes inside the video studio and export seamlessly to Instagram Reels, YouTube Shorts, and Facebook Reels.",
                glazeDrawableResId = R.drawable.img_glaze_rocket_1793986824239,
                badgeText = "VIRAL SOCIAL SATELLITE",
                highlights = listOf("1-tap Google Drive import", "Direct Instagram share", "Lossless local backup")
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastSlide = pagerState.currentPage == slides.size - 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            // Cyber perspective grid background drawing
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw horizontal perspective grid lines
                val gridSpacing = 44.dp.toPx()
                var y = 0f
                while (y < canvasHeight) {
                    drawLine(
                        color = CyberGridColor.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += gridSpacing
                }

                // Draw vertical grid lines
                var x = 0f
                while (x < canvasWidth) {
                    drawLine(
                        color = CyberGridColor.copy(alpha = 0.25f),
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += gridSpacing
                }
            }
            .testTag("onboarding_screen")
    ) {
        // Ambient Neon Glow Halos
        Box(
            modifier = Modifier
                .size(460.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.30f),
                            NeonCyan.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = statusBarPadding + 14.dp,
                    bottom = navBarPadding + 20.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: App Branding & Skip action
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
                            .background(NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TelePrompt Studio",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = WiseDisplayFontFamily,
                        letterSpacing = 0.5.sp
                    )
                }

                if (!isLastSlide) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = CyberCardBg.copy(alpha = 0.7f)
                    ) {
                        TextButton(
                            onClick = onFinishOnboarding,
                            modifier = Modifier.testTag("onboarding_skip_button")
                        ) {
                            Text(
                                text = "Skip",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = WiseBodyFontFamily
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Swipeable HorizontalPager Container with Dynamic Layout per Slide
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("onboarding_swipeable_pager")
            ) { pageIndex ->
                val slide = slides[pageIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // DYNAMIC HERO STAGE: changes layout per slide to match reference mockup!
                    when (pageIndex) {
                        0 -> Slide1FloatingBubblesHero(slide = slide)
                        1 -> Slide2AngledCyberViewportsHero(slide = slide)
                        else -> Slide3OrbitalSatelliteHero(slide = slide)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Content Card: Title, Badge, Description & Highlights (frameless without borders)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = CyberCardBg.copy(alpha = 0.90f),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_slide_card_$pageIndex")
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Badge pill (borderless)
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = NeonCyan.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = slide.badgeText,
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = WiseBodyFontFamily,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Heavy Display Headline
                            Text(
                                text = slide.title,
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = WiseDisplayFontFamily,
                                textAlign = TextAlign.Center,
                                letterSpacing = (-0.5).sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = slide.subtitle,
                                color = NeonMagenta,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = WiseBodyFontFamily,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = slide.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontFamily = WiseBodyFontFamily,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Highlights
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                slide.highlights.forEach { highlight ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(NeonCyan)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = highlight,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 11.sp,
                                            fontFamily = WiseBodyFontFamily,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Navigation: Indicators & Full-Width Pill Button with Arrow Icon
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    slides.indices.forEach { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 30.dp else 8.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(
                                    if (isSelected) NeonCyan else Color.White.copy(alpha = 0.25f)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                // Full-width gradient pill button with right arrow icon
                Button(
                    onClick = {
                        if (isLastSlide) {
                            onFinishOnboarding()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(NeonCyan, NeonPurple, NeonMagenta)
                            ),
                            shape = RoundedCornerShape(9999.dp)
                        )
                        .scaleOnPress(0.96f)
                        .testTag("onboarding_primary_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = if (isLastSlide) "Get Started & Sign In" else "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = WiseBodyFontFamily,
                            color = Color.White
                        )

                        // Circular white arrow button matching reference mockup style!
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// SLIDE 1 HERO: Floating creator avatar bubbles around a central 3D camera
// Inspired by Left Screen of user reference mockup!
// =========================================================================
@Composable
private fun Slide1FloatingBubblesHero(slide: OnboardingSlide) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // Glowing Background Ring (borderless soft glow)
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.15f))
        )

        // Central Glaze 3D Camera Box (borderless)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(CyberCardBg)
        ) {
            Image(
                painter = painterResource(id = slide.glazeDrawableResId),
                contentDescription = "Glaze 3D Camera",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // Floating Real Creator Portrait 1 (Top Left)
        CreatorAvatarBubble(
            imageResId = R.drawable.img_creator_female_neon_1793991207502,
            tag = "Reel 4K",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = 10.dp)
        )

        // Floating Real Creator Portrait 2 (Top Right)
        CreatorAvatarBubble(
            imageResId = R.drawable.img_creator_male_cyber_1793991224978,
            tag = "60 FPS",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 6.dp)
        )

        // Floating Real Creator Portrait 3 (Bottom Left)
        CreatorAvatarBubble(
            imageResId = R.drawable.img_creator_woman_studio_1793991240252,
            tag = "Vlog",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 14.dp, y = (-10).dp)
        )

        // Floating Real Creator Portrait 4 (Bottom Right)
        CreatorAvatarBubble(
            imageResId = R.drawable.img_creator_man_presenter_1793991256745,
            tag = "Keynote",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-14).dp, y = (-12).dp)
        )
    }
}

// =========================================================================
// SLIDE 2 HERO: Angled Cyber Viewport Cards with Glow Rims & Creator Photos
// Inspired by Center Screen of user reference mockup!
// =========================================================================
@Composable
private fun Slide2AngledCyberViewportsHero(slide: OnboardingSlide) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // Tilted Background Card with real male creator photo (Left tilt -8 degrees)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E1B4B),
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(width = 165.dp, height = 215.dp)
                .offset(x = (-44).dp, y = (-8).dp)
                .rotate(-8f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_creator_male_cyber_1793991224978),
                    contentDescription = "Male Creator Recording",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark bottom vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC090D1A)),
                                startY = 180f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✨ 60 FPS NOIR",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WiseBodyFontFamily
                    )
                }
            }
        }

        // Tilted Foreground Card with real female cyber creator photo (Right tilt +6 degrees)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CyberCardBg,
            shadowElevation = 12.dp,
            modifier = Modifier
                .size(width = 175.dp, height = 225.dp)
                .offset(x = 38.dp, y = 14.dp)
                .rotate(6f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_creator_female_neon_1793991207502),
                    contentDescription = "Female Cyber Creator",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark bottom vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xDD101726)),
                                startY = 200f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = NeonMagenta.copy(alpha = 0.90f),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "💄 Real-Time Cyber Glam",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// SLIDE 3 HERO: Orbital Satellite Constellation with Social Badges
// Inspired by Right Screen of user reference mockup!
// =========================================================================
@Composable
private fun Slide3OrbitalSatelliteHero(slide: OnboardingSlide) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // Orbital Halo Circle (borderless soft background ring)
        Box(
            modifier = Modifier
                .size(210.dp)
                .clip(CircleShape)
                .background(NeonPurple.copy(alpha = 0.12f))
        )

        // Central 3D Glaze Rocket Box (borderless)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(136.dp)
                .clip(CircleShape)
                .background(CyberCardBg)
        ) {
            Image(
                painter = painterResource(id = slide.glazeDrawableResId),
                contentDescription = "Glaze 3D Rocket",
                modifier = Modifier
                    .size(126.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // Satellite Badge 1 (Top Center: Instagram Reels)
        SocialSatellitePill(
            iconText = "📸",
            label = "Reels",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 4.dp)
        )

        // Satellite Badge 2 (Right Center: YouTube Shorts)
        SocialSatellitePill(
            iconText = "▶️",
            label = "Shorts",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 6.dp)
        )

        // Satellite Badge 3 (Bottom Center: Facebook Video)
        SocialSatellitePill(
            iconText = "🌐",
            label = "FB Reels",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-6).dp)
        )

        // Satellite Badge 4 (Left Center: Google Drive)
        SocialSatellitePill(
            iconText = "📁",
            label = "Drive Sync",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-6).dp)
        )

        // Creator Satellite 1 (Top Left)
        CreatorAvatarBubble(
            imageResId = R.drawable.img_creator_woman_studio_1793991240252,
            tag = "Vlogger",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = 14.dp)
        )

        // Creator Satellite 2 (Bottom Right)
        CreatorAvatarBubble(
            imageResId = R.drawable.img_creator_man_presenter_1793991256745,
            tag = "Pro Reel",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp, y = (-12).dp)
        )
    }
}

// Micro-component: Floating Creator Avatar Bubble with real photo (borderless)
@Composable
private fun CreatorAvatarBubble(
    imageResId: Int,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CyberCardBg.copy(alpha = 0.92f),
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = tag,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tag,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = WiseBodyFontFamily
            )
        }
    }
}

// Micro-component: Satellite Social Export Pill (borderless)
@Composable
private fun SocialSatellitePill(
    iconText: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = CyberCardBg,
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        ) {
            Text(iconText, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = WiseBodyFontFamily
            )
        }
    }
}
