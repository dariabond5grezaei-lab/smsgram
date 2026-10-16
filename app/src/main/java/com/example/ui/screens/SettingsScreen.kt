package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.model.AppLanguage
import com.example.data.model.CalendarSystem
import com.example.domain.util.NotificationHelper
import com.example.ui.components.*
import com.example.ui.locale.appStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SmsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SmsViewModel,
    onBack: () -> Unit
) {
    val strings = appStrings()
    val colors = LocalSmsGramColors.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentTheme by viewModel.themeModeFlow.collectAsState()
    val currentFamily by viewModel.themeFamilyFlow.collectAsState()
    val currentAccent by viewModel.accentColorFlow.collectAsState()
    val isDark by viewModel.isDarkFlow.collectAsState()
    val currentFontId by viewModel.fontIdFlow.collectAsState()
    val currentLanguage by viewModel.languageFlow.collectAsState()
    val usePersianDigits by viewModel.persianDigitsFlow.collectAsState()
    val calendarSystem by viewModel.calendarSystemFlow.collectAsState()
    val fontSize by viewModel.fontSizeFlow.collectAsState()
    val defaultSimId by viewModel.defaultSimIdFlow.collectAsState()
    val activeSims by viewModel.activeSims.collectAsState()
    val deliveryReports by viewModel.deliveryReportsFlow.collectAsState()

    val customFontName = viewModel.preferences.getCustomFontName() ?: ""

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var showFontPicker by remember { mutableStateOf(false) }
    var showSimManagerModal by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }

    var sliderFontSize by remember(fontSize) { mutableFloatStateOf(fontSize.toFloat()) }

    val isLockEnabled = viewModel.preferences.isAppLockEnabled()
    var appLockState by remember { mutableStateOf(isLockEnabled) }

    var useBiometricState by remember { mutableStateOf(viewModel.preferences.isUseBiometric()) }
    var autoLockTimeoutState by remember { mutableStateOf(viewModel.preferences.getAutoLockTimeoutMinutes()) }
    var hideInRecentsState by remember { mutableStateOf(viewModel.preferences.isHideContentInRecents()) }
    var showAutoLockDialog by remember { mutableStateOf(false) }

    val isPrivacyEnabled = viewModel.preferences.isNotificationPrivacy()
    var notifPrivacyState by remember { mutableStateOf(isPrivacyEnabled) }

    val autoDeleteHours = viewModel.preferences.getAutoDeleteOtpHours()
    var otpHoursState by remember { mutableStateOf(autoDeleteHours) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmsGramTopBar(
                title = strings.settingsTitle,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // SMSGRAM Brand Header & Support Integrated
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "SMSGRAM",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "v4.5 • ${strings.appSubtitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        IosGroupItem(
                            icon = Icons.Default.Shield,
                            iconBg = IosTelegramSky,
                            title = strings.directTelegramContact,
                            value = "@alirezaei66",
                            onClick = {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://t.me/alirezaei66")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Telegram Developer", "https://t.me/alirezaei66")
                                    clipboard.setPrimaryClip(clip)
                                    val msg = String.format(strings.telegramAddressCopied, "t.me/alirezaei66")
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Group 1: Consolidated Appearance & Typography
                IosSectionHeader(title = strings.appearance)
                IosGroupCard {
                    IosGroupItem(
                        icon = Icons.Default.ColorLens,
                        iconBg = IosTelegramPurple,
                        title = strings.theme,
                        subtitle = currentTheme.getPaletteName(currentLanguage),
                        value = currentTheme.getDisplayName(currentLanguage),
                        onClick = { showThemeDialog = true }
                    )

                    IosDivider()

                    AccentColorRow(
                        selectedAccent = currentAccent,
                        isDark = isDark,
                        onAccentSelected = { accent ->
                            viewModel.setAccentColor(accent)
                        },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )

                    IosDivider()

                    val fontDisplay = if (currentFontId == "custom") {
                        String.format(strings.customFontNamed, customFontName)
                    } else {
                        com.example.data.model.AppFont.getById(currentFontId).getName(currentLanguage)
                    }

                    IosGroupItem(
                        icon = Icons.Default.FormatSize,
                        iconBg = IosTelegramIndigo,
                        title = strings.fontsAndDialogs,
                        value = fontDisplay,
                        onClick = { showFontPicker = true }
                    )

                    IosDivider()

                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.fontSize,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${sliderFontSize.toInt()} sp",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textSecondary)
                            Slider(
                                value = sliderFontSize,
                                onValueChange = { sliderFontSize = it },
                                onValueChangeFinished = {
                                    viewModel.preferences.setFontSize(sliderFontSize.toInt())
                                },
                                valueRange = 12f..24f,
                                steps = 11,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text("A", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.PERSIAN) "پیش‌نمایش اندازه قلم متن پیام‌ها" else "Message font size preview text",
                                fontSize = sliderFontSize.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IosDivider()

                    IosGroupItem(
                        icon = Icons.Default.Language,
                        iconBg = MaterialTheme.colorScheme.primary,
                        title = strings.languageSettings,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "زبان رابط کاربری برنامه" else "App Interface Language",
                        value = if (currentLanguage == AppLanguage.PERSIAN) "فارسی" else "English",
                        onClick = { showLanguageDialog = true }
                    )

                    IosDivider()

                    IosGroupSwitchItem(
                        icon = Icons.Default.FormatSize,
                        iconBg = IosTelegramOrange,
                        title = strings.persianDigits,
                        checked = usePersianDigits,
                        onCheckedChange = { viewModel.preferences.setPersianDigits(it) }
                    )

                    IosDivider()

                    val calendarSystemLabel = when (calendarSystem) {
                        CalendarSystem.SHAMSI -> strings.calendarShamsi
                        CalendarSystem.GREGORIAN -> strings.calendarGregorian
                        CalendarSystem.BOTH -> strings.calendarBoth
                    }

                    IosGroupItem(
                        icon = Icons.Default.CalendarToday,
                        iconBg = IosTelegramSky,
                        title = strings.calendarSystem,
                        value = calendarSystemLabel,
                        onClick = { showCalendarDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Group 2: Features & Management
                IosSectionHeader(title = if (currentLanguage == AppLanguage.PERSIAN) "قابلیت‌ها و امکانات" else "Features & Tools")
                IosGroupCard {
                    IosGroupItem(
                        icon = Icons.Default.Folder,
                        iconBg = MaterialTheme.colorScheme.primary,
                        title = strings.folders,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "دسته‌بندی و پوشه‌بندی گفتگوها" else "Category & conversation folders",
                        onClick = { viewModel.navigateTo(ScreenDestination.Folders) }
                    )

                    IosDivider()

                    IosGroupItem(
                        icon = Icons.Default.Schedule,
                        iconBg = IosTelegramPurple,
                        title = strings.scheduledSms,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "مدیریت پیامک‌های زمان‌بندی‌شده" else "Manage scheduled messages",
                        onClick = { viewModel.navigateTo(ScreenDestination.ScheduledSms) }
                    )

                    IosDivider()

                    IosGroupItem(
                        icon = Icons.Default.Bookmark,
                        iconBg = IosTelegramGreen,
                        title = strings.savedMessages,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "پیام‌های ذخیره‌شده و یادداشت‌ها" else "Saved messages & quick notes",
                        onClick = { viewModel.navigateTo(ScreenDestination.SavedMessages) }
                    )

                    IosDivider()

                    IosGroupItem(
                        icon = Icons.Default.Quickreply,
                        iconBg = IosTelegramGreen,
                        title = strings.cannedTemplates,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "قالب‌های متنی پاسخ سریع" else "Quick reply text templates",
                        onClick = { viewModel.navigateTo(ScreenDestination.CannedTemplates) }
                    )

                    IosDivider()

                    IosGroupItem(
                        icon = Icons.Default.Shield,
                        iconBg = IosTelegramTeal,
                        title = strings.rulesEngine,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "فیلتر هوشمند شماره‌ها و کلیدواژه‌ها" else "Smart filtering for senders and keywords",
                        onClick = { viewModel.navigateTo(ScreenDestination.Rules) }
                    )

                    IosDivider()

                    IosGroupItem(
                        icon = Icons.Default.Delete,
                        iconBg = IosTelegramRed,
                        title = strings.trash,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "بازیابی پیام‌های حذف شده" else "Restore deleted messages",
                        onClick = { viewModel.navigateTo(ScreenDestination.Trash) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Group 3: SIM & Delivery Reports
                IosSectionHeader(title = if (currentLanguage == AppLanguage.PERSIAN) "سیم‌کارت‌ها و شبکه" else "SIM Cards & Network")
                IosGroupCard {
                    val defaultSimTitle = if (defaultSimId == -1) {
                        if (currentLanguage == AppLanguage.PERSIAN) "همیشه بپرس" else "Always Ask"
                    } else {
                        activeSims.find { it.subscriptionId == defaultSimId }?.displayTitle ?: if (currentLanguage == AppLanguage.PERSIAN) "سیم‌کارت پیش‌فرض" else "Default SIM"
                    }

                    val simSubtitle = if (activeSims.isNotEmpty()) {
                        activeSims.joinToString(" • ") { sim ->
                            val opName = if (currentLanguage == AppLanguage.PERSIAN) sim.operatorType.faName else sim.operatorType.enName
                            val slotLabel = if (currentLanguage == AppLanguage.PERSIAN) "اسلات" else "Slot"
                            "$opName ($slotLabel ${sim.slotIndex + 1})"
                        }
                    } else {
                        if (currentLanguage == AppLanguage.PERSIAN) "مدیریت اسلات‌ها و سیم‌کارت پیش‌فرض" else "Manage dual SIM slots & default SIM"
                    }

                    IosGroupItem(
                        icon = Icons.Default.SimCard,
                        iconBg = MaterialTheme.colorScheme.primary,
                        title = if (currentLanguage == AppLanguage.PERSIAN) "مدیریت دو سیم‌کارت و ارسال" else "Dual SIM Management",
                        subtitle = simSubtitle,
                        value = defaultSimTitle,
                        onClick = { showSimManagerModal = true }
                    )

                    IosDivider()

                    IosGroupSwitchItem(
                        icon = Icons.Default.Check,
                        iconBg = IosTelegramGreen,
                        title = strings.deliveryReports,
                        checked = deliveryReports,
                        onCheckedChange = {
                            viewModel.preferences.setDeliveryReports(it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Group 4: Security & Privacy
                IosSectionHeader(title = strings.securityAndPrivacy)
                IosGroupCard {
                    IosGroupSwitchItem(
                        icon = Icons.Default.Lock,
                        iconBg = IosTelegramGreen,
                        title = strings.appLock,
                        checked = appLockState,
                        onCheckedChange = { checked ->
                            if (checked) {
                                newPinInput = ""
                                showPinDialog = true
                            } else {
                                viewModel.preferences.setAppLockEnabled(false)
                                appLockState = false
                            }
                        }
                    )

                    if (appLockState) {
                        IosDivider()

                        IosGroupItem(
                            icon = Icons.Default.Lock,
                            iconBg = MaterialTheme.colorScheme.primary,
                            title = if (currentLanguage == AppLanguage.PERSIAN) "تغییر رمز عبور" else "Change Passcode",
                            subtitle = if (currentLanguage == AppLanguage.PERSIAN) "رمز ۴ رقمی ورود به برنامه" else "4-digit app passcode",
                            onClick = {
                                newPinInput = ""
                                showPinDialog = true
                            }
                        )

                        IosDivider()

                        IosGroupSwitchItem(
                            icon = Icons.Default.Shield,
                            iconBg = IosTelegramPurple,
                            title = if (currentLanguage == AppLanguage.PERSIAN) "ورود سریع با اثرانگشت" else "Fingerprint Unlock",
                            checked = useBiometricState,
                            onCheckedChange = {
                                viewModel.preferences.setUseBiometric(it)
                                useBiometricState = it
                            }
                        )

                        IosDivider()

                        val timeoutLabel = when (autoLockTimeoutState) {
                            0 -> if (currentLanguage == AppLanguage.PERSIAN) "بلافاصله" else "Immediately"
                            1 -> if (currentLanguage == AppLanguage.PERSIAN) "پس از ۱ دقیقه" else "After 1 min"
                            5 -> if (currentLanguage == AppLanguage.PERSIAN) "پس از ۵ دقیقه" else "After 5 min"
                            15 -> if (currentLanguage == AppLanguage.PERSIAN) "پس از ۱۵ دقیقه" else "After 15 min"
                            else -> if (currentLanguage == AppLanguage.PERSIAN) "غیرفعال" else "Disabled"
                        }
                        IosGroupItem(
                            icon = Icons.Default.Schedule,
                            iconBg = IosTelegramOrange,
                            title = if (currentLanguage == AppLanguage.PERSIAN) "زمان قفل خودکار برنامه" else "Auto-Lock Timeout",
                            value = timeoutLabel,
                            onClick = { showAutoLockDialog = true }
                        )

                        IosDivider()

                        IosGroupSwitchItem(
                            icon = Icons.Default.Lock,
                            iconBg = IosTelegramTeal,
                            title = if (currentLanguage == AppLanguage.PERSIAN) "مخفی‌سازی در جابجایی برنامه‌ها" else "Hide Content in Recents",
                            checked = hideInRecentsState,
                            onCheckedChange = {
                                viewModel.preferences.setHideContentInRecents(it)
                                hideInRecentsState = it
                                (context as? MainActivity)?.applyWindowSecurityFlags()
                            }
                        )
                    }

                    IosDivider()

                    IosGroupItem(
                        icon = Icons.Default.Shield,
                        iconBg = IosTelegramRed,
                        title = if (currentLanguage == AppLanguage.PERSIAN) "کاربران و شماره‌های مسدود" else "Blocked Contacts",
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "مدیریت لیست سیاه (Blacklist)" else "Manage blacklisted senders",
                        onClick = { viewModel.navigateTo(ScreenDestination.BlockedContacts) }
                    )

                    IosDivider()

                    IosGroupSwitchItem(
                        icon = Icons.Default.Notifications,
                        iconBg = IosTelegramIndigo,
                        title = strings.notificationPrivacy,
                        checked = notifPrivacyState,
                        onCheckedChange = {
                            viewModel.preferences.setNotificationPrivacy(it)
                            notifPrivacyState = it
                        }
                    )

                    IosDivider()

                    val otpVal = if (otpHoursState <= 0) {
                        if (currentLanguage == AppLanguage.PERSIAN) "غیرفعال" else "Disabled"
                    } else {
                        if (currentLanguage == AppLanguage.PERSIAN) "$otpHoursState ساعت" else "$otpHoursState hours"
                    }

                    IosGroupItem(
                        icon = Icons.Default.AutoDelete,
                        iconBg = IosTelegramRed,
                        title = strings.autoDeleteOtp,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "حذف هوشمند پیامک‌های حاوی کد تایید" else "Automatically purge verification OTP codes",
                        value = otpVal,
                        onClick = {
                            val nextHours = when (otpHoursState) {
                                0 -> 1
                                1 -> 6
                                6 -> 24
                                else -> 0
                            }
                            viewModel.preferences.setAutoDeleteOtpHours(nextHours)
                            otpHoursState = nextHours
                            if (nextHours > 0) {
                                viewModel.triggerOtpPurge()
                            }
                        }
                    )

                    IosDivider()

                    val isIgnoringBattery = remember {
                        com.example.domain.scheduling.BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
                    }
                    val batteryStatusText = if (isIgnoringBattery) strings.batteryStatusUnrestricted else strings.batteryStatusRestricted

                    IosGroupItem(
                        icon = Icons.Default.BatteryAlert,
                        iconBg = if (isIgnoringBattery) IosTelegramGreen else IosTelegramOrange,
                        title = strings.batteryStatusTitle,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "بهینه‌سازی باتری برای ارسال دقیق پیامک‌های زمان‌بندی‌شده" else "Battery optimization for scheduled SMS reliability",
                        value = batteryStatusText,
                        onClick = {
                            com.example.domain.scheduling.BatteryOptimizationHelper.openBatterySettings(context)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Group 5: Backup & Export
                IosSectionHeader(title = strings.backupAndRestore)
                IosGroupCard {
                    IosGroupItem(
                        icon = Icons.Default.Upload,
                        iconBg = IosTelegramTeal,
                        title = strings.exportJson,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "کپی ساختار تنظیمات در حافظه کلیپ‌بورد" else "Copy settings JSON to clipboard",
                        onClick = {
                            scope.launch {
                                val json = viewModel.exportSettingsJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SMSGRAM Backup", json)
                                clipboard.setPrimaryClip(clip)
                                val msg = if (currentLanguage == AppLanguage.PERSIAN) "پشتیبان در حافظه کلیپ‌بورد کپی شد 💾" else "Backup copied to clipboard 💾"
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    IosDivider()

                    IosGroupItem(
                        icon = Icons.Default.Download,
                        iconBg = IosTelegramPurple,
                        title = strings.importJson,
                        subtitle = if (currentLanguage == AppLanguage.PERSIAN) "جایگذاری تنظیمات از کلیپ‌بورد" else "Restore settings from clipboard",
                        onClick = { showImportDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    // Font Selection Modal Sheet
    if (showFontPicker) {
        com.example.ui.components.FontPickerModal(
            viewModel = viewModel,
            onDismiss = { showFontPicker = false }
        )
    }

    // Dual SIM Manager Modal Sheet
    if (showSimManagerModal) {
        com.example.ui.components.DualSimManagerModal(
            viewModel = viewModel,
            onDismiss = { showSimManagerModal = false }
        )
    }

    // Theme Selection Modal Sheet
    if (showThemeDialog) {
        com.example.ui.components.ThemePickerModal(
            viewModel = viewModel,
            onDismiss = { showThemeDialog = false }
        )
    }

    // Calendar System Selector Dialog
    if (showCalendarDialog) {
        AlertDialog(
            onDismissRequest = { showCalendarDialog = false },
            title = { Text(strings.calendarSystem, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    CalendarSystem.values().forEach { sys ->
                        val label = when (sys) {
                            CalendarSystem.SHAMSI -> strings.calendarShamsi
                            CalendarSystem.GREGORIAN -> strings.calendarGregorian
                            CalendarSystem.BOTH -> strings.calendarBoth
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCalendarSystem(sys)
                                    showCalendarDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = (calendarSystem == sys),
                                onClick = {
                                    viewModel.setCalendarSystem(sys)
                                    showCalendarDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCalendarDialog = false }) {
                    Text(strings.cancel, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.languageSettings, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(AppLanguage.PERSIAN)
                                NotificationHelper.setupNotificationChannels(context)
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = (currentLanguage == AppLanguage.PERSIAN),
                            onClick = {
                                viewModel.setLanguage(AppLanguage.PERSIAN)
                                NotificationHelper.setupNotificationChannels(context)
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("فارسی (Persian)")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(AppLanguage.ENGLISH)
                                NotificationHelper.setupNotificationChannels(context)
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = (currentLanguage == AppLanguage.ENGLISH),
                            onClick = {
                                viewModel.setLanguage(AppLanguage.ENGLISH)
                                NotificationHelper.setupNotificationChannels(context)
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("English")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(strings.cancel, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Set PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(if (currentLanguage == AppLanguage.PERSIAN) "تنظیم رمز عبور (۴ تا ۶ رقم)" else "Set Passcode (4-6 digits)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(if (currentLanguage == AppLanguage.PERSIAN) "رمز عبور جدید را وارد کنید:" else "Enter new passcode:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 6) newPinInput = it },
                        label = { Text(if (currentLanguage == AppLanguage.PERSIAN) "رمز ۴ تا ۶ رقمی" else "Passcode (4-6 digits)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length >= 4) {
                            viewModel.preferences.setAppLockPin(newPinInput)
                            viewModel.preferences.setAppLockEnabled(true)
                            appLockState = true
                            showPinDialog = false
                            val toastMsg = if (currentLanguage == AppLanguage.PERSIAN) "رمز عبور با موفقیت تنظیم شد 🔒" else "Passcode saved successfully 🔒"
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = newPinInput.length >= 4
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Auto-Lock Timeout Dialog
    if (showAutoLockDialog) {
        val options = listOf(
            0 to (if (currentLanguage == AppLanguage.PERSIAN) "بلافاصله پس از خروج" else "Immediately after exit"),
            1 to (if (currentLanguage == AppLanguage.PERSIAN) "پس از ۱ دقیقه" else "After 1 minute"),
            5 to (if (currentLanguage == AppLanguage.PERSIAN) "پس از ۵ دقیقه" else "After 5 minutes"),
            15 to (if (currentLanguage == AppLanguage.PERSIAN) "پس از ۱۵ دقیقه" else "After 15 minutes"),
            -1 to (if (currentLanguage == AppLanguage.PERSIAN) "غیرفعال (فقط دستی)" else "Disabled (Manual only)")
        )
        AlertDialog(
            onDismissRequest = { showAutoLockDialog = false },
            title = { Text(if (currentLanguage == AppLanguage.PERSIAN) "زمان قفل خودکار" else "Auto-Lock Timeout", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    options.forEach { (mins, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.preferences.setAutoLockTimeoutMinutes(mins)
                                    autoLockTimeoutState = mins
                                    showAutoLockDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = (autoLockTimeoutState == mins),
                                onClick = {
                                    viewModel.preferences.setAutoLockTimeoutMinutes(mins)
                                    autoLockTimeoutState = mins
                                    showAutoLockDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoLockDialog = false }) {
                    Text(strings.cancel, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Import Backup Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(strings.importJson, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(if (currentLanguage == AppLanguage.PERSIAN) "محتوای فایل JSON پشتیبان را در کادر زیر وارد نمایید:" else "Paste backup JSON content below:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        label = { Text(if (currentLanguage == AppLanguage.PERSIAN) "کد پشتیبان JSON" else "JSON Backup Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = viewModel.importSettingsJson(importJsonText)
                            if (success) {
                                val msg = if (currentLanguage == AppLanguage.PERSIAN) "پشتیبان با موفقیت بازیابی شد ✅" else "Backup restored successfully ✅"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                val msg = if (currentLanguage == AppLanguage.PERSIAN) "خطا در ساختار فایل پشتیبان!" else "Invalid backup file structure!"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = importJsonText.isNotBlank()
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun IosSectionHeader(title: String) {
    val colors = com.example.ui.theme.LocalSmsGramColors.current
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = colors.textSecondary,
        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
    )
}

@Composable
private fun IosGroupCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun IosDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 58.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
}

@Composable
private fun IosGroupItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // iOS Squircle Icon Box
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            val colors = com.example.ui.theme.LocalSmsGramColors.current
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        if (value != null) {
            val colors = com.example.ui.theme.LocalSmsGramColors.current
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun IosGroupSwitchItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = com.example.ui.theme.LocalSmsGramColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.success
            )
        )
    }
}
