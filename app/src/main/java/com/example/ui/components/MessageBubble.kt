package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Surface
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmsMessageItem
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.SmartTextParser
import com.example.ui.locale.appStrings
import com.example.ui.theme.LocalBubbleColors
import com.example.ui.theme.LocalSmsGramColors
import com.example.ui.theme.SmsGramShapes
import com.example.ui.theme.SmsGramSpacing

import androidx.compose.ui.text.style.TextDirection
import com.example.data.model.DualSimInfo

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MessageBubble(
    message: SmsMessageItem,
    fontSize: Int = 15,
    usePersianNumerals: Boolean = true,
    activeSims: List<DualSimInfo> = emptyList(),
    isGroupedTop: Boolean = false,
    isGroupedBottom: Boolean = false,
    onPinMessage: (SmsMessageItem, Boolean) -> Unit,
    onSetReminder: (SmsMessageItem) -> Unit = {},
    onResendMessage: ((SmsMessageItem) -> Unit)? = null,
    onDeleteMessage: (SmsMessageItem) -> Unit,
    onCopyOtp: ((String) -> Unit)? = null,
    searchQuery: String = "",
    isCurrentSearchMatch: Boolean = false
) {
    val context = LocalContext.current
    val strings = appStrings()
    val bubbleColors = LocalBubbleColors.current
    val colors = LocalSmsGramColors.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val isUser = message.isSentByUser
    val alignment = if (isUser) Alignment.End else Alignment.Start

    // Adaptive bubble corners with grouping
    val topStartRadius = if (!isUser && isGroupedTop) 6.dp else 16.dp
    val topEndRadius = if (isUser && isGroupedTop) 6.dp else 16.dp
    val bottomStartRadius = if (!isUser) (if (isGroupedBottom) 6.dp else 4.dp) else 16.dp
    val bottomEndRadius = if (isUser) (if (isGroupedBottom) 6.dp else 4.dp) else 16.dp

    val bubbleShape = RoundedCornerShape(
        topStart = topStartRadius,
        topEnd = topEndRadius,
        bottomStart = bottomStartRadius,
        bottomEnd = bottomEndRadius
    )

    val bubbleBg = if (isUser) bubbleColors.outgoingBackground else bubbleColors.incomingBackground
    val textColor = if (isUser) bubbleColors.outgoingText else bubbleColors.incomingText

    val parsed = message.smartActions
    val isFinancial = parsed.bankCard != null || parsed.shebaNumber != null ||
            message.body.contains("واریز") || message.body.contains("برداشت") || message.body.contains("بانک")

    val verticalPadding = if (isGroupedBottom) 1.5.dp else 4.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SmsGramSpacing.s, vertical = verticalPadding),
        horizontalAlignment = alignment
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .wrapContentWidth(alignment)
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .combinedClickable(
                        onClick = { },
                        onLongClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showMenu = true
                        }
                    )
                    .padding(horizontal = 11.dp, vertical = 7.dp)
            ) {
                // Pinned indicator badge
                if (message.isPinned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = SmsGramSpacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = strings.pinnedMessage,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(SmsGramSpacing.xs))
                        Text(
                            text = strings.pinnedMessage,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Bank Header for banking SMS
                if (isFinancial && !isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(colors.success.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.bankTransactionBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.success
                        )
                    }
                }

                // Message Text
                SelectionContainer {
                    val highlightedText = remember(message.body, searchQuery, isCurrentSearchMatch) {
                        if (searchQuery.isNotBlank()) {
                            val builder = AnnotatedString.Builder(message.body)
                            val highlightColor = if (isCurrentSearchMatch) Color(0xFFFFD54F) else Color(0xFFFFE082).copy(alpha = 0.6f)
                            val lowerBody = message.body.lowercase()
                            val lowerQuery = searchQuery.trim().lowercase()
                            var startIndex = 0
                            while (startIndex < message.body.length && lowerQuery.isNotEmpty()) {
                                val found = lowerBody.indexOf(lowerQuery, startIndex)
                                if (found == -1) break
                                builder.addStyle(
                                    SpanStyle(background = highlightColor),
                                    found,
                                    found + lowerQuery.length
                                )
                                startIndex = found + lowerQuery.length
                            }
                            builder.toAnnotatedString()
                        } else {
                            AnnotatedString(message.body)
                        }
                    }

                    Text(
                        text = highlightedText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize + 5).sp,
                            textDirection = TextDirection.Content
                        ),
                        color = textColor
                    )
                }

                // Smart Action Chips in iOS Style (OTP, Card, Sheba, Call, URL)
                if (parsed.otpCode != null || parsed.bankCard != null || parsed.shebaNumber != null ||
                    parsed.phoneNumbers.isNotEmpty() || parsed.urls.isNotEmpty()
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // OTP Code Button (Phase B: 48dp height, 16sp text)
                        parsed.otpCode?.let { code ->
                            var isCopiedState by remember { mutableStateOf(false) }
                            LaunchedEffect(isCopiedState) {
                                if (isCopiedState) {
                                    delay(3000L)
                                    isCopiedState = false
                                }
                            }
                            val buttonLabel = if (isCopiedState) strings.copied else strings.copy_code
                            val buttonIcon = if (isCopiedState) Icons.Default.Check else Icons.Default.Key

                            Surface(
                                shape = SmsGramShapes.medium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clickable {
                                        isCopiedState = true
                                        if (onCopyOtp != null) {
                                            onCopyOtp(code)
                                        } else {
                                            copyToClipboard(context, strings.copy_code, code)
                                        }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = buttonIcon,
                                        contentDescription = buttonLabel,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = buttonLabel,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Bank Card Chip
                        parsed.bankCard?.let { card ->
                            val formatted = SmartTextParser.formatCardNumber(card)
                            AssistChip(
                                onClick = {
                                    copyToClipboard(context, strings.card, card)
                                    Toast.makeText(context, "${strings.cardCopied} 💳", Toast.LENGTH_SHORT).show()
                                },
                                label = {
                                    Text(
                                        text = formatted,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = strings.card,
                                        tint = colors.warning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                shape = SmsGramShapes.medium
                            )
                        }

                        // Sheba Chip
                        parsed.shebaNumber?.let { sheba ->
                            AssistChip(
                                onClick = {
                                    copyToClipboard(context, strings.shebaNumber, sheba)
                                    Toast.makeText(context, strings.shebaCopied, Toast.LENGTH_SHORT).show()
                                },
                                label = {
                                    Text(
                                        text = strings.copySheba,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                shape = SmsGramShapes.medium
                            )
                        }

                        // Phone Call Chip
                        parsed.phoneNumbers.firstOrNull()?.let { phone ->
                            AssistChip(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                },
                                label = {
                                    Text(
                                        text = "${strings.call}: $phone",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = strings.call,
                                        tint = colors.success,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                shape = SmsGramShapes.medium
                            )
                        }

                        // Web Link Chip
                        parsed.urls.firstOrNull()?.let { url ->
                            AssistChip(
                                onClick = {
                                    val safeUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                        "https://$url"
                                    } else url
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))
                                    context.startActivity(intent)
                                },
                                label = {
                                    Text(
                                        text = strings.openLink,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = strings.openLink,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                shape = SmsGramShapes.medium
                            )
                        }
                    }
                }

                // Timestamp & Delivery Status embedded inside bubble
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val matchingSim = remember(message.subId, activeSims) {
                        if (message.subId == -1 || activeSims.isEmpty()) {
                            null
                        } else {
                            val bySubId = activeSims.find { it.subscriptionId == message.subId }
                            if (bySubId != null) {
                                bySubId
                            } else if (message.subId == 0) {
                                activeSims.find { it.slotIndex == 0 } ?: activeSims.firstOrNull()
                            } else if (message.subId == 1 && activeSims.none { it.subscriptionId == 1 }) {
                                activeSims.find { it.slotIndex == 1 } ?: activeSims.getOrNull(1)
                            } else {
                                activeSims.find { it.slotIndex == message.subId }
                                    ?: if (message.subId in 0 until activeSims.size) activeSims.getOrNull(message.subId) else null
                            }
                        }
                    }

                    if (activeSims.size > 1 && matchingSim != null) {
                        val simNumber = matchingSim.slotIndex + 1
                        val badgeLabel = if (matchingSim.customLabel.isNotBlank()) {
                            matchingSim.customLabel
                        } else if (usePersianNumerals) {
                            JalaliCalendar.toPersianDigits("سیم $simNumber")
                        } else {
                            "SIM $simNumber"
                        }
                        val badgeColor = matchingSim.getThemeHarmonizedColor()

                        Box(
                            modifier = Modifier
                                .clip(SmsGramShapes.small)
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = badgeColor
                            )
                        }
                        Spacer(modifier = Modifier.width(SmsGramSpacing.xs))
                    }

                    val timeStr = JalaliCalendar.formatMessageTime(message.dateEpoch, usePersianNumerals)
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )

                    if (isUser) {
                        Spacer(modifier = Modifier.width(3.dp))
                        when {
                            message.status == 64 -> { // STATUS_FAILED
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onResendMessage?.invoke(message) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = strings.failedToSend,
                                        tint = colors.danger,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = strings.resend,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = colors.danger
                                    )
                                }
                            }
                            message.status == 32 -> { // STATUS_PENDING
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = strings.sendingStatus,
                                    tint = colors.warning,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            message.isDelivered -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = strings.delivered,
                                    tint = colors.success,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = strings.sent,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Context Menu on Long Press
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (isUser && (message.status == 64 || message.status == 32) && onResendMessage != null) {
                    DropdownMenuItem(
                        text = { Text(strings.resend) },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            onResendMessage(message)
                            showMenu = false
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text(strings.copyText) },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        copyToClipboard(context, strings.copyText, message.body)
                        Toast.makeText(context, "${strings.textCopied} 📋", Toast.LENGTH_SHORT).show()
                        showMenu = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(if (message.isPinned) strings.unpinConversation else strings.pinConversation) },
                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = colors.warning) },
                    onClick = {
                        onPinMessage(message, !message.isPinned)
                        showMenu = false
                    }
                )

                DropdownMenuItem(
                    text = { Text(strings.setReminder) },
                    leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                    onClick = {
                        onSetReminder(message)
                        showMenu = false
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = strings.deleteMessage,
                            color = colors.danger
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = colors.danger) },
                    onClick = {
                        onDeleteMessage(message)
                        showMenu = false
                    }
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
