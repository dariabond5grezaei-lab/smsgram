package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.AvatarPalette
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarSystem
import com.example.data.model.ConversationItem
import com.example.domain.util.AppDateFormatter
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.PhoneNumberFormatter
import com.example.domain.util.SmartTextParser
import com.example.ui.locale.appStrings
import com.example.ui.theme.SmsGramShapes
import com.example.ui.theme.smsGramColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationListItem(
    conversation: ConversationItem,
    usePersianNumerals: Boolean = true,
    calendarSystem: CalendarSystem = CalendarSystem.SHAMSI,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onToggleSelect: () -> Unit = {},
    onPinToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onCreateRule: () -> Unit,
    onMoveToFolder: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val strings = appStrings()
    val colors = smsGramColors

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val gradientColors = AvatarPalette.getGradientFor(conversation.address, isDarkTheme)

    val parsedActions = remember(conversation.snippet) {
        SmartTextParser.parse(conversation.snippet)
    }
    val isOtp = remember(parsedActions) {
        parsedActions.otpCode != null
    }
    val bankInfo = remember(conversation.snippet, usePersianNumerals) {
        SmartTextParser.parseBankTransaction(conversation.snippet, usePersianNumerals)
    }
    val isBank = remember(bankInfo) {
        bankInfo != null
    }

    // Clean, 100% opaque background surface so SwipeToDismissBox background color never bleeds through
    val itemBg = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else if (conversation.isPinned) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    // Formatted title with Phone Number formatting
    val displayTitle = remember(conversation.contactName, conversation.address, usePersianNumerals) {
        if (conversation.contactName.isNullOrBlank()) {
            PhoneNumberFormatter.format(conversation.address, usePersianNumerals)
        } else {
            conversation.contactName
        }
    }
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) {
                            onToggleSelect()
                        } else {
                            onClick()
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (onLongClick != null) {
                            onLongClick()
                        } else {
                            onToggleSelect()
                        }
                    }
                ),
            color = itemBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Multi-select Checkbox
                AnimatedVisibility(
                    visible = isSelectionMode,
                    enter = slideInHorizontally() + fadeIn(),
                    exit = slideOutHorizontally() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(24.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = strings.selected,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = strings.notSelected,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Compact Avatar (46dp)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.initials,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )

                    // Badge on Avatar for OTP or Bank
                    if (isOtp || isBank) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                                    .background(if (isOtp) MaterialTheme.colorScheme.primary else colors.success),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOtp) Icons.Default.Key else Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Middle: Name & Snippet
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Line 1: Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Line 2: Message snippet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bankInfo != null) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isDeposit = bankInfo.actionType.contains("واریز")
                                val badgeColor = if (isDeposit) colors.success else colors.warning

                                Box(
                                    modifier = Modifier
                                        .clip(SmsGramShapes.small)
                                        .background(badgeColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = bankInfo.actionType,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        ),
                                        color = badgeColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "${bankInfo.amount} ${bankInfo.currency}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else if (!conversation.draftText.isNullOrBlank()) {
                            // G4: Draft message in error color
                            val draftDisplay = if (conversation.isLocked) strings.draftOnly else "${strings.draftPrefix}${conversation.draftText}"
                            Text(
                                text = draftDisplay,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            val formattedSnippet = remember(conversation.snippet, usePersianNumerals) {
                                if (usePersianNumerals) JalaliCalendar.toPersianDigits(conversation.snippet) else conversation.snippet
                            }
                            val isUnread = conversation.unreadCount > 0
                            Text(
                                text = formattedSnippet.ifBlank { strings.typeMessage.replace("…", "") },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal
                                ),
                                color = if (isUnread) MaterialTheme.colorScheme.onSurface else colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Meta (RTL Trailing): Timestamp, Pin, Unread Badge
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Time and Mute status
                    val isMuted = conversation.isMuted && (conversation.muteUntilEpoch == null || System.currentTimeMillis() < conversation.muteUntilEpoch)
                    val strings = appStrings()
                    val isPersianLanguage = (strings.appSubtitle == "پیام‌رسان متنی پیشرفته")
                    val timeStr = remember(conversation.dateEpoch, usePersianNumerals, calendarSystem) {
                        AppDateFormatter.formatRelativeTime(
                            epochMs = conversation.dateEpoch,
                            system = calendarSystem,
                            persianDigits = usePersianNumerals,
                            isPersianLanguage = isPersianLanguage
                        )
                    }
                    val isUnread = conversation.unreadCount > 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isMuted) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = strings.mute,
                                tint = colors.textSecondary,
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 2.dp)
                            )
                        }
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isUnread && !isMuted) MaterialTheme.colorScheme.primary else colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (conversation.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = strings.pinnedMessage,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(13.dp)
                                    .padding(end = 2.dp)
                            )
                        }

                        if (conversation.unreadCount > 0) {
                            val countText = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString()
                            val displayCount = if (usePersianNumerals) JalaliCalendar.toPersianDigits(countText) else countText

                            val badgeBg = if (isMuted) colors.textSecondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(badgeBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayCount,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Inset separator line
        HorizontalDivider(
            modifier = Modifier.padding(start = 74.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )

        // Long-press Context Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(strings.select) },
                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                onClick = {
                    onToggleSelect()
                    showMenu = false
                }
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            DropdownMenuItem(
                text = { Text(if (conversation.isPinned) strings.unpinConversation else strings.pinConversation) },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                onClick = {
                    onPinToggle(!conversation.isPinned)
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = { Text(strings.moveToFolder) },
                leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = colors.warning) },
                onClick = {
                    onMoveToFolder()
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = { Text(strings.createFilterRule) },
                leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                onClick = {
                    onCreateRule()
                    showMenu = false
                }
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = strings.deleteConversation,
                        color = colors.danger,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = colors.danger
                    )
                },
                onClick = {
                    onDelete()
                    showMenu = false
                }
            )
        }
    }
}
