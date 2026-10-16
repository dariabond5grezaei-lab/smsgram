package com.example

import com.example.data.model.AppFont
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testAllFontsRegistrationAndIds() {
        val fonts = AppFont.ALL_FONTS
        assertTrue(fonts.isNotEmpty())

        val expectedIds = listOf(
            "vazirmatn", "noto_sans_arabic", "noto_naskh_arabic",
            "cairo", "rubik", "readex_pro", "lalezar",
            "markazi_text", "amiri", "almarai", "changa",
            "system_default", "custom"
        )

        for (id in expectedIds) {
            val font = AppFont.getById(id)
            assertEquals(id, font.id)
            if (id != "system_default" && id != "custom") {
                assertNotNull(font.fontRes)
                assertTrue((font.fontRes ?: 0) != 0)
            }
        }
    }
}
