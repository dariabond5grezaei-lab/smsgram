package com.example.data.model

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import java.io.File

data class AppFont(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val description: String,
    val fontRes: Int? = null,
    val isCustom: Boolean = false
) {
    fun getName(language: AppLanguage): String {
        return if (language == AppLanguage.ENGLISH && nameEn.isNotBlank()) {
            nameEn
        } else {
            nameFa.substringBefore(" (")
        }
    }
    fun getFontFamily(context: Context): FontFamily {
        if (isCustom) {
            val customFile = File(context.filesDir, "custom_font.ttf")
            if (customFile.exists() && customFile.length() > 0) {
                return try {
                    FontFamily(Font(customFile, FontWeight.Normal, FontStyle.Normal))
                } catch (e: Exception) {
                    FontFamily.Default
                }
            }
            return FontFamily.Default
        }

        return if (fontRes != null) {
            try {
                FontFamily(
                    Font(fontRes, FontWeight.Normal, FontStyle.Normal),
                    Font(fontRes, FontWeight.Medium, FontStyle.Normal),
                    Font(fontRes, FontWeight.Bold, FontStyle.Normal),
                    Font(fontRes, FontWeight.SemiBold, FontStyle.Normal)
                )
            } catch (e: Exception) {
                try {
                    FontFamily(Font(fontRes))
                } catch (e2: Exception) {
                    FontFamily.Default
                }
            }
        } else {
            FontFamily.Default
        }
    }

    companion object {
        val ALL_FONTS = listOf(
            AppFont(
                id = "vazirmatn",
                nameFa = "وزیرمتن (Vazirmatn)",
                nameEn = "Vazirmatn",
                description = "فونت مدرن، استاندارد و پرکاربرد فارسی و لاتین",
                fontRes = R.font.vazirmatn
            ),
            AppFont(
                id = "noto_sans_arabic",
                nameFa = "نوتو سنس (Noto Sans Arabic)",
                nameEn = "Noto Sans Arabic",
                description = "فونت خوانا و بدون‌سریال استاندارد گوگل",
                fontRes = R.font.noto_sans_arabic
            ),
            AppFont(
                id = "noto_naskh_arabic",
                nameFa = "نوتو نسخ (Noto Naskh Arabic)",
                nameEn = "Noto Naskh Arabic",
                description = "خط اصیل و رسمی نسخ مناسب مطالعه",
                fontRes = R.font.noto_naskh_arabic
            ),
            AppFont(
                id = "cairo",
                nameFa = "قاهره (Cairo)",
                nameEn = "Cairo",
                description = "تایپ‌فیس مدرن، پرانرژی و هندسی",
                fontRes = R.font.cairo
            ),
            AppFont(
                id = "rubik",
                nameFa = "روبیک (Rubik)",
                nameEn = "Rubik",
                description = "فونت گوشه‌گرد، دوستانه و نرم",
                fontRes = R.font.rubik
            ),
            AppFont(
                id = "readex_pro",
                nameFa = "ریدکس پرو (Readex Pro)",
                nameEn = "Readex Pro",
                description = "فونت مینیمال و بهینه‌سازی‌شده برای صفحه نمایش",
                fontRes = R.font.readex_pro
            ),
            AppFont(
                id = "lalezar",
                nameFa = "لاله‌زار (Lalezar)",
                nameEn = "Lalezar",
                description = "فونت نوستالژیک و نمایشی با هویت ایرانی",
                fontRes = R.font.lalezar
            ),
            AppFont(
                id = "markazi_text",
                nameFa = "مرکزی (Markazi Text)",
                nameEn = "Markazi Text",
                description = "تایپ‌فیس مطبوعاتی، شیک و ادبی",
                fontRes = R.font.markazi_text
            ),
            AppFont(
                id = "amiri",
                nameFa = "امیری (Amiri)",
                nameEn = "Amiri",
                description = "خط کلاسیک و چشم‌نواز کتابی",
                fontRes = R.font.amiri
            ),
            AppFont(
                id = "almarai",
                nameFa = "المراعی (Almarai)",
                nameEn = "Almarai",
                description = "فونت شرکتی، اداری و مهندسی",
                fontRes = R.font.almarai
            ),
            AppFont(
                id = "changa",
                nameFa = "چانگا (Changa)",
                nameEn = "Changa",
                description = "تایپ‌فیس بولد و پیکسلی-هندسی",
                fontRes = R.font.changa
            ),
            AppFont(
                id = "system_default",
                nameFa = "پیش‌فرض سیستم (System Default)",
                nameEn = "System Default",
                description = "فونت استاندارد کارخانه‌ای دستگاه شما",
                fontRes = null
            ),
            AppFont(
                id = "custom",
                nameFa = "فونت بارگذاری‌شده شما (Custom Font)",
                nameEn = "Custom User Font",
                description = "فایل فونت اختصاصی TTF یا OTF شما",
                fontRes = null,
                isCustom = true
            )
        )

        fun getById(id: String): AppFont {
            return ALL_FONTS.find { it.id == id } ?: ALL_FONTS.first()
        }
    }
}
