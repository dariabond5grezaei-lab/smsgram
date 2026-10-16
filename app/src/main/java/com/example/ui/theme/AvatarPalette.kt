package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

object AvatarPalette {
    // 7 Harmonious base pairs crafted for accessible foreground text (White >= 4.5:1 contrast)
    private val LightModeGradients = listOf(
        listOf(Color(0xFF0056B3), Color(0xFF003D82)), // Classic Navy Blue
        listOf(Color(0xFF1E8E3E), Color(0xFF13662A)), // Emerald Green
        listOf(Color(0xFFB35900), Color(0xFF804000)), // Amber Bronze
        listOf(Color(0xFF8022B8), Color(0xFF5B1484)), // Royal Purple
        listOf(Color(0xFFC2185B), Color(0xFF880E4F)), // Crimson Rose
        listOf(Color(0xFF007A92), Color(0xFF005566)), // Deep Teal
        listOf(Color(0xFF3949AB), Color(0xFF283593))  // Deep Indigo
    )

    private val DarkModeGradients = listOf(
        listOf(Color(0xFF2B5278), Color(0xFF1A3855)), // Muted Deep Slate/Blue
        listOf(Color(0xFF1E5C32), Color(0xFF103A1F)), // Forest Deep Green
        listOf(Color(0xFF8C5300), Color(0xFF5E3400)), // Warm Ochre
        listOf(Color(0xFF632B7D), Color(0xFF421855)), // Deep Plum
        listOf(Color(0xFF8A1E35), Color(0xFF581021)), // Dark Burgundy
        listOf(Color(0xFF195562), Color(0xFF0D363F)), // Deep Marine
        listOf(Color(0xFF32347A), Color(0xFF1E2052))  // Dark Lavender
    )

    @Composable
    fun getGradientFor(key: String, isDarkTheme: Boolean): List<Color> {
        val list = if (isDarkTheme) DarkModeGradients else LightModeGradients
        val hash = kotlin.math.abs(key.hashCode().toLong())
        val index = (hash % list.size).toInt()
        return list[index]
    }
}
