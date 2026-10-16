package com.example.data.model

enum class ThemeFamily(val lightMode: ThemeMode?, val darkMode: ThemeMode?) {
    CLASSIC(ThemeMode.DAY, ThemeMode.DARK_SLATE),
    AMOLED(ThemeMode.AMOLED_LIGHT, ThemeMode.AMOLED_BLACK),
    CYPRUS(ThemeMode.CYPRUS_SAND_LIGHT, ThemeMode.CYPRUS_SAND_DARK),
    BRIDAL(ThemeMode.BRIDAL_SKIN_LIGHT, ThemeMode.BRIDAL_SKIN_DARK),
    PUMPKIN(ThemeMode.PUMPKIN_CHARCOAL_LIGHT, ThemeMode.PUMPKIN_CHARCOAL_DARK),
    OCEAN(ThemeMode.OCEAN_CLOUDY_LIGHT, ThemeMode.OCEAN_CLOUDY_DARK),
    OLIVE(ThemeMode.OLIVE_SAGE_LIGHT, ThemeMode.OLIVE_SAGE_DARK),
    DYNAMIC(ThemeMode.DYNAMIC, ThemeMode.DYNAMIC);

    companion object {
        fun familyOf(mode: ThemeMode): ThemeFamily {
            return when (mode) {
                ThemeMode.DAY, ThemeMode.DARK_SLATE -> CLASSIC
                ThemeMode.AMOLED_LIGHT, ThemeMode.AMOLED_BLACK -> AMOLED
                ThemeMode.CYPRUS_SAND_LIGHT, ThemeMode.CYPRUS_SAND_DARK -> CYPRUS
                ThemeMode.BRIDAL_SKIN_LIGHT, ThemeMode.BRIDAL_SKIN_DARK -> BRIDAL
                ThemeMode.PUMPKIN_CHARCOAL_LIGHT, ThemeMode.PUMPKIN_CHARCOAL_DARK -> PUMPKIN
                ThemeMode.OCEAN_CLOUDY_LIGHT, ThemeMode.OCEAN_CLOUDY_DARK -> OCEAN
                ThemeMode.OLIVE_SAGE_LIGHT, ThemeMode.OLIVE_SAGE_DARK -> OLIVE
                ThemeMode.DYNAMIC -> DYNAMIC
            }
        }
    }
}
