package com.example.camera

/**
 * Short-form video duration limits for popular creator platforms
 * (YouTube Shorts, Instagram Reels, TikTok, Facebook Reels, Stories).
 */
enum class ShortFormPlatformLimit(
    val title: String,
    val durationSeconds: Int,
    val platformName: String,
    val badgeLabel: String,
    val iconEmoji: String
) {
    TIKTOK_QUICK(
        title = "15s",
        durationSeconds = 15,
        platformName = "TikTok / Stories",
        badgeLabel = "15s Quick Take",
        iconEmoji = "⚡"
    ),
    REELS_STANDARD(
        title = "30s",
        durationSeconds = 30,
        platformName = "Reels / TikTok",
        badgeLabel = "30s Standard",
        iconEmoji = "✨"
    ),
    SHORTS_60(
        title = "60s",
        durationSeconds = 60,
        platformName = "YouTube Shorts / Reels",
        badgeLabel = "60s Shorts & Reels",
        iconEmoji = "▶️"
    ),
    REELS_90(
        title = "90s",
        durationSeconds = 90,
        platformName = "Instagram Reels Max",
        badgeLabel = "90s Max Reel",
        iconEmoji = "📸"
    ),
    SHORTS_3M(
        title = "3m",
        durationSeconds = 180,
        platformName = "Extended Shorts / TikTok",
        badgeLabel = "3m Deep Dive",
        iconEmoji = "🎬"
    ),
    UNLIMITED(
        title = "Off",
        durationSeconds = 0,
        platformName = "No Limit (Count Up)",
        badgeLabel = "Unlimited Duration",
        iconEmoji = "♾️"
    );

    val isLimited: Boolean get() = durationSeconds > 0
}
