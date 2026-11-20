package com.example

import com.example.data.BeverageType
import com.example.data.HydrationUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class HydrationModelsTest {

  @Test
  fun `beverage hydration factors compute effective milliliters accurately`() {
    assertEquals(250, BeverageType.WATER.computeEffectiveMl(250))
    assertEquals(288, BeverageType.ELECTROLYTES.computeEffectiveMl(250))
    assertEquals(200, BeverageType.COFFEE.computeEffectiveMl(250))
    assertEquals(238, BeverageType.HERBAL_TEA.computeEffectiveMl(250))
  }

  @Test
  fun `unknown enum strings fall back safely without throwing`() {
    assertEquals(BeverageType.WATER, BeverageType.fromRaw("UNKNOWN_DRINK"))
    assertEquals(BeverageType.WATER, BeverageType.fromRaw(null))
    assertEquals(HydrationUnit.ML, HydrationUnit.fromRaw("LITERS"))
    assertEquals(HydrationUnit.OZ, HydrationUnit.fromRaw("OZ"))
  }

  @Test
  fun `unit formatting supports mL and oz cleanly`() {
    assertEquals("2,500 mL", HydrationUnit.ML.formatVolume(2500))
    assertEquals("250 mL", HydrationUnit.ML.formatCompact(250))
    assertEquals("8.5 oz", HydrationUnit.OZ.formatCompact(250))
  }
}
