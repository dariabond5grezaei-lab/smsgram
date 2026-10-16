package com.example.data.model

enum class ThemeMode(
    val titleFa: String,
    val titleEn: String,
    val isDark: Boolean,
    val paletteNameFa: String,
    val paletteNameEn: String
) {
    // 1. Classic Telegram Styles
    DAY(
        titleFa = "روز کلاسیک (روشن)",
        titleEn = "Classic Day (Light)",
        isDark = false,
        paletteNameFa = "تلگرامی کلاسیک",
        paletteNameEn = "Classic Telegram"
    ),
    DARK_SLATE(
        titleFa = "تیره کلاسیک (تلگرامی)",
        titleEn = "Dark Slate (Telegram)",
        isDark = true,
        paletteNameFa = "تلگرامی کلاسیک",
        paletteNameEn = "Classic Telegram"
    ),
    AMOLED_BLACK(
        titleFa = "مشکی خالص (AMOLED)",
        titleEn = "Pure AMOLED Black",
        isDark = true,
        paletteNameFa = "مشکی خالص",
        paletteNameEn = "AMOLED Black"
    ),
    AMOLED_LIGHT(
        titleFa = "مشکی خالص (روشن)",
        titleEn = "Pure AMOLED (Light)",
        isDark = false,
        paletteNameFa = "مشکی خالص",
        paletteNameEn = "AMOLED Black"
    ),

    // 2. Cyprus & Sand (#004741 & #F0EDE4)
    CYPRUS_SAND_LIGHT(
        titleFa = "شنی و قبرسی (روشن)",
        titleEn = "Sand & Cyprus (Light)",
        isDark = false,
        paletteNameFa = "قبرسی و شنی (Cyprus & Sand)",
        paletteNameEn = "Cyprus & Sand"
    ),
    CYPRUS_SAND_DARK(
        titleFa = "قبرسی و شنی (تیره)",
        titleEn = "Cyprus & Sand (Dark)",
        isDark = true,
        paletteNameFa = "قبرسی و شنی (Cyprus & Sand)",
        paletteNameEn = "Cyprus & Sand"
    ),

    // 3. Bridal & Skin Tone (#741A2F & #FFC6A8)
    BRIDAL_SKIN_LIGHT(
        titleFa = "هلویی و شرابی (روشن)",
        titleEn = "Skin Tone & Bridal (Light)",
        isDark = false,
        paletteNameFa = "شرابی و هلویی (Bridal & Skin)",
        paletteNameEn = "Bridal & Skin Tone"
    ),
    BRIDAL_SKIN_DARK(
        titleFa = "شرابی و هلویی (تیره)",
        titleEn = "Bridal & Skin Tone (Dark)",
        isDark = true,
        paletteNameFa = "شرابی و هلویی (Bridal & Skin)",
        paletteNameEn = "Bridal & Skin Tone"
    ),

    // 4. Pumpkin & Charcoal (#FD802E & #233D4C)
    PUMPKIN_CHARCOAL_LIGHT(
        titleFa = "کدو تنبل و زغالی (روشن)",
        titleEn = "Pumpkin & Charcoal (Light)",
        isDark = false,
        paletteNameFa = "کدو تنبل و زغالی (Pumpkin & Charcoal)",
        paletteNameEn = "Pumpkin & Charcoal"
    ),
    PUMPKIN_CHARCOAL_DARK(
        titleFa = "زغالی و کدو تنبل (تیره)",
        titleEn = "Charcoal & Pumpkin (Dark)",
        isDark = true,
        paletteNameFa = "کدو تنبل و زغالی (Pumpkin & Charcoal)",
        paletteNameEn = "Pumpkin & Charcoal"
    ),

    // 5. Ocean Blue & Cloudy Sky (#2872A1 & #CBDDE9)
    OCEAN_CLOUDY_LIGHT(
        titleFa = "ابری و اقیانوسی (روشن)",
        titleEn = "Cloudy Sky & Ocean (Light)",
        isDark = false,
        paletteNameFa = "اقیانوسی و ابری (Ocean & Cloud)",
        paletteNameEn = "Ocean & Cloudy Sky"
    ),
    OCEAN_CLOUDY_DARK(
        titleFa = "اقیانوسی و ابری (تیره)",
        titleEn = "Ocean & Cloudy Sky (Dark)",
        isDark = true,
        paletteNameFa = "اقیانوسی و ابری (Ocean & Cloud)",
        paletteNameEn = "Ocean & Cloudy Sky"
    ),

    // 6. Deep Olive & Soft Sage (#1A2517 & #ACC8A2)
    OLIVE_SAGE_LIGHT(
        titleFa = "مریم‌گلی و زیتونی (روشن)",
        titleEn = "Soft Sage & Olive (Light)",
        isDark = false,
        paletteNameFa = "زیتونی و مریم‌گلی (Olive & Sage)",
        paletteNameEn = "Olive & Soft Sage"
    ),
    OLIVE_SAGE_DARK(
        titleFa = "زیتونی و مریم‌گلی (تیره)",
        titleEn = "Deep Olive & Sage (Dark)",
        isDark = true,
        paletteNameFa = "زیتونی و مریم‌گلی (Olive & Sage)",
        paletteNameEn = "Olive & Soft Sage"
    ),

    // 7. Dynamic Material You
    DYNAMIC(
        titleFa = "رنگ پویای سیستم (Android 12+)",
        titleEn = "Dynamic Colors (Android 12+)",
        isDark = false,
        paletteNameFa = "پویای سیستم",
        paletteNameEn = "System Dynamic"
    );

    fun getDisplayName(language: AppLanguage): String {
        return if (language == AppLanguage.PERSIAN) titleFa else titleEn
    }

    fun getPaletteName(language: AppLanguage): String {
        return if (language == AppLanguage.PERSIAN) paletteNameFa else paletteNameEn
    }

    fun variantFor(dark: Boolean): ThemeMode {
        return when (this) {
            DAY, DARK_SLATE -> if (dark) DARK_SLATE else DAY
            AMOLED_LIGHT, AMOLED_BLACK -> if (dark) AMOLED_BLACK else AMOLED_LIGHT
            CYPRUS_SAND_LIGHT, CYPRUS_SAND_DARK -> if (dark) CYPRUS_SAND_DARK else CYPRUS_SAND_LIGHT
            BRIDAL_SKIN_LIGHT, BRIDAL_SKIN_DARK -> if (dark) BRIDAL_SKIN_DARK else BRIDAL_SKIN_LIGHT
            PUMPKIN_CHARCOAL_LIGHT, PUMPKIN_CHARCOAL_DARK -> if (dark) PUMPKIN_CHARCOAL_DARK else PUMPKIN_CHARCOAL_LIGHT
            OCEAN_CLOUDY_LIGHT, OCEAN_CLOUDY_DARK -> if (dark) OCEAN_CLOUDY_DARK else OCEAN_CLOUDY_LIGHT
            OLIVE_SAGE_LIGHT, OLIVE_SAGE_DARK -> if (dark) OLIVE_SAGE_DARK else OLIVE_SAGE_LIGHT
            DYNAMIC -> DYNAMIC
        }
    }

    fun getToggleTarget(): ThemeMode {
        return when (this) {
            DAY -> DARK_SLATE
            DARK_SLATE -> DAY
            AMOLED_LIGHT -> AMOLED_BLACK
            AMOLED_BLACK -> AMOLED_LIGHT
            CYPRUS_SAND_LIGHT -> CYPRUS_SAND_DARK
            CYPRUS_SAND_DARK -> CYPRUS_SAND_LIGHT
            BRIDAL_SKIN_LIGHT -> BRIDAL_SKIN_DARK
            BRIDAL_SKIN_DARK -> BRIDAL_SKIN_LIGHT
            PUMPKIN_CHARCOAL_LIGHT -> PUMPKIN_CHARCOAL_DARK
            PUMPKIN_CHARCOAL_DARK -> PUMPKIN_CHARCOAL_LIGHT
            OCEAN_CLOUDY_LIGHT -> OCEAN_CLOUDY_DARK
            OCEAN_CLOUDY_DARK -> OCEAN_CLOUDY_LIGHT
            OLIVE_SAGE_LIGHT -> OLIVE_SAGE_DARK
            OLIVE_SAGE_DARK -> OLIVE_SAGE_LIGHT
            DYNAMIC -> DARK_SLATE
        }
    }
}

