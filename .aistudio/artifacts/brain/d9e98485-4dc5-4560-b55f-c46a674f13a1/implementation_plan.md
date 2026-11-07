# Walkthrough Design Inspired by Reference Mockup

Transform the walkthrough flow (`OnboardingScreen`) into a vibrant, high-energy **Electric Neon Cyber Grid** experience, implementing the distinct compositional dynamics from the user's reference image across 3 custom-crafted feature slides, paired with a full-width pill action deck and interactive swipe navigation.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following key design directions were confirmed during Phase 1 clarification:
> - **Visual Composition**: Dynamic layout changing per slide to match each specific feature:
>   - **Slide 1 (Natural Eye Contact & Prompter)**: Floating creator bubble avatars surrounding a central 3D camera element, with framing hands/lens guidance.
>   - **Slide 2 (Interactive Makeup & Real-Time Shaders)**: Cyberpunk dual-angle tilted preview cards with glowing neon glasses, electric purple/cyan HUD rims, and holographic beauty shaders.
>   - **Slide 3 (Direct Social Reels Export)**: Orbital constellation layout with floating video take thumbnails circling a central 3D fiery propulsion heart/rocket badge.
> - **Color Palette & Atmosphere**: Electric neon cyber tones on a dark perspective grid canvas (`#0A0E1A` deep space navy, `#00E5FF` electric cyan, `#E040FB` electric neon magenta/purple, glowing grid lines).
> - **Navigation & Actions**: Full-width pill action button at the bottom of the card with press scaling, integrated with a circular action arrow badge and animated page indicator pills.

---

## 1. Overview & Core Concept

- **What It Does**:
  - Replaces the uniform card walkthrough with three uniquely composed visual hero stages directly inspired by the user's reference mockup:
    1. **Stage 1 (Eye Contact & Framing)**: Central 3D Glaze Camera framed by floating creator portrait pills, illustrating real-time gaze capture.
    2. **Stage 2 (Cyber Glam & Makeup Shaders)**: Layered perspective cards with neon cyberpunk overlays, glowing HUD goggles, and real-time shader switches.
    3. **Stage 3 (Viral Reels & Social Export)**: Satellite orbital cards showcasing exported reels orbiting a central glowing 3D asset with instant export tags.
  - Features a custom **perspective cyber grid canvas** drawn dynamically via Canvas (`Modifier.drawBehind`), subtle ambient neon flares, bold high-contrast display typography, and smooth gesture swiping.
- **Target Persona**: Modern short-form video creators, influencers, and presenters looking for an inspiring, cutting-edge recording studio tool.

---

## 2. User Experience & Visual Design

### Neon Cyber Grid Color Tokens

| Token Role | Hex Color | Application |
|---|---|---|
| `cyberBg` | `#080C16` | Deep obsidian space background |
| `cyberGrid` | `#1E293B` | Subtle perspective grid coordinate lines (`0.18f` opacity) |
| `neonCyan` | `#00E5FF` | Primary electric accent, lens rings, active indicator dot |
| `neonMagenta` | `#E040FB` | Secondary electric flare, shader accents, glowing badges |
| `neonPurple` | `#7C4DFF` | Deep glow halos and card border gradients |
| `cyberCardSurface` | `#111827` | Semi-translucent glassmorphic card fill (`0.85f` alpha) |
| `textWhite` | `#FFFFFF` | Heavy display headline (Weight 900) |
| `textSecondary` | `#94A3B8` | Clean body copy and subtitles |

### Slide-by-Slide Composition Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                 Top Navigation & Skip Button                │
└──────────────────────────────┬──────────────────────────────┘
                               │
            ┌──────────────────┼──────────────────┐
            ▼                  ▼                  ▼
┌───────────────────────┐┌───────────────────┐┌───────────────────────┐
│ Slide 1: Eye Contact  ││ Slide 2: Makeup   ││ Slide 3: Export       │
│ - Floating circular   ││ - 2 angled tilted ││ - Orbital ring cards  │
│   creator bubbles     ││   cards with neon ││   circling central    │
│ - Center 3D Camera    ││   cyber glasses   ││   3D rocket / heart   │
│ - Focus framing guide ││ - Shader sliders  ││ - Multi-platform tags │
└───────────┬───────────┘└─────────┬─────────┘└───────────┬───────────┘
            │                      │                      │
            └──────────────────────┼──────────────────────┘
                                   │
┌──────────────────────────────────┴──────────────────────────┐
│ Bold Headline: "LOOK INTO THE LENS / GLAM SHADERS / GO LIVE"│
│ Subtitle & Highlights Description                           │
│ Animated Pill Page Indicators & Full-Width Gradient CTA     │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dynamic Per-Slide Canvas Layouts vs. Static Single Template**
  - *Chosen Approach*: Implement a modular `when (pageIndex)` layout renderer within `HorizontalPager`. Slide 1 renders a radial bubble layout; Slide 2 renders perspective angled viewport cards; Slide 3 renders an orbital satellite arrangement.
  - *Why*: Directly honors the user's choice and the visual variety seen across the 3 screens in the user's uploaded image.

- **Decision 2: Electric Cyber Grid Atmosphere**
  - *Chosen Approach*: Use a dark perspective grid background with neon cyan and magenta radial lighting, providing high contrast and visual punch while seamlessly leading into the teleprompter studio.

---

## 4. Implementation Steps

1. **Create Cyber Grid Background & Canvas Drawing**:
   - Implement `Modifier.drawBehind` drawing subtle perspective grid lines and neon ambient radial flares in `OnboardingScreen.kt`.

2. **Build the 3 Distinct Hero Compositions**:
   - **Slide 1 Hero (Floating Bubbles & Central 3D Camera)**:
     - Central Glaze 3D camera with glowing neon cyan ring.
     - 4 floating creator avatar pills positioned around the center with subtle offset coordinates.
   - **Slide 2 Hero (Angled Cyber Viewport Cards)**:
     - Two overlapping angled cards with `graphicsLayer { rotationZ = -6f }` and `graphicsLayer { rotationZ = 6f }`, neon rim borders, and cyber glitz badges.
   - **Slide 3 Hero (Orbital Social Constellation)**:
     - Central 3D Glaze Rocket / Heart asset with satellite cards orbiting around it with social reel badges (Instagram Reels, YouTube Shorts).

3. **Style Typography & Navigation Deck**:
   - Bold uppercase headline in `WiseDisplayFontFamily` (Weight 900).
   - High-contrast body copy and pill badges.
   - Full-width pill action button with an electric gradient (`Brush.horizontalGradient(listOf(neonCyan, neonMagenta))`), circular arrow icon, and animated indicator dots.

4. **Build & Verify**:
   - Execute `compile_applet` and verify Gradle build succeeds.
   - Run `gradle :app:testDebugUnitTest` to guarantee all unit tests pass with zero regressions.
