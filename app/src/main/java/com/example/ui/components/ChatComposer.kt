package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.example.domain.util.pickQuickReplies
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CannedTemplateEntity
import com.example.data.model.AppLanguage
import com.example.data.model.DualSimInfo
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.SmsEncodingUtils
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.appStrings
import com.example.ui.theme.SmsGramShapes
import com.example.ui.theme.SmsGramSpacing
import com.example.ui.theme.smsGramColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatComposer(
    text: String,
    onTextChanged: (String) -> Unit,
    activeSims: List<DualSimInfo>,
    selectedSimIndex: Int,
    onSelectSimIndex: (Int) -> Unit,
    templates: List<CannedTemplateEntity>,
    onSend: (String) -> Unit,
    onScheduleClick: () -> Unit,
    threadId: Long? = null,
    onSetThreadDefaultSim: ((Int) -> Unit)? = null,
    onOpenSimManager: (() -> Unit)? = null
) {
    val strings = appStrings()
    val currentLanguage = LocalAppLanguage.current
    val colors = smsGramColors
    var showTemplateSheet by remember { mutableStateOf(false) }
    var showSimModal by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val countInfo = remember(text) {
        SmsEncodingUtils.calculateSmsCount(text)
    }

    val encodingLabel = if (countInfo.encoding == SmsEncodingUtils.EncodingType.UNICODE) {
        if (currentLanguage == AppLanguage.PERSIAN) "فارسی" else "Unicode"
    } else "GSM"

    val currentSim = activeSims.getOrNull(selectedSimIndex) ?: activeSims.firstOrNull()
    val simBrandColor = currentSim?.getThemeHarmonizedColor() ?: MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
        ) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = colors.divider
            )

            // Dynamic Counter & SIM Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SmsGramSpacing.m, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive SIM Selector Pill
                if (activeSims.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(SmsGramShapes.medium)
                            .background(simBrandColor.copy(alpha = 0.12f))
                            .clickable {
                                if (activeSims.size > 1) {
                                    showSimModal = true
                                }
                            }
                            .padding(horizontal = SmsGramSpacing.s, vertical = SmsGramSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored Circle Slot Badge
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(simBrandColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(currentSim?.slotIndex ?: 0) + 1}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        val defaultSimLabel = if (currentLanguage == AppLanguage.PERSIAN) "سیم‌کارت ۱" else "SIM 1"
                        Text(
                            text = currentSim?.displayTitle ?: defaultSimLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = simBrandColor
                        )

                        if (activeSims.size > 1) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = strings.changeSim,
                                tint = simBrandColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Show character counter only when near boundary or multi-segment
                val shouldShowCounter = countInfo.segmentCount > 1 || countInfo.remainingInSegment <= 20
                if (text.isNotEmpty() && shouldShowCounter) {
                    val counterText = if (currentLanguage == AppLanguage.PERSIAN) {
                        JalaliCalendar.toPersianDigits("${countInfo.remainingInSegment}/${countInfo.segmentCount} پیامک ($encodingLabel)")
                    } else {
                        "${countInfo.remainingInSegment}/${countInfo.segmentCount} SMS ($encodingLabel)"
                    }
                    Text(
                        text = counterText,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            // Quick Reply Chips (Phase A)
            val quickReplies = remember(templates) {
                pickQuickReplies(templates.map { it.content }, max = 6)
            }
            if (text.isEmpty() && quickReplies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.quickReply,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                    )
                    quickReplies.forEach { reply ->
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .padding(end = 6.dp)
                                .clickable {
                                    onTextChanged(reply)
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = reply,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Main Message Input Row
            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, top = 2.dp, bottom = 6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Quick Templates Icon
                IconButton(
                    onClick = { showTemplateSheet = true },
                    modifier = Modifier
                        .size(48.dp)
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Subject,
                        contentDescription = strings.cannedTemplates,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Rounded Capsule Text Field
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = {
                        Text(
                            text = strings.typeMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = SmsGramShapes.bubble,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 6,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = SmsGramSpacing.xs)
                )

                // Single Morphing Action Button (Schedule when blank, Send when text entered)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .minimumInteractiveComponentSize()
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .clickable {
                            if (text.isNotBlank()) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onSend(text.trim())
                            } else {
                                onScheduleClick()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (text.isNotBlank()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = strings.send,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "زمان‌بندی پیامک",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Quick Canned Templates Sheet
    if (showTemplateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTemplateSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = SmsGramSpacing.m)
            ) {
                Text(
                    text = strings.cannedTemplates,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(SmsGramSpacing.m))

                templates.forEach { t ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SmsGramShapes.medium)
                            .clickable {
                                onTextChanged(t.content)
                                showTemplateSheet = false
                            }
                            .padding(vertical = SmsGramSpacing.m, horizontal = SmsGramSpacing.s),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subject,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(SmsGramSpacing.m))
                        Column {
                            Text(
                                text = t.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = t.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = colors.divider
                    )
                }

                Spacer(modifier = Modifier.height(SmsGramSpacing.xxl))
            }
        }
    }

    if (showSimModal) {
        SimSelectionModal(
            activeSims = activeSims,
            selectedSimIndex = selectedSimIndex,
            threadId = threadId,
            onSimSelected = { index, remember ->
                onSelectSimIndex(index)
                if (remember && threadId != null && onSetThreadDefaultSim != null) {
                    val subId = activeSims.getOrNull(index)?.subscriptionId ?: -1
                    onSetThreadDefaultSim(subId)
                }
            },
            onOpenSimManager = onOpenSimManager,
            onDismiss = { showSimModal = false }
        )
    }
}
