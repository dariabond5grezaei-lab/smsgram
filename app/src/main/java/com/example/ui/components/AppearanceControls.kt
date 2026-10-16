package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccentColor
import com.example.ui.locale.appStrings

@Composable
fun AccentColorRow(
    selectedAccent: AccentColor,
    isDark: Boolean,
    onAccentSelected: (AccentColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = appStrings()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = strings.accentColor,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccentColor.values().forEach { accent ->
                val isSelected = (accent == selectedAccent)
                val accentLabel = when (accent) {
                    AccentColor.DEFAULT -> strings.accentDefault
                    AccentColor.BLUE -> strings.accentBlue
                    AccentColor.GREEN -> strings.accentGreen
                    AccentColor.ORANGE -> strings.accentOrange
                    AccentColor.RED -> strings.accentRed
                    AccentColor.PURPLE -> strings.accentPurple
                    AccentColor.TEAL -> strings.accentTeal
                    AccentColor.PINK -> strings.accentPink
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                            } else Modifier
                        )
                        .padding(if (isSelected) 3.dp else 0.dp)
                        .clip(CircleShape)
                        .then(
                            if (accent == AccentColor.DEFAULT) {
                                Modifier.background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                            } else {
                                val colorLong = accent.argbFor(isDark) ?: 0xFF007AFFL
                                Modifier.background(Color(colorLong))
                            }
                        )
                        .clickable { onAccentSelected(accent) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        val checkTint = if (accent == AccentColor.DEFAULT) {
                            Color.White
                        } else {
                            val colorLong = accent.argbFor(isDark) ?: 0xFF007AFFL
                            val c = Color(colorLong)
                            val lum = 0.2126f * c.red + 0.7152f * c.green + 0.0722f * c.blue
                            if (lum > 0.5f) Color.Black else Color.White
                        }
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = accentLabel,
                            tint = checkTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
