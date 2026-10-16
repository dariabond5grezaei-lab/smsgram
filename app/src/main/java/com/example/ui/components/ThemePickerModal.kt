package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.appStrings
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledLightBackground
import com.example.ui.theme.AmoledLightPrimary
import com.example.ui.theme.AmoledPrimary
import com.example.ui.theme.BridalColor
import com.example.ui.theme.BridalSkinLightBackground
import com.example.ui.theme.CyprusColor
import com.example.ui.theme.CyprusSandLightBackground
import com.example.ui.theme.DayBackground
import com.example.ui.theme.DayPrimary
import com.example.ui.theme.OceanBlueColor
import com.example.ui.theme.OceanCloudyLightBackground
import com.example.ui.theme.OliveColor
import com.example.ui.theme.OliveSageLightBackground
import com.example.ui.theme.PumpkinColor
import com.example.ui.theme.PumpkinCharcoalLightBackground
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlatePrimary
import com.example.ui.theme.smsGramColors
import com.example.ui.viewmodel.SmsViewModel

import com.example.data.model.ThemeFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerModal(
    viewModel: SmsViewModel,
    onDismiss: () -> Unit
) {
    val strings = appStrings()
    val currentLanguage = LocalAppLanguage.current
    val isFa = (currentLanguage == AppLanguage.PERSIAN)
    val colors = smsGramColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentFamily by viewModel.themeFamilyFlow.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isFa) "پوسته‌ها و رنگ‌بندی" else "Theme & Colors",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isFa) "انتخاب تم ظاهری و پالت رنگی برنامه" else "Customize your visual appearance",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.cancel,
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Theme Family Options List - Exactly 8 entries with side-by-side light and dark preview
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = ThemeFamily.values(), key = { it.name }) { family ->
                        val isSelected = (currentFamily == family)
                        val title = when (family) {
                            ThemeFamily.CLASSIC -> strings.themeClassic
                            ThemeFamily.AMOLED -> strings.themeAmoled
                            ThemeFamily.CYPRUS -> strings.themeCyprus
                            ThemeFamily.BRIDAL -> strings.themeBridal
                            ThemeFamily.PUMPKIN -> strings.themePumpkin
                            ThemeFamily.OCEAN -> strings.themeOcean
                            ThemeFamily.OLIVE -> strings.themeOlive
                            ThemeFamily.DYNAMIC -> strings.themeDynamic
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.setThemeFamily(family)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Side-by-side Light and Dark Swatches
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Light preview box
                                    val lightMode = family.lightMode ?: ThemeMode.DYNAMIC
                                    val (primaryLight, bgLight) = getThemePreviewColors(lightMode)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgLight)
                                            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp, 9.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(primaryLight.copy(alpha = 0.25f))
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp, 9.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(primaryLight)
                                        )
                                    }

                                    // Dark preview box
                                    val darkMode = family.darkMode ?: ThemeMode.DYNAMIC
                                    val (primaryDark, bgDark) = getThemePreviewColors(darkMode)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgDark)
                                            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp, 9.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(primaryDark.copy(alpha = 0.25f))
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp, 9.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(primaryDark)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Family Title
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                // Selection Checkmark Badge
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun getThemePreviewColors(mode: ThemeMode): Pair<Color, Color> {
    return when (mode) {
        ThemeMode.DAY -> Pair(DayPrimary, DayBackground)
        ThemeMode.DARK_SLATE -> Pair(SlatePrimary, SlateBackground)
        ThemeMode.AMOLED_LIGHT -> Pair(AmoledLightPrimary, AmoledLightBackground)
        ThemeMode.AMOLED_BLACK -> Pair(AmoledPrimary, AmoledBackground)
        ThemeMode.CYPRUS_SAND_LIGHT -> Pair(CyprusColor, CyprusSandLightBackground)
        ThemeMode.CYPRUS_SAND_DARK -> Pair(Color(0xFF72DDD0), Color(0xFF002421))
        ThemeMode.BRIDAL_SKIN_LIGHT -> Pair(BridalColor, BridalSkinLightBackground)
        ThemeMode.BRIDAL_SKIN_DARK -> Pair(Color(0xFFFFB3B8), Color(0xFF200B10))
        ThemeMode.PUMPKIN_CHARCOAL_LIGHT -> Pair(PumpkinColor, PumpkinCharcoalLightBackground)
        ThemeMode.PUMPKIN_CHARCOAL_DARK -> Pair(Color(0xFFFFB784), Color(0xFF141414))
        ThemeMode.OCEAN_CLOUDY_LIGHT -> Pair(OceanBlueColor, OceanCloudyLightBackground)
        ThemeMode.OCEAN_CLOUDY_DARK -> Pair(Color(0xFF86D2FF), Color(0xFF0B1720))
        ThemeMode.OLIVE_SAGE_LIGHT -> Pair(OliveColor, OliveSageLightBackground)
        ThemeMode.OLIVE_SAGE_DARK -> Pair(Color(0xFFACC8A2), Color(0xFF111C0F))
        ThemeMode.DYNAMIC -> Pair(Color(0xFF007AFF), Color(0xFFF2F2F7))
    }
}
