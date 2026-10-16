package com.example

import com.example.data.model.ThemeFamily
import com.example.data.model.ThemeMode
import com.example.domain.theme.ThemeResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeResolverTest {

    @Test
    fun testResolvePaletteIsDarkMatchesForNonDynamicFamilies() {
        ThemeFamily.values().filter { it != ThemeFamily.DYNAMIC }.forEach { family ->
            val lightMode = ThemeResolver.resolvePalette(family, dark = false)
            val darkMode = ThemeResolver.resolvePalette(family, dark = true)
            assertNotNull(lightMode)
            assertNotNull(darkMode)
            assertEquals(false, lightMode!!.isDark)
            assertEquals(true, darkMode!!.isDark)
        }
    }

    @Test
    fun testAmoledResolution() {
        val amoledLight = ThemeResolver.resolvePalette(ThemeFamily.AMOLED, dark = false)
        val amoledDark = ThemeResolver.resolvePalette(ThemeFamily.AMOLED, dark = true)
        assertEquals(ThemeMode.AMOLED_LIGHT, amoledLight)
        assertEquals(ThemeMode.AMOLED_BLACK, amoledDark)
    }

    @Test
    fun testDynamicResolution() {
        val dynamicLight = ThemeResolver.resolvePalette(ThemeFamily.DYNAMIC, dark = false)
        val dynamicDark = ThemeResolver.resolvePalette(ThemeFamily.DYNAMIC, dark = true)
        assertEquals(ThemeMode.DYNAMIC, dynamicLight)
        assertEquals(ThemeMode.DYNAMIC, dynamicDark)
    }
}
