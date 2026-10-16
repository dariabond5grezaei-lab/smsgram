package com.example.data.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class DualSimInfo(
    val slotIndex: Int,
    val subscriptionId: Int,
    val displayName: String,
    val carrierName: String,
    val iccId: String = "",
    val phoneNumber: String = "",
    val customLabel: String = "",
    val customColorHex: String? = null
) {
    val displayTitle: String
        get() = when {
            customLabel.isNotBlank() -> customLabel
            displayName.isNotBlank() && !displayName.startsWith("سیم‌کارت") -> displayName
            carrierName.isNotBlank() && carrierName != "اپراتور" -> carrierName
            else -> "سیم‌کارت ${slotIndex + 1}"
        }

    fun getTitle(language: AppLanguage): String = when {
        customLabel.isNotBlank() -> customLabel
        displayName.isNotBlank() && !displayName.startsWith("سیم‌کارت") && !displayName.startsWith("SIM") -> displayName
        carrierName.isNotBlank() && carrierName != "اپراتور" && carrierName != "Carrier" -> carrierName
        else -> if (language == AppLanguage.ENGLISH) "SIM ${slotIndex + 1}" else "سیم‌کارت ${slotIndex + 1}"
    }

    val operatorType: OperatorType
        get() {
            val full = "$displayName $carrierName".lowercase()
            return when {
                full.contains("mci") || full.contains("hamrah") || full.contains("همراه") -> OperatorType.MCI
                full.contains("mtn") || full.contains("irancell") || full.contains("ایرانسل") -> OperatorType.IRANCELL
                full.contains("rightel") || full.contains("رایتل") -> OperatorType.RIGHTEL
                full.contains("shatel") || full.contains("شاتل") -> OperatorType.SHATEL
                full.contains("taliya") || full.contains("تالیا") -> OperatorType.TALIYA
                else -> OperatorType.GENERIC
            }
        }

    val carrierBrandColor: Color
        get() = if (customColorHex != null) {
            try {
                Color(android.graphics.Color.parseColor(customColorHex))
            } catch (e: Exception) {
                operatorType.primaryColor
            }
        } else {
            operatorType.primaryColor
        }

    @Composable
    fun getThemeHarmonizedColor(): Color {
        if (customColorHex != null) {
            try {
                return Color(android.graphics.Color.parseColor(customColorHex))
            } catch (e: Exception) {
                // Fallback below
            }
        }
        val themePrimary = MaterialTheme.colorScheme.primary
        val themeSecondary = MaterialTheme.colorScheme.secondary
        return if (slotIndex == 0) themePrimary else themeSecondary
    }
}

enum class OperatorType(
    val faName: String,
    val enName: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    MCI(
        faName = "همراه اول",
        enName = "MCI",
        primaryColor = Color(0xFF00A3E0),
        secondaryColor = Color(0xFFFF7900)
    ),
    IRANCELL(
        faName = "ایرانسل",
        enName = "Irancell MTN",
        primaryColor = Color(0xFFFFB300),
        secondaryColor = Color(0xFF222222)
    ),
    RIGHTEL(
        faName = "رایتل",
        enName = "Rightel",
        primaryColor = Color(0xFF931A6D),
        secondaryColor = Color(0xFF8E24AA)
    ),
    SHATEL(
        faName = "شاتل موبایل",
        enName = "Shatel Mobile",
        primaryColor = Color(0xFFE63027),
        secondaryColor = Color(0xFFD32F2F)
    ),
    TALIYA(
        faName = "تالیا",
        enName = "Taliya",
        primaryColor = Color(0xFF00897B),
        secondaryColor = Color(0xFF004D40)
    ),
    GENERIC(
        faName = "اپراتور همراه",
        enName = "Cellular",
        primaryColor = Color(0xFF007AFF),
        secondaryColor = Color(0xFF5856D6)
    );

    fun getName(language: AppLanguage): String = if (language == AppLanguage.ENGLISH) enName else faName
}
