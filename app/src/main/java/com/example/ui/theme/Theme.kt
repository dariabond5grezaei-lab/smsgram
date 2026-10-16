package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import com.example.data.model.ThemeMode

// Custom Message Bubble Colors Provider
data class BubbleColors(
    val incomingBackground: Color,
    val incomingText: Color,
    val outgoingBackground: Color,
    val outgoingText: Color,
    val timeTextColor: Color,
    val tickColor: Color
)

val LocalAppFontFamily = staticCompositionLocalOf<androidx.compose.ui.text.font.FontFamily> {
    androidx.compose.ui.text.font.FontFamily.Default
}

val LocalBubbleColors = staticCompositionLocalOf {
    BubbleColors(
        incomingBackground = SlateIncomingBubble,
        incomingText = SlateOnSurface,
        outgoingBackground = SlateOutgoingBubble,
        outgoingText = SlateOnPrimary,
        timeTextColor = SlateOnSurfaceVariant,
        tickColor = SlatePrimary
    )
}

private val DayColorScheme = lightColorScheme(
    primary = DayPrimary,
    onPrimary = DayOnPrimary,
    primaryContainer = DayPrimaryContainer,
    onPrimaryContainer = DayOnPrimaryContainer,
    background = DayBackground,
    onBackground = DayOnBackground,
    surface = DaySurface,
    onSurface = DayOnSurface,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = DayOnSurfaceVariant
)

private val SlateColorScheme = darkColorScheme(
    primary = SlatePrimary,
    onPrimary = SlateOnPrimary,
    primaryContainer = SlatePrimaryContainer,
    onPrimaryContainer = SlateOnPrimaryContainer,
    background = SlateBackground,
    onBackground = SlateOnBackground,
    surface = SlateSurface,
    onSurface = SlateOnSurface,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = SlateOnSurfaceVariant
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    onPrimary = AmoledOnPrimary,
    primaryContainer = AmoledPrimaryContainer,
    onPrimaryContainer = AmoledOnPrimaryContainer,
    background = AmoledBackground,
    onBackground = AmoledOnBackground,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledOnSurfaceVariant
)

private val AmoledLightColorScheme = lightColorScheme(
    primary = AmoledLightPrimary,
    onPrimary = AmoledLightOnPrimary,
    primaryContainer = AmoledLightPrimaryContainer,
    onPrimaryContainer = AmoledLightOnPrimaryContainer,
    background = AmoledLightBackground,
    onBackground = AmoledLightOnBackground,
    surface = AmoledLightSurface,
    onSurface = AmoledLightOnSurface,
    surfaceVariant = AmoledLightSurfaceVariant,
    onSurfaceVariant = AmoledLightOnSurfaceVariant
)

// 1. Cyprus & Sand
private val CyprusSandLightColorScheme = lightColorScheme(
    primary = CyprusSandLightPrimary,
    onPrimary = CyprusSandLightOnPrimary,
    primaryContainer = CyprusSandLightPrimaryContainer,
    onPrimaryContainer = CyprusSandLightOnPrimaryContainer,
    background = CyprusSandLightBackground,
    onBackground = CyprusSandLightOnBackground,
    surface = CyprusSandLightSurface,
    onSurface = CyprusSandLightOnSurface,
    surfaceVariant = CyprusSandLightSurfaceVariant,
    onSurfaceVariant = CyprusSandLightOnSurfaceVariant
)

private val CyprusSandDarkColorScheme = darkColorScheme(
    primary = CyprusSandDarkPrimary,
    onPrimary = CyprusSandDarkOnPrimary,
    primaryContainer = CyprusSandDarkPrimaryContainer,
    onPrimaryContainer = CyprusSandDarkOnPrimaryContainer,
    background = CyprusSandDarkBackground,
    onBackground = CyprusSandDarkOnBackground,
    surface = CyprusSandDarkSurface,
    onSurface = CyprusSandDarkOnSurface,
    surfaceVariant = CyprusSandDarkSurfaceVariant,
    onSurfaceVariant = CyprusSandDarkOnSurfaceVariant
)

// 2. Bridal & Skin Tone
private val BridalSkinLightColorScheme = lightColorScheme(
    primary = BridalSkinLightPrimary,
    onPrimary = BridalSkinLightOnPrimary,
    primaryContainer = BridalSkinLightPrimaryContainer,
    onPrimaryContainer = BridalSkinLightOnPrimaryContainer,
    background = BridalSkinLightBackground,
    onBackground = BridalSkinLightOnBackground,
    surface = BridalSkinLightSurface,
    onSurface = BridalSkinLightOnSurface,
    surfaceVariant = BridalSkinLightSurfaceVariant,
    onSurfaceVariant = BridalSkinLightOnSurfaceVariant
)

private val BridalSkinDarkColorScheme = darkColorScheme(
    primary = BridalSkinDarkPrimary,
    onPrimary = BridalSkinDarkOnPrimary,
    primaryContainer = BridalSkinDarkPrimaryContainer,
    onPrimaryContainer = BridalSkinDarkOnPrimaryContainer,
    background = BridalSkinDarkBackground,
    onBackground = BridalSkinDarkOnBackground,
    surface = BridalSkinDarkSurface,
    onSurface = BridalSkinDarkOnSurface,
    surfaceVariant = BridalSkinDarkSurfaceVariant,
    onSurfaceVariant = BridalSkinDarkOnSurfaceVariant
)

// 3. Pumpkin & Charcoal
private val PumpkinCharcoalLightColorScheme = lightColorScheme(
    primary = PumpkinCharcoalLightPrimary,
    onPrimary = PumpkinCharcoalLightOnPrimary,
    primaryContainer = PumpkinCharcoalLightPrimaryContainer,
    onPrimaryContainer = PumpkinCharcoalLightOnPrimaryContainer,
    background = PumpkinCharcoalLightBackground,
    onBackground = PumpkinCharcoalLightOnBackground,
    surface = PumpkinCharcoalLightSurface,
    onSurface = PumpkinCharcoalLightOnSurface,
    surfaceVariant = PumpkinCharcoalLightSurfaceVariant,
    onSurfaceVariant = PumpkinCharcoalLightOnSurfaceVariant
)

private val PumpkinCharcoalDarkColorScheme = darkColorScheme(
    primary = PumpkinCharcoalDarkPrimary,
    onPrimary = PumpkinCharcoalDarkOnPrimary,
    primaryContainer = PumpkinCharcoalDarkPrimaryContainer,
    onPrimaryContainer = PumpkinCharcoalDarkOnPrimaryContainer,
    background = PumpkinCharcoalDarkBackground,
    onBackground = PumpkinCharcoalDarkOnBackground,
    surface = PumpkinCharcoalDarkSurface,
    onSurface = PumpkinCharcoalDarkOnSurface,
    surfaceVariant = PumpkinCharcoalDarkSurfaceVariant,
    onSurfaceVariant = PumpkinCharcoalDarkOnSurfaceVariant
)

// 4. Ocean Blue & Cloudy Sky
private val OceanCloudyLightColorScheme = lightColorScheme(
    primary = OceanCloudyLightPrimary,
    onPrimary = OceanCloudyLightOnPrimary,
    primaryContainer = OceanCloudyLightPrimaryContainer,
    onPrimaryContainer = OceanCloudyLightOnPrimaryContainer,
    background = OceanCloudyLightBackground,
    onBackground = OceanCloudyLightOnBackground,
    surface = OceanCloudyLightSurface,
    onSurface = OceanCloudyLightOnSurface,
    surfaceVariant = OceanCloudyLightSurfaceVariant,
    onSurfaceVariant = OceanCloudyLightOnSurfaceVariant
)

private val OceanCloudyDarkColorScheme = darkColorScheme(
    primary = OceanCloudyDarkPrimary,
    onPrimary = OceanCloudyDarkOnPrimary,
    primaryContainer = OceanCloudyDarkPrimaryContainer,
    onPrimaryContainer = OceanCloudyDarkOnPrimaryContainer,
    background = OceanCloudyDarkBackground,
    onBackground = OceanCloudyDarkOnBackground,
    surface = OceanCloudyDarkSurface,
    onSurface = OceanCloudyDarkOnSurface,
    surfaceVariant = OceanCloudyDarkSurfaceVariant,
    onSurfaceVariant = OceanCloudyDarkOnSurfaceVariant
)

// 5. Deep Olive & Soft Sage
private val OliveSageLightColorScheme = lightColorScheme(
    primary = OliveSageLightPrimary,
    onPrimary = OliveSageLightOnPrimary,
    primaryContainer = OliveSageLightPrimaryContainer,
    onPrimaryContainer = OliveSageLightOnPrimaryContainer,
    background = OliveSageLightBackground,
    onBackground = OliveSageLightOnBackground,
    surface = OliveSageLightSurface,
    onSurface = OliveSageLightOnSurface,
    surfaceVariant = OliveSageLightSurfaceVariant,
    onSurfaceVariant = OliveSageLightOnSurfaceVariant
)

private val OliveSageDarkColorScheme = darkColorScheme(
    primary = OliveSageDarkPrimary,
    onPrimary = OliveSageDarkOnPrimary,
    primaryContainer = OliveSageDarkPrimaryContainer,
    onPrimaryContainer = OliveSageDarkOnPrimaryContainer,
    background = OliveSageDarkBackground,
    onBackground = OliveSageDarkOnBackground,
    surface = OliveSageDarkSurface,
    onSurface = OliveSageDarkOnSurface,
    surfaceVariant = OliveSageDarkSurfaceVariant,
    onSurfaceVariant = OliveSageDarkOnSurfaceVariant
)

private val AmoledLightBubbleColors = BubbleColors(
    incomingBackground = AmoledLightIncomingBubble,
    incomingText = AmoledLightOnSurface,
    outgoingBackground = AmoledLightOutgoingBubble,
    outgoingText = AmoledLightOnPrimaryContainer,
    timeTextColor = AmoledLightTimeText,
    tickColor = AmoledLightTickColor
)

private fun applyAccentOverlay(
    colorScheme: ColorScheme,
    bubbleColors: BubbleColors,
    accent: com.example.data.model.AccentColor,
    isDark: Boolean
): Pair<ColorScheme, BubbleColors> {
    val accentArgb = accent.argbFor(isDark) ?: return Pair(colorScheme, bubbleColors)
    val accentColor = Color(accentArgb)
    
    val lum = 0.2126f * accentColor.red + 0.7152f * accentColor.green + 0.0722f * accentColor.blue
    val onPrimary = if (lum > 0.5f) Color(0xFF000000) else Color(0xFFFFFFFF)
    
    val bg = colorScheme.background
    val onBg = colorScheme.onBackground
    
    val primaryContainer = androidx.compose.ui.graphics.lerp(bg, accentColor, 0.18f)
    val onPrimaryContainer = androidx.compose.ui.graphics.lerp(onBg, accentColor, 0.5f)
    
    val updatedScheme = colorScheme.copy(
        primary = accentColor,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer
    )
    
    val updatedBubbles = bubbleColors.copy(
        outgoingBackground = accentColor,
        outgoingText = onPrimary,
        tickColor = accentColor
    )
    
    return Pair(updatedScheme, updatedBubbles)
}

@Composable
fun SmsGramTheme(
    family: com.example.data.model.ThemeFamily = com.example.data.model.ThemeFamily.CLASSIC,
    isDark: Boolean = true,
    accent: com.example.data.model.AccentColor = com.example.data.model.AccentColor.DEFAULT,
    fontId: String = "vazirmatn",
    isRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val appFont = androidx.compose.runtime.remember(fontId) { com.example.data.model.AppFont.getById(fontId) }
    val fontFamily = androidx.compose.runtime.remember(appFont, context) { appFont.getFontFamily(context) }
    val appTypography = androidx.compose.runtime.remember(fontFamily) { createAppTypography(fontFamily) }

    val effectiveMode = com.example.domain.theme.ThemeResolver.resolvePalette(family, isDark)

    val (rawScheme, rawBubbles) = androidx.compose.runtime.remember(family, isDark, systemDark) {
        when (effectiveMode) {
            ThemeMode.DAY -> Pair(
                DayColorScheme,
                BubbleColors(
                    incomingBackground = DayIncomingBubble,
                    incomingText = DayOnSurface,
                    outgoingBackground = DayOutgoingBubble,
                    outgoingText = DayOnBackground,
                    timeTextColor = DayOnSurfaceVariant,
                    tickColor = DayPrimary
                )
            )
            ThemeMode.DARK_SLATE -> Pair(
                SlateColorScheme,
                BubbleColors(
                    incomingBackground = SlateIncomingBubble,
                    incomingText = SlateOnSurface,
                    outgoingBackground = SlateOutgoingBubble,
                    outgoingText = SlateOnPrimary,
                    timeTextColor = SlateOnSurfaceVariant,
                    tickColor = SlatePrimary
                )
            )
            ThemeMode.AMOLED_BLACK -> Pair(
                AmoledColorScheme,
                BubbleColors(
                    incomingBackground = AmoledIncomingBubble,
                    incomingText = AmoledOnSurface,
                    outgoingBackground = AmoledOutgoingBubble,
                    outgoingText = AmoledOnPrimary,
                    timeTextColor = AmoledOnSurfaceVariant,
                    tickColor = AmoledPrimary
                )
            )
            ThemeMode.AMOLED_LIGHT -> Pair(
                AmoledLightColorScheme,
                AmoledLightBubbleColors
            )
            ThemeMode.CYPRUS_SAND_LIGHT -> Pair(
                CyprusSandLightColorScheme,
                BubbleColors(
                    incomingBackground = CyprusSandLightIncomingBubble,
                    incomingText = CyprusSandLightOnSurface,
                    outgoingBackground = CyprusSandLightOutgoingBubble,
                    outgoingText = CyprusSandLightOnPrimaryContainer,
                    timeTextColor = CyprusSandLightOnSurfaceVariant,
                    tickColor = CyprusSandLightPrimary
                )
            )
            ThemeMode.CYPRUS_SAND_DARK -> Pair(
                CyprusSandDarkColorScheme,
                BubbleColors(
                    incomingBackground = CyprusSandDarkIncomingBubble,
                    incomingText = CyprusSandDarkOnSurface,
                    outgoingBackground = CyprusSandDarkOutgoingBubble,
                    outgoingText = CyprusSandDarkOnPrimaryContainer,
                    timeTextColor = CyprusSandDarkOnSurfaceVariant,
                    tickColor = CyprusSandDarkPrimary
                )
            )
            ThemeMode.BRIDAL_SKIN_LIGHT -> Pair(
                BridalSkinLightColorScheme,
                BubbleColors(
                    incomingBackground = BridalSkinLightIncomingBubble,
                    incomingText = BridalSkinLightOnSurface,
                    outgoingBackground = BridalSkinLightOutgoingBubble,
                    outgoingText = BridalSkinLightOnPrimaryContainer,
                    timeTextColor = BridalSkinLightOnSurfaceVariant,
                    tickColor = BridalSkinLightPrimary
                )
            )
            ThemeMode.BRIDAL_SKIN_DARK -> Pair(
                BridalSkinDarkColorScheme,
                BubbleColors(
                    incomingBackground = BridalSkinDarkIncomingBubble,
                    incomingText = BridalSkinDarkOnSurface,
                    outgoingBackground = BridalSkinDarkOutgoingBubble,
                    outgoingText = BridalSkinDarkOnPrimaryContainer,
                    timeTextColor = BridalSkinDarkOnSurfaceVariant,
                    tickColor = BridalSkinDarkPrimary
                )
            )
            ThemeMode.PUMPKIN_CHARCOAL_LIGHT -> Pair(
                PumpkinCharcoalLightColorScheme,
                BubbleColors(
                    incomingBackground = PumpkinCharcoalLightIncomingBubble,
                    incomingText = PumpkinCharcoalLightOnSurface,
                    outgoingBackground = PumpkinCharcoalLightOutgoingBubble,
                    outgoingText = PumpkinCharcoalLightOnPrimaryContainer,
                    timeTextColor = PumpkinCharcoalLightOnSurfaceVariant,
                    tickColor = PumpkinCharcoalLightPrimary
                )
            )
            ThemeMode.PUMPKIN_CHARCOAL_DARK -> Pair(
                PumpkinCharcoalDarkColorScheme,
                BubbleColors(
                    incomingBackground = PumpkinCharcoalDarkIncomingBubble,
                    incomingText = PumpkinCharcoalDarkOnSurface,
                    outgoingBackground = PumpkinCharcoalDarkOutgoingBubble,
                    outgoingText = PumpkinCharcoalDarkOnPrimaryContainer,
                    timeTextColor = PumpkinCharcoalDarkOnSurfaceVariant,
                    tickColor = PumpkinCharcoalDarkPrimary
                )
            )
            ThemeMode.OCEAN_CLOUDY_LIGHT -> Pair(
                OceanCloudyLightColorScheme,
                BubbleColors(
                    incomingBackground = OceanCloudyLightIncomingBubble,
                    incomingText = OceanCloudyLightOnSurface,
                    outgoingBackground = OceanCloudyLightOutgoingBubble,
                    outgoingText = OceanCloudyLightOnPrimaryContainer,
                    timeTextColor = OceanCloudyLightOnSurfaceVariant,
                    tickColor = OceanCloudyLightPrimary
                )
            )
            ThemeMode.OCEAN_CLOUDY_DARK -> Pair(
                OceanCloudyDarkColorScheme,
                BubbleColors(
                    incomingBackground = OceanCloudyDarkIncomingBubble,
                    incomingText = OceanCloudyDarkOnSurface,
                    outgoingBackground = OceanCloudyDarkOutgoingBubble,
                    outgoingText = OceanCloudyDarkOnPrimaryContainer,
                    timeTextColor = OceanCloudyDarkOnSurfaceVariant,
                    tickColor = OceanCloudyDarkPrimary
                )
            )
            ThemeMode.OLIVE_SAGE_LIGHT -> Pair(
                OliveSageLightColorScheme,
                BubbleColors(
                    incomingBackground = OliveSageLightIncomingBubble,
                    incomingText = OliveSageLightOnSurface,
                    outgoingBackground = OliveSageLightOutgoingBubble,
                    outgoingText = OliveSageLightOnPrimaryContainer,
                    timeTextColor = OliveSageLightOnSurfaceVariant,
                    tickColor = OliveSageLightPrimary
                )
            )
            ThemeMode.OLIVE_SAGE_DARK -> Pair(
                OliveSageDarkColorScheme,
                BubbleColors(
                    incomingBackground = OliveSageDarkIncomingBubble,
                    incomingText = OliveSageDarkOnSurface,
                    outgoingBackground = OliveSageDarkOutgoingBubble,
                    outgoingText = OliveSageDarkOnPrimaryContainer,
                    timeTextColor = OliveSageDarkOnSurfaceVariant,
                    tickColor = OliveSageDarkPrimary
                )
            )
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val dynamicScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                    Pair(
                        dynamicScheme,
                        BubbleColors(
                            incomingBackground = dynamicScheme.surfaceVariant,
                            incomingText = dynamicScheme.onSurfaceVariant,
                            outgoingBackground = dynamicScheme.primaryContainer,
                            outgoingText = dynamicScheme.onPrimaryContainer,
                            timeTextColor = dynamicScheme.outline,
                            tickColor = dynamicScheme.primary
                        )
                    )
                } else {
                    if (isDark) {
                        Pair(
                            SlateColorScheme,
                            BubbleColors(
                                incomingBackground = SlateIncomingBubble,
                                incomingText = SlateOnSurface,
                                outgoingBackground = SlateOutgoingBubble,
                                outgoingText = SlateOnPrimary,
                                timeTextColor = SlateOnSurfaceVariant,
                                tickColor = SlatePrimary
                            )
                        )
                    } else {
                        Pair(
                            DayColorScheme,
                            BubbleColors(
                                incomingBackground = DayIncomingBubble,
                                incomingText = DayOnSurface,
                                outgoingBackground = DayOutgoingBubble,
                                outgoingText = DayOnBackground,
                                timeTextColor = DayOnSurfaceVariant,
                                tickColor = DayPrimary
                            )
                        )
                    }
                }
            }
        }
    }

    val (colorScheme, bubbleColors) = androidx.compose.runtime.remember(rawScheme, rawBubbles, accent, isDark) {
        applyAccentOverlay(rawScheme, rawBubbles, accent, isDark)
    }

    val layoutDir = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val smsGramColors = androidx.compose.runtime.remember(colorScheme, bubbleColors, effectiveMode) {
        when (effectiveMode) {
            ThemeMode.DAY -> LightSmsGramColors.copy(
                simColors = listOf(colorScheme.primary, Color(0xFF34C759), Color(0xFFFF9500), Color(0xFFAF52DE))
            )
            ThemeMode.AMOLED_BLACK -> AmoledSmsGramColors.copy(
                simColors = listOf(colorScheme.primary, Color(0xFF34C759), Color(0xFFFF9500), Color(0xFFAF52DE))
            )
            ThemeMode.DARK_SLATE -> SlateSmsGramColors.copy(
                simColors = listOf(colorScheme.primary, Color(0xFF34C759), Color(0xFFFF9500), Color(0xFFAF52DE))
            )
            else -> SmsGramColors(
                bubbleOut = bubbleColors.outgoingBackground,
                bubbleIn = bubbleColors.incomingBackground,
                textPrimary = colorScheme.onBackground,
                textSecondary = colorScheme.onSurfaceVariant,
                surfaceCard = colorScheme.surfaceVariant,
                divider = colorScheme.outlineVariant.copy(alpha = 0.4f),
                danger = Color(0xFFFF3B30),
                success = Color(0xFF34C759),
                warning = Color(0xFFFF9500),
                simColors = listOf(
                    colorScheme.primary,
                    Color(0xFF34C759),
                    Color(0xFFFF9500),
                    Color(0xFFAF52DE)
                )
            )
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDir,
        LocalBubbleColors provides bubbleColors,
        LocalSmsGramColors provides smsGramColors,
        LocalAppFontFamily provides fontFamily
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = appTypography
        ) {
            CompositionLocalProvider(
                androidx.compose.material3.LocalTextStyle provides androidx.compose.material3.LocalTextStyle.current.copy(fontFamily = fontFamily),
                content = content
            )
        }
    }
}
