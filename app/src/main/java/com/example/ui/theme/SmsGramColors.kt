package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class SmsGramColors(
    val bubbleOut: Color,
    val bubbleIn: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val danger: Color = Color(0xFFFF3B30),
    val success: Color = Color(0xFF34C759),
    val warning: Color = Color(0xFFFF9500),
    val surfaceCard: Color,
    val divider: Color,
    val simColors: List<Color> = listOf(
        Color(0xFF007AFF),
        Color(0xFF34C759),
        Color(0xFFFF9500),
        Color(0xFFAF52DE)
    )
)

object SmsGramSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

object SmsGramShapes {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val bubble = RoundedCornerShape(18.dp)
    val pill = RoundedCornerShape(50)
}

val LightSmsGramColors = SmsGramColors(
    bubbleOut = DayOutgoingBubble,
    bubbleIn = DayIncomingBubble,
    textPrimary = DayOnBackground,
    // Contrast compliant #6C6C70 (4.5:1 ratio on light surfaces)
    textSecondary = Color(0xFF6C6C70),
    surfaceCard = Color(0xFFFFFFFF),
    divider = Color(0xFF000000).copy(alpha = 0.08f)
)

val SlateSmsGramColors = SmsGramColors(
    bubbleOut = SlateOutgoingBubble,
    bubbleIn = SlateIncomingBubble,
    textPrimary = SlateOnBackground,
    textSecondary = SlateOnSurfaceVariant,
    surfaceCard = SlateSurface,
    divider = Color(0xFFFFFFFF).copy(alpha = 0.08f)
)

val AmoledSmsGramColors = SmsGramColors(
    bubbleOut = AmoledOutgoingBubble,
    bubbleIn = AmoledIncomingBubble,
    textPrimary = AmoledOnBackground,
    textSecondary = AmoledOnSurfaceVariant,
    surfaceCard = AmoledSurface,
    divider = Color(0xFFFFFFFF).copy(alpha = 0.1f)
)

val LocalSmsGramColors = staticCompositionLocalOf { SlateSmsGramColors }

val smsGramColors: SmsGramColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSmsGramColors.current
