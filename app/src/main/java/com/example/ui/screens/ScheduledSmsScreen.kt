package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.local.ScheduleStatus
import com.example.data.local.ScheduledSmsEntity
import com.example.data.model.AppLanguage
import com.example.domain.scheduling.BatteryOptimizationHelper
import com.example.domain.scheduling.SmsScheduler
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.ShamsiDateUtils
import com.example.ui.components.ScheduleSheet
import com.example.ui.components.SmsGramEmptyState
import com.example.ui.components.SmsGramTopBar
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.appStrings
import com.example.ui.theme.IosTelegramPurple
import com.example.ui.theme.smsGramColors
import com.example.ui.viewmodel.SmsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledSmsScreen(
    viewModel: SmsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = appStrings()
    val colors = smsGramColors
    val currentLanguage = LocalAppLanguage.current
    val usePersianNumerals by viewModel.persianDigitsFlow.collectAsState()
    val calendarSystem by viewModel.calendarSystemFlow.collectAsState()
    val activeSims by viewModel.activeSims.collectAsState()

    val activeList by viewModel.scheduledActive.collectAsState()
    val historyList by viewModel.scheduledHistory.collectAsState()

    // Permission check for exact alarms on Android 12+
    var canUseExactAlarm by remember {
        mutableStateOf(SmsScheduler.canUseExactAlarms(context))
    }
    var exactAlarmBannerDismissed by remember { mutableStateOf(false) }

    // Battery optimization state
    var isIgnoringBattery by remember {
        mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }
    var batteryBannerSessionDismissed by remember { mutableStateOf(false) }

    // Re-check on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canUseExactAlarm = SmsScheduler.canUseExactAlarms(context)
                isIgnoringBattery = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // State for editing scheduled item
    var itemToEdit by remember { mutableStateOf<ScheduledSmsEntity?>(null) }
    // State for canceling confirmation dialog
    var itemToCancel by remember { mutableStateOf<ScheduledSmsEntity?>(null) }

    val now = remember { System.currentTimeMillis() }
    val todayStartCal = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val todayEndCal = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    val todayItems = remember(activeList) {
        activeList.filter {
            it.scheduledEpoch in todayStartCal.timeInMillis..todayEndCal.timeInMillis
        }
    }
    val laterItems = remember(activeList) {
        activeList.filter {
            it.scheduledEpoch > todayEndCal.timeInMillis || it.scheduledEpoch < todayStartCal.timeInMillis
        }
    }

    val totalCount = activeList.size
    val formattedCount = if (usePersianNumerals) JalaliCalendar.toPersianDigits(totalCount.toString()) else totalCount.toString()
    val subtitle = if (totalCount > 0) "$formattedCount ${strings.scheduledSmsCount}" else null

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmsGramTopBar(
                title = strings.scheduledSms,
                subtitle = subtitle,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Exact Alarm Permission Banner (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canUseExactAlarm && !exactAlarmBannerDismissed) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.exactAlarmPermissionBanner,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                    data = Uri.parse("package:${context.packageName}")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.fromParts("package", context.packageName, null)
                                                }
                                                context.startActivity(fallback)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text(strings.allowExactAlarmButton)
                                    }
                                    TextButton(onClick = { exactAlarmBannerDismissed = true }) {
                                        Text(strings.laterButton, color = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Battery Optimization Guidance Card (S13)
                val hasPendingOrSending = activeList.any { it.status == ScheduleStatus.PENDING || it.status == ScheduleStatus.SENDING }
                val isBatteryDismissedInPrefs = (context.applicationContext as? com.example.SmsGramApp)?.preferences?.isBatteryBannerDismissed() ?: false
                if (hasPendingOrSending && !isIgnoringBattery && !isBatteryDismissedInPrefs && !batteryBannerSessionDismissed) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryAlert,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.batteryRestrictionTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.batteryRestrictionBody,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { BatteryOptimizationHelper.openBatterySettings(context) }
                                    ) {
                                        Text(strings.openSettingsButton)
                                    }
                                    TextButton(onClick = { batteryBannerSessionDismissed = true }) {
                                        Text(strings.laterButton)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(onClick = {
                                        (context.applicationContext as? com.example.SmsGramApp)?.preferences?.dismissBatteryBanner()
                                        batteryBannerSessionDismissed = true
                                    }) {
                                        Text(strings.dontShowAgainButton, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Force Stop Notice (S12)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.forceStopWarningNotice,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (todayItems.isEmpty() && laterItems.isEmpty() && historyList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SmsGramEmptyState(
                                icon = Icons.Default.Schedule,
                                title = strings.noScheduledSmsTitle,
                                subtitle = strings.noScheduledSmsSubtitle
                            )
                        }
                    }
                } else {
                    // Section: Today
                    if (todayItems.isNotEmpty()) {
                        item {
                            SectionHeader(strings.sectionToday)
                        }
                        items(todayItems, key = { it.id }) { item ->
                            ScheduledItemCard(
                                item = item,
                                activeSims = activeSims,
                                usePersianNumerals = usePersianNumerals,
                                currentLanguage = currentLanguage,
                                calendarSystem = calendarSystem,
                                onEdit = { itemToEdit = item },
                                onCancel = { itemToCancel = item },
                                onSendNow = { viewModel.sendScheduledNow(item.id) },
                                onDelete = { viewModel.deleteScheduledFromHistory(item.id) }
                            )
                        }
                    }

                    // Section: Later
                    if (laterItems.isNotEmpty()) {
                        item {
                            SectionHeader(strings.sectionLater)
                        }
                        items(laterItems, key = { it.id }) { item ->
                            ScheduledItemCard(
                                item = item,
                                activeSims = activeSims,
                                usePersianNumerals = usePersianNumerals,
                                currentLanguage = currentLanguage,
                                calendarSystem = calendarSystem,
                                onEdit = { itemToEdit = item },
                                onCancel = { itemToCancel = item },
                                onSendNow = { viewModel.sendScheduledNow(item.id) },
                                onDelete = { viewModel.deleteScheduledFromHistory(item.id) }
                            )
                        }
                    }

                    // Section: History
                    if (historyList.isNotEmpty()) {
                        item {
                            SectionHeader(strings.sectionHistory)
                        }
                        items(historyList, key = { it.id }) { item ->
                            ScheduledItemCard(
                                item = item,
                                activeSims = activeSims,
                                usePersianNumerals = usePersianNumerals,
                                currentLanguage = currentLanguage,
                                calendarSystem = calendarSystem,
                                isHistory = true,
                                onEdit = { itemToEdit = item },
                                onCancel = { itemToCancel = item },
                                onSendNow = { viewModel.sendScheduledNow(item.id) },
                                onDelete = { viewModel.deleteScheduledFromHistory(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Schedule Sheet
    itemToEdit?.let { item ->
        ScheduleSheet(
            recipient = item.recipient,
            messageBody = item.body,
            initialEpoch = item.scheduledEpoch,
            calendarSystem = calendarSystem,
            usePersianDigits = usePersianNumerals,
            onDismissRequest = { itemToEdit = null },
            onScheduleConfirmed = { newEpoch ->
                viewModel.editScheduledSms(
                    id = item.id,
                    epochTime = newEpoch,
                    body = item.body,
                    subId = item.subId,
                    onSuccess = { itemToEdit = null }
                )
            }
        )
    }

    // Cancel Confirmation Dialog
    itemToCancel?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToCancel = null },
            title = { Text(strings.cancel) },
            text = { Text(strings.cancelScheduleConfirm) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelScheduledSms(item.id)
                        itemToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.danger)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToCancel = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ScheduledItemCard(
    item: ScheduledSmsEntity,
    activeSims: List<com.example.data.model.DualSimInfo>,
    usePersianNumerals: Boolean,
    currentLanguage: AppLanguage,
    calendarSystem: com.example.data.model.CalendarSystem = com.example.data.model.CalendarSystem.SHAMSI,
    isHistory: Boolean = false,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onSendNow: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = appStrings()
    val colors = smsGramColors

    val statusLabel = when (item.status) {
        ScheduleStatus.PENDING -> strings.statusPendingLabel
        ScheduleStatus.SENDING -> strings.statusSendingLabel
        ScheduleStatus.SENT -> strings.statusSentLabel
        ScheduleStatus.FAILED -> strings.statusFailedLabel
        ScheduleStatus.CANCELLED -> strings.statusCancelledLabel
        ScheduleStatus.MISSED -> strings.statusMissedLabel
        else -> item.status
    }

    val statusColor = when (item.status) {
        ScheduleStatus.PENDING -> IosTelegramPurple
        ScheduleStatus.SENDING -> MaterialTheme.colorScheme.primary
        ScheduleStatus.SENT -> colors.success
        ScheduleStatus.FAILED, ScheduleStatus.MISSED -> colors.danger
        ScheduleStatus.CANCELLED -> colors.textSecondary
        else -> colors.textSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.recipientName ?: item.recipient,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // SIM label only when activeSimCount >= 2 and subId != -1 (S10)
                    if (activeSims.size >= 2 && item.subId != -1) {
                        val sim = activeSims.firstOrNull { it.subscriptionId == item.subId }
                        val simLabel = sim?.displayName ?: "SIM ${item.subId}"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = simLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Status chip
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body snippet
            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            val scheduledTime = com.example.domain.util.AppDateFormatter.formatDateTime(
                epochMs = item.scheduledEpoch,
                system = calendarSystem,
                persianDigits = usePersianNumerals,
                isPersianLanguage = (currentLanguage == AppLanguage.PERSIAN)
            )

            Text(
                text = scheduledTime,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )

            // Actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isHistory) {
                    // Actions for PENDING, MISSED, FAILED: Edit & Cancel
                    if (item.status in listOf(ScheduleStatus.PENDING, ScheduleStatus.MISSED, ScheduleStatus.FAILED)) {
                        IconButton(onClick = onEdit) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = strings.editTime, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onCancel) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = strings.cancel, tint = colors.danger)
                        }
                    }
                    // For FAILED and MISSED also "Send now"
                    if (item.status == ScheduleStatus.FAILED || item.status == ScheduleStatus.MISSED) {
                        TextButton(onClick = onSendNow) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.sendNowAction)
                        }
                    }
                } else {
                    // History row: delete action
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = strings.deleteFromHistoryAction, tint = colors.textSecondary)
                    }
                }
            }
        }
    }
}
