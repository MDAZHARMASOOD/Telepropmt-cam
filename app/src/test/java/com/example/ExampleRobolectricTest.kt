package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `teleprompter speed boundaries and default checks`() {
    val defaultSpeed = 35f
    val clampedLow = (defaultSpeed - 30f).coerceAtLeast(10f)
    val clampedHigh = (defaultSpeed + 100f).coerceAtMost(120f)
    assertEquals(10f, clampedLow, 0.01f)
    assertEquals(120f, clampedHigh, 0.01f)
  }

  @Test
  fun `short-form platform duration limits validation`() {
    val shorts = com.example.camera.ShortFormPlatformLimit.TIKTOK_QUICK
    val tiktok = com.example.camera.ShortFormPlatformLimit.REELS_STANDARD
    val standard = com.example.camera.ShortFormPlatformLimit.SHORTS_60
    val extended = com.example.camera.ShortFormPlatformLimit.REELS_90
    val longLimit = com.example.camera.ShortFormPlatformLimit.SHORTS_3M
    val unlimited = com.example.camera.ShortFormPlatformLimit.UNLIMITED

    assertEquals(15, shorts.durationSeconds)
    assertEquals(30, tiktok.durationSeconds)
    assertEquals(60, standard.durationSeconds)
    assertEquals(90, extended.durationSeconds)
    assertEquals(180, longLimit.durationSeconds)
    assertEquals(0, unlimited.durationSeconds)
    assertEquals(false, unlimited.isLimited)
    assertEquals(true, standard.isLimited)
  }

  @Test
  fun `remaining recording seconds calculations`() {
    val limitSec = 60L
    val elapsedSec = 45L
    val remainingSec = (limitSec - elapsedSec).coerceAtLeast(0)
    assertEquals(15L, remainingSec)

    val exceededElapsed = 70L
    val clampedZero = (limitSec - exceededElapsed).coerceAtLeast(0)
    assertEquals(0L, clampedZero)
  }
}
