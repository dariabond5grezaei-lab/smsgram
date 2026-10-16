package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.util.SmartTextParser
import com.example.domain.util.SmsChangeBus
import com.example.ui.locale.appStrings
import kotlinx.coroutines.delay

@Composable
fun IncomingBanner(
    isPrivacyMode: Boolean = false,
    isAppLockActive: Boolean = false,
    onOpenThread: (threadId: Long, sender: String) -> Unit
) {
    var bannerData by remember { mutableStateOf<SmsChangeBus.IncomingSms?>(null) }
    val strings = appStrings()

    LaunchedEffect(Unit) {
        SmsChangeBus.incoming.collect { incomingSms ->
            bannerData = incomingSms
            delay(4000)
            if (bannerData == incomingSms) {
                bannerData = null
            }
        }
    }

    AnimatedVisibility(
        visible = bannerData != null,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val data = bannerData ?: return@AnimatedVisibility
        val parsed = remember(data.body) { SmartTextParser.parse(data.body) }
        val isOtp = parsed.otpCode != null

        val isPrivate = isPrivacyMode || isAppLockActive
        val displayText = when {
            isPrivate -> strings.newMessageBanner
            isOtp -> strings.otpReceivedBanner
            else -> data.body
        }

        val senderTitle = data.sender.ifBlank { strings.appName }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val currentData = bannerData
                    bannerData = null
                    if (currentData != null) {
                        onOpenThread(currentData.threadId, currentData.sender)
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = senderTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (displayText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
