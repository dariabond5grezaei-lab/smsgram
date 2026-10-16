package com.example

import com.example.data.model.AccentColor
import com.example.data.model.ThemeFamily
import com.example.data.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeFamilyTest {

    @Test
    fun testExactlyEightFamilies() {
        val families = ThemeFamily.values()
        assertEquals(8, families.size)
    }

    @Test
    fun testEveryThemeModeMapsToOneFamilyViaFamilyOf() {
        ThemeMode.values().forEach { mode ->
            val family = ThemeFamily.familyOf(mode)
            assertNotNull(family)
        }
    }

    @Test
    fun testAccentColorDefaultArgbForIsNull() {
        assertNull(AccentColor.DEFAULT.argbFor(isDark = false))
        assertNull(AccentColor.DEFAULT.argbFor(isDark = true))
    }

    @Test
    fun testOtherAccentsHaveAlpha0xFFAndDifferBetweenLightAndDark() {
        AccentColor.values().filter { it != AccentColor.DEFAULT }.forEach { accent ->
            val lightArgb = accent.argbFor(isDark = false)
            val darkArgb = accent.argbFor(isDark = true)
            assertNotNull(lightArgb)
            assertNotNull(darkArgb)
            assertTrue(lightArgb != darkArgb)
            assertEquals(0xFF000000L, lightArgb!! and 0xFF000000L)
            assertEquals(0xFF000000L, darkArgb!! and 0xFF000000L)
        }
    }
}
