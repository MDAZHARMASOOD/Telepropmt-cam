package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// MATERIAL 3 — CHATION YELLOW & CHARCOAL PALETTE
// Inspired by modern creator & AI assistant visual language
// ==========================================

// Primary Brand: Vibrant Butter Yellow / Golden Amber (Hero accent, buttons, active tabs)
val ChationYellow = Color(0xFFFFD026)          // Dominant hero yellow from reference
val ChationYellowActive = Color(0xFFFFDF00)    // Pure electric yellow
val ChationYellowDeep = Color(0xFFF59E0B)      // Amber gold for accents
val ChationYellowContainer = Color(0xFFFEF9C3) // Warm soft cream-yellow container
val ChationOnYellowContainer = Color(0xFF422006) // Deep warm brown for yellow containers

// Dark Charcoal & Jet Black (Top bar, bottom bar, hero cards, dark circular badges)
val ChationDark = Color(0xFF111315)            // Primary deep charcoal
val ChationDarkSurface = Color(0xFF181A20)     // Dark card surface
val ChationDarkSurfaceElevated = Color(0xFF22252D)
val ChationDarkBorder = Color(0xFF2D313A)

// Soft Accents (Lavender / Pink from hero CTA button)
val ChationLavender = Color(0xFFF5D0FE)        // Soft pastel pink/lavender from reference
val ChationOnLavender = Color(0xFF581C87)       // Deep purple on lavender
val ChationVerified = Color(0xFF10B981)        // Green verified badge from avatar

// Light Surface Neutrals (Crisp white cards on soft gray-tinted background)
val ChationLightBg = Color(0xFFF4F5F9)         // Airy modern background
val ChationWhite = Color(0xFFFFFFFF)           // Crisp white card surfaces
val ChationCardBg = Color(0xFFFFFFFF)
val ChationCardBorder = Color(0xFFE5E7EB)
val ChationTextPrimary = Color(0xFF111315)     // High contrast dark text
val ChationTextSecondary = Color(0xFF6B7280)   // Subtle slate text
val ChationTextMuted = Color(0xFF9CA3AF)

// Backwards compatibility mappings for existing codebase references
val TorqueBluePrimary = ChationYellow
val TorqueBlueOnPrimary = ChationDark
val TorqueBlueContainer = ChationYellowContainer
val TorqueBlueOnContainer = ChationOnYellowContainer
val TorqueBlueAccent = ChationYellowActive
val TorqueBlueAccentMuted = ChationYellowDeep
val TorqueBlueSecondary = ChationDark
val TorqueBlueOnSecondary = ChationWhite
val TorqueBlueSecondaryContainer = Color(0xFF22252D)
val TorqueBlueOnSecondaryContainer = ChationWhite
val TorqueBlueTertiary = ChationLavender
val TorqueBlueOnTertiary = ChationOnLavender
val TorqueBlueTertiaryContainer = ChationLavender
val TorqueBlueOnTertiaryContainer = ChationOnLavender

val TorqueLightSurface = ChationLightBg
val TorqueLightSurfaceContainerLowest = ChationWhite
val TorqueLightSurfaceContainerLow = Color(0xFFF9FAFB)
val TorqueLightSurfaceContainer = ChationWhite
val TorqueLightSurfaceContainerHigh = Color(0xFFF3F4F6)
val TorqueLightSurfaceContainerHighest = Color(0xFFE5E7EB)

val TorqueLightOnSurface = ChationTextPrimary
val TorqueLightSurfaceVariant = Color(0xFFF3F4F6)
val TorqueLightOnSurfaceVariant = ChationTextSecondary
val TorqueLightOutline = Color(0xFFD1D5DB)
val TorqueLightOutlineVariant = Color(0xFFE5E7EB)

val StudioLightBg = ChationLightBg
val StudioLightCardBg = ChationWhite
val StudioLightBorder = ChationCardBorder

val FantaOrange = ChationYellow
val FantaOrangeActive = ChationYellowActive
val FantaOrangePale = ChationYellowContainer
val FantaOrangeContainer = ChationYellowContainer
val OnFantaOrange = ChationDark

val FantaGreen = ChationYellow
val FantaGreenActive = ChationYellowActive
val FantaGreenPale = ChationYellowContainer
val FantaGreenDeep = ChationDark
val FantaGreenContainer = ChationYellowContainer
val OnFantaGreen = ChationDark

// Studio Dark Viewfinder Overlays
val StudioDarkBg = Color(0xFF0D0E11)
val StudioCardBg = Color(0xFF181A20)
val StudioSurfaceVariant = Color(0xFF242730)
val StudioBorder = Color(0xFF2D313A)

val StudioIndigo = ChationYellow
val StudioIndigoLight = ChationYellowActive
val StudioCyan = ChationYellow
val StudioRecordRed = Color(0xFFEF4444)
val StudioRecordRedDark = Color(0xFFDC2626)
val StudioSuccess = ChationVerified
val StudioWarning = ChationYellowDeep

val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

val TeleprompterBg = Color(0xFF000000)
val TeleprompterHighlight = Color(0x33FFD026)
