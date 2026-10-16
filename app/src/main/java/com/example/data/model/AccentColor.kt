package com.example.data.model

enum class AccentColor(val lightArgb: Long, val darkArgb: Long) {
    DEFAULT(0L, 0L),
    BLUE(0xFF0062CCL, 0xFF64A8FFL),
    GREEN(0xFF1E8E3EL, 0xFF5BD17AL),
    ORANGE(0xFFD9650BL, 0xFFFFA04DL),
    RED(0xFFD32F2FL, 0xFFFF7A7AL),
    PURPLE(0xFF7B3FD1L, 0xFFB68CFFL),
    TEAL(0xFF00838FL, 0xFF4DD0E1L),
    PINK(0xFFC2185BL, 0xFFF48FB1L);

    fun argbFor(isDark: Boolean): Long? {
        if (this == DEFAULT) return null
        return if (isDark) darkArgb else lightArgb
    }
}
