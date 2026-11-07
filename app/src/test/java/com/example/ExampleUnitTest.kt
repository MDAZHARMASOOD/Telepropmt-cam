package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.regex.Pattern

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun script_word_count_calculation() {
    val text = "Stop scrolling if you want to double your video watch time right now!"
    val words = text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
    assertEquals(13, words)
    val durationSec = ((words / 130f) * 60).toInt().coerceAtLeast(1)
    assertTrue(durationSec >= 1)
  }

  @Test
  fun google_doc_and_drive_link_parsing() {
    val sampleDocUrl = "https://docs.google.com/document/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit"
    val docPattern = Pattern.compile("docs\\.google\\.com/document/d/([a-zA-Z0-9_-]+)")
    val matcher = docPattern.matcher(sampleDocUrl)
    assertTrue(matcher.find())
    assertEquals("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms", matcher.group(1))

    val driveUrl = "https://drive.google.com/file/d/1a2b3c4d5e6f/view?usp=sharing"
    val drivePattern = Pattern.compile("drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)")
    val driveMatcher = drivePattern.matcher(driveUrl)
    assertTrue(driveMatcher.find())
    assertEquals("1a2b3c4d5e6f", driveMatcher.group(1))
  }

  @Test
  fun teleprompter_48_percent_opacity_and_cycling_logic() {
    val defaultOpacity = 0.48f
    assertEquals(0.48f, defaultOpacity, 0.001f)
    assertEquals(48, (defaultOpacity * 100).toInt())

    // Verify opacity cycle logic: 25% -> 48% -> 75% -> 25%
    val nextFromLow = if (0.25f < 0.35f) 0.48f else 0.75f
    assertEquals(0.48f, nextFromLow, 0.001f)

    val nextFromDefault = if (0.48f < 0.35f) 0.48f else if (0.48f < 0.60f) 0.75f else 0.25f
    assertEquals(0.75f, nextFromDefault, 0.001f)
  }

  @Test
  fun makeup_filter_swipe_drag_intensity_clamping() {
    val initialIntensity = 0.85f
    val dragRightAmount = 140f
    val increased = (initialIntensity + dragRightAmount / 350f).coerceIn(0.15f, 1.0f)
    assertEquals(1.0f, increased, 0.001f)

    val dragLeftAmount = -280f
    val decreased = (initialIntensity + dragLeftAmount / 350f).coerceIn(0.15f, 1.0f)
    assertEquals(0.15f, decreased, 0.001f)
  }

  @Test
  fun phone_and_email_validation_logic() {
    val validEmail = "creator@studio.com"
    assertTrue(validEmail.contains("@") && validEmail.contains("."))

    val validPhone = "+15552345678"
    assertTrue(validPhone.length >= 8)

    val cleanPhoneUid = "phone_" + validPhone.replace("[^0-9]".toRegex(), "")
    assertEquals("phone_15552345678", cleanPhoneUid)
  }

  @Test
  fun short_form_platform_limit_remaining_time_calculation() {
    val shorts60 = com.example.camera.ShortFormPlatformLimit.SHORTS_60
    assertEquals(60, shorts60.durationSeconds)
    assertTrue(shorts60.isLimited)

    // When 42s have elapsed:
    val elapsed42 = 42L
    val remaining18 = (shorts60.durationSeconds - elapsed42).coerceAtLeast(0)
    assertEquals(18L, remaining18)

    // Formatting: 18s -> "00:18"
    val formatted = "%02d:%02d".format(remaining18 / 60, remaining18 % 60)
    assertEquals("00:18", formatted)

    // When elapsed exceeds limit:
    val elapsed75 = 75L
    val remaining0 = (shorts60.durationSeconds - elapsed75).coerceAtLeast(0)
    assertEquals(0L, remaining0)

    // Unlimited mode check:
    val unlimited = com.example.camera.ShortFormPlatformLimit.UNLIMITED
    assertEquals(0, unlimited.durationSeconds)
    org.junit.Assert.assertFalse(unlimited.isLimited)
  }
}
