package com.example.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.AppLanguage

interface Strings {

    val appName: String
    val appSubtitle: String
    val cellularSms: String
    val scrollToBottom: String
    val blockedNumbers: String
    // Drawer & Navigation
    val allChats: String
    val personal: String
    val bank: String
    val advertising: String
    val services: String
    val savedMessages: String
    val folders: String
    val scheduledSms: String
    val cannedTemplates: String
    val rulesEngine: String
    val trash: String
    val settings: String
    val language: String
    val selectLanguage: String
    val nightMode: String
    val dayMode: String
    val defaultSmsApp: String
    val defaultSmsActive: String
    val tapToSetDefault: String

    // Home Screen
    val searchHint: String
    val noMessagesFound: String
    val noMessagesSub: String
    val newMessage: String
    val permissionRequired: String
    val permissionDescription: String
    val grantPermissions: String
    val setAsDefault: String
    val defaultPromptTitle: String
    val defaultPromptDesc: String

    // Conversation Screen
    val typeMessage: String
    val send: String
    val schedule: String
    val templates: String
    val sim1: String
    val sim2: String
    val today: String
    val yesterday: String
    val copyCode: String
    val copyCard: String
    val call: String
    val openLink: String
    val codeCopied: String
    val cardCopied: String
    val textCopied: String
    val messageInfo: String
    val sentAt: String
    val deliveredAt: String
    val statusSent: String
    val statusDelivered: String
    val statusPending: String
    val statusFailed: String
    val charsCount: String
    val partCount: String

    // Settings
    val settingsTitle: String
    val languageSettings: String
    val appearance: String
    val theme: String
    val fontSize: String
    val numbersAndDate: String
    val persianDigits: String
    val persianDigitsDesc: String
    val calendarSystem: String
    val calendarShamsi: String
    val calendarGregorian: String
    val calendarBoth: String
    val deliveryReports: String
    val deliveryReportsDesc: String
    val defaultSimCard: String
    val securityAndPrivacy: String
    val appLock: String
    val appLockDesc: String
    val notificationPrivacy: String
    val notificationPrivacyDesc: String
    val autoDeleteOtp: String
    val autoDeleteOtpDesc: String
    val backupAndRestore: String
    val exportJson: String
    val exportJsonDesc: String
    val importJson: String
    val importJsonDesc: String
    val aboutApp: String
    val version: String
    val developerNote: String

    // Actions & Common
    val cancel: String
    val confirm: String
    val save: String
    val delete: String
    val edit: String
    val back: String
    val close: String
    val done: String
    val success: String
    val error: String
    val enterPin: String
    val setPin: String
    val pinMismatch: String
    val pinSaved: String
    val permissionRequiredTitle: String
    val permissionRequiredDesc: String
    val pin: String
    val wrongPin: String
    val unlock: String
    val biometricUnlock: String
    val createRule: String
    val deleteConversation: String
    val pinnedMessage: String
    val unpin: String
    val scheduleSms: String
    val recipientNumber: String
    val messageBody: String
    val startChat: String
    val select: String
    val selectAll: String
    val savedNotesTitle: String
    val savedNotesSubtitle: String
    val noSavedNotes: String
    val messagingCategory: String
    val dataCategory: String
    val onboardingWelcomeTitle: String
    val onboardingWelcomeDesc: String
    val clearTrash: String
    val confirmClearTrash: String
    val newNotePlaceholder: String
    val changeSim: String
    val attachmentsAndTemplates: String
    val more: String
    val carrierSms: String
    val tenMinutesLater: String
    val thirtyMinutesLater: String
    val oneHourLater: String
    val tomorrowSameTime: String
    val copyText: String
    val pinConversation: String
    val unpinConversation: String
    val moveToFolder: String
    val createFilterRule: String
    val setReminder: String
    val deleteMessage: String
    val card: String
    val shebaNumber: String
    val shebaCopied: String
    val copySheba: String
    val delivered: String
    val sent: String
    val selected: String
    val notSelected: String
    val unknownMessage: String
    val dualSimManagement: String
    val activeSimsOnDevice: String
    val invertSimSlots: String
    val invertSimSlotsDesc: String
    val contactsPermissionRequired: String
    val grantContactsPermission: String
    val grantPermission: String
    val searchContactHint: String
    val clearSearch: String
    val editSimName: String
    val changeSimColor: String
    val defaultSimForSending: String
    val alwaysAskBeforeSending: String
    val manualSimSelectionDesc: String
    val alwaysWithSim: String
    val slotNumber: String
    val simDisplayName: String
    val enterCustomSimLabel: String
    val simNameSaved: String
    val selectSimColor: String
    val simColorDesc: String
    val resetToDefaultColor: String
    val fontsAndDialogs: String
    val haveCustomFont: String
    val fontFormatsSupported: String
    val addFont: String
    val defaultAppFonts: String
    val customFont: String
    val fontAppliedSuccess: String
    val fontLoadError: String
    val biometricSubtitle: String
    val conversationMovedToTrash: String
    val batchDeleteIrreversible: String
    val directTelegramContact: String
    val telegramAddressCopied: String
    val smartDefaultFolder: String
    val newRule: String
    val newTemplate: String
    val emptyTrashTitle: String
    val emptyTrashSubtitle: String
    val deselectAll: String
    val rulesEngineDesc: String
    val noRulesTitle: String
    val noRulesSubtitle: String
    val patternPrefix: String
    val autoMoveToFolder: String
    val rulePatternLabel: String
    val ruleExplanationLabel: String
    val targetFolderLabel: String
    val activeRulesCount: String
    val customFolder: String
    val folderNameFa: String
    val folderNameEn: String
    val foldersDesc: String
    val addFolder: String
    val blockSender: String
    val blockSenderAdded: String
    val deleteAllMessages: String
    val scheduledTimeHeader: String
    val autoRouteChatToFolder: String
    val ruleCreatedFromChat: String
    val routingRuleApplied: String
    val inSixHours: String
    val quickRepliesCount: String
    val quickRepliesDesc: String
    val noQuickRepliesTitle: String
    val noQuickRepliesSubtitle: String
    val addQuickReplyTitle: String
    val templateTitleLabel: String
    val templateContentLabel: String
    val templateCategoryLabel: String
    val scheduledSmsCount: String
    val noScheduledSmsTitle: String
    val noScheduledSmsSubtitle: String
    val trashMessagesCount: String
    val dateDeletedPrefix: String
    val restoreMessage: String
    val permanentDelete: String
    val blockedContactsCount: String
    val noBlockedContactsTitle: String
    val noBlockedContactsSubtitle: String
    val blockContactDesc: String
    val unblock: String
    val toLabel: String
    val newRecipientHint: String
    val chooseFromContacts: String
    val deviceContacts: String
    val openPhoneContacts: String
    val noContactsFound: String
    val sendWithSim: String
    val selectSimForSending: String
    val chooseSimToSend: String
    val activeSim: String
    val rememberForConversation: String
    val futureMessagesUseThisSim: String
    val bankTransactionBadge: String
    val copyOtpCodeBadge: String
    val otpCopiedToast: String
    val callSender: String
    val moreOptions: String
    val clearInput: String
    val persianEncoding: String
    val unicodeEncoding: String
    val scheduledToastSuccess: String
    val undo: String
    val conversationsSection: String
    val messagesSection: String
    val selectedConversationsCount: String
    val unreadCountSuffix: String
    val noSearchResultFor: String
    val noSearchResultSub: String
    val routeIncomingToFolder: String
    val ruleForSender: String
    val selectFolderToMove: String
    val moveSelectedConversations: String
    val targetFolderForCount: String
    val markAsRead: String
    val markAsUnread: String
    val deleteConversationAction: String
    val pinLabel: String
    val customFontNamed: String
    val resend: String
    val sendingStatus: String
    val failedToSend: String
    val messageDeletedWithUndo: String
    val openAppSettings: String
    val continueLater: String
    val swipeActionsHint: String
    val dismissHint: String

    // G2 - Pinned and Archive
    val maxPinnedLimitReached: String
    val archived: String
    val archive: String
    val unarchive: String
    val unarchiveOnNewMessage: String
    val unarchiveOnNewMessageDesc: String
    val mute: String
    val unmute: String
    val muteFor1Hour: String
    val muteFor8Hours: String
    val muteFor1Week: String
    val muteAlways: String

    // G4 - Drafts
    val draftPrefix: String
    val draftOnly: String

    // G5 - In-app Banner
    val newIncomingMessage: String
    val oneTimeCodeReceived: String

    // G6 - Global Search
    val searchContacts: String
    val searchConversations: String
    val searchMessages: String
    val noMessagesHit: String

    // G7 - Scheduled Screen
    val scheduledToday: String
    val scheduledLater: String
    val scheduledSent: String
    val scheduledNoticeDelay: String
    val editTime: String
    val cancelScheduleConfirm: String

    // G8 - Spam and Blocked Center
    val spamTab: String
    val blockedTab: String
    val notSpamAction: String
    val senderInSpamList: String
    val autoDeleteSpam: String
    val autoDeleteSpamNever: String
    val autoDeleteSpam7Days: String
    val autoDeleteSpam30Days: String

    // G9 - OTP Center
    val otpCenterTitle: String
    val otpWindowHours: String
    val copyOtp: String
    val otpReceivedAt: String
    val noOtpCodes: String

    // G10 - SIM
    val chooseSim: String

    // G11 - Contact Info Screen
    val contactInfoTitle: String
    val searchInConversation: String
    val totalMessagesStat: String
    val firstMessageStat: String
    val lastMessageStat: String
    val phoneNumbers: String
    val addToContacts: String
    val openInContacts: String

    // G12 - Settings Section
    val chatsAndNotificationsSection: String

    // Scheduled SMS Professional
    val scheduleSheetTitle: String
    val scheduleQuick10Min: String
    val scheduleQuick1Hour: String
    val scheduleQuickTonight20: String
    val scheduleQuickTomorrow09: String
    val scheduleQuickCustom: String
    val scheduleSendsAt: String
    val scheduleButtonConfirm: String
    val scheduleErrorRecipient: String
    val scheduleErrorBody: String
    val scheduleErrorTooSoon: String
    val scheduleErrorTooFar: String
    val scheduledSmsScheduledSuccess: String
    val viewAction: String
    val sectionToday: String
    val sectionLater: String
    val sectionHistory: String
    val statusPendingLabel: String
    val statusSendingLabel: String
    val statusSentLabel: String
    val statusFailedLabel: String
    val statusCancelledLabel: String
    val statusMissedLabel: String
    val sendNowAction: String
    val deleteFromHistoryAction: String
    val exactAlarmPermissionBanner: String
    val allowExactAlarmButton: String
    val scheduledForBubblePrefix: String
    val forceStopWarningNotice: String
    val batteryRestrictionTitle: String
    val batteryRestrictionBody: String
    val openSettingsButton: String
    val laterButton: String
    val dontShowAgainButton: String
    val batteryStatusTitle: String
    val batteryStatusUnrestricted: String
    val batteryStatusRestricted: String
    val scheduledFailedNotificationTitle: String
    val scheduledFailedNotificationBody: String

    // Folder Tab Actions
    val reorderFolders: String
    val editFolder: String
    val muteAll: String
    val markAllAsReadAction: String
    val deleteFolderAction: String
    val hideFolder: String
    val showHiddenFolders: String
    val otpMuteWarning: String
    val confirmMarkAllReadTitle: String
    val confirmMarkAllReadBody: String
    val confirmDeleteFolderTitle: String
    val confirmDeleteFolderBody: String
    val confirmHideFolderTitle: String
    val confirmHideFolderBody: String
    val conversationsAffectedSnackbar: String
    val folderRenamedSnackbar: String
    val folderDeletedSnackbar: String
    val folderHiddenSnackbar: String
    val unhideFolder: String
    val hiddenFoldersSection: String
    val noHiddenFolders: String
    val doneAction: String
    val muteDurationTitle: String
    val mute1Hour: String
    val mute8Hours: String
    val mute1Week: String

    // Rules Feature Strings
    val rulesMyRules: String
    val rulesReadyMade: String
    val rulesComingSoon: String
    val rulesNewRule: String
    val rulesNoRulesTitle: String
    val rulesFirstTimeHelp: String
    val rulesGotIt: String
    val rulesAdvancedRule: String
    val rulesStateOn: String
    val rulesStateOff: String
    val rulesConfirmDelete: String
    val rulesDeletedMessage: String
    val rulesUndoAction: String
    val rulesStep1Title: String
    val rulesChoiceContact: String
    val rulesChoiceNumberOrName: String
    val rulesChoiceAnyoneWithWord: String
    val rulesNumberOrNameHint: String
    val rulesStep2Title: String
    val rulesKeywordHint: String
    val rulesAddWord: String
    val rulesSkip: String
    val rulesStep3Title: String
    val rulesCreateNewFolder: String
    val rulesStepSummaryTitle: String
    val rulesAffectsConversations: String
    val rulesSave: String
    val rulesSaveAndApply: String
    val rulesConversationsMoved: String
    val rulesBack: String
    val rulesNext: String
    val rulesStepOfThree: String
    val rulesEditRule: String
    val rulesApplyOlderButton: String
    val rulesApplyOlderConfirmDialog: String
    val ruleSaveFailed: String
    val rulesMinKeywordLengthHint: String
    val rulesApplyFailed: String
    val notifChannelQuietName: String
    val notifChannelQuietDesc: String
    val notifChannelScheduledName: String
    val notifChannelScheduledDesc: String
    val scheduledSmsMissedNotification: String
    val scheduledSmsFailedNotification: String
    val messageNotSent: String
    val callContact: String
    val unread: String
    val otpReceivedBanner: String
    val newMessageBanner: String
    val templatesTabName: String
    val noTemplatesMessage: String
    val createTemplateButton: String
    val saveAsTemplate: String
    val templateAlreadyExists: String
    val deleteTemplateConfirm: String
    val importFromFile: String
    val templatesFoundFormat: String
    val addTemplatesDisabled: String
    val importInfoText: String
    val shareTemplatesButton: String
    val removePersonalNumbersCheckbox: String
    val removePersonalNumbersDesc: String
    val createFileAndSend: String
    val noTemplatesToShare: String
    val sort: String
    val newest: String
    val oldest: String
    val show: String
    val allMessages: String
    val order: String
    val quickReply: String
    val copy_code: String
    val copied: String
    val mark_as_read: String
    val code_copied: String
    val unknownNumber: String
    val addContact: String
    val block: String
    val searchInChat: String
    val searchLoadedOnly: String
    // Themes & Accents
    val themeClassic: String
    val themeAmoled: String
    val themeCyprus: String
    val themeBridal: String
    val themePumpkin: String
    val themeOcean: String
    val themeOlive: String
    val themeDynamic: String
    val switchToLight: String
    val switchToDark: String
    val accentColor: String
    val accentDefault: String
    val accentBlue: String
    val accentGreen: String
    val accentOrange: String
    val accentRed: String
    val accentPurple: String
    val accentTeal: String
    val accentPink: String
}

object PersianStrings : Strings {
    override val appName = "SMSGRAM"
    override val appSubtitle = "پیام‌رسان بومی تلگرامی پیامک"
    override val cellularSms = "پیامک مخابراتی"
    override val scrollToBottom = "رفتن به پایین"
    override val blockedNumbers = "شماره‌های مسدود شده"
    override val allChats = "همه پیام‌ها"
    override val personal = "شخصی"
    override val bank = "بانکی"
    override val advertising = "تبلیغات"
    override val services = "خدمات"
    override val savedMessages = "پیام‌های ذخیره شده"
    override val folders = "پوشه‌ها"
    override val scheduledSms = "پیام‌های زمان‌بندی شده"
    override val cannedTemplates = "پاسخ‌های آماده"
    override val rulesEngine = "قوانین هوشمند دسته‌بندی"
    override val trash = "سطل بازیافت"
    override val settings = "تنظیمات"
    override val language = "زبان برنامه"
    override val selectLanguage = "انتخاب زبان برنامه"
    override val nightMode = "حالت شب"
    override val dayMode = "حالت روز"
    override val defaultSmsApp = "برنامه پیامک پیش‌فرض"
    override val defaultSmsActive = "برنامه پیش‌فرض فعال است"
    override val tapToSetDefault = "برای تنظیم به عنوان پیش‌فرض ضربه بزنید"
    override val searchHint = "جستجو در پیام‌ها و مخاطبین…"
    override val noMessagesFound = "پیامکی یافت نشد"
    override val noMessagesSub = "برای ارسال اولین پیام، دکمه + را لمس کنید"
    override val newMessage = "پیام جدید"
    override val permissionRequired = "دسترسی‌های پیامک نیاز است"
    override val permissionDescription = "برای خواندن و ارسال پیامک، لطفاً دسترسی‌های لازم را اعطا کنید."
    override val grantPermissions = "اعطای دسترسی"
    override val setAsDefault = "تنظیم به عنوان پیش‌فرض"
    override val defaultPromptTitle = "پیام‌رسان پیش‌فرض پیامک"
    override val defaultPromptDesc = "برای دریافت و ارسال بدون وقفه پیامک‌ها، SMSGRAM را برنامه پیش‌فرض کنید."
    override val typeMessage = "نوشتن پیامک…"
    override val send = "ارسال"
    override val schedule = "زمان‌بندی ارسال"
    override val templates = "پاسخ‌های آماده"
    override val sim1 = "سیم ۱"
    override val sim2 = "سیم ۲"
    override val today = "امروز"
    override val yesterday = "دیروز"
    override val copyCode = "کپی کد تایید"
    override val copyCard = "کپی شماره کارت"
    override val call = "تماس"
    override val openLink = "باز کردن پیوند"
    override val codeCopied = "کد تایید کپی شد"
    override val cardCopied = "شماره کارت کپی شد"
    override val textCopied = "متن کپی شد"
    override val messageInfo = "اطلاعات پیام"
    override val sentAt = "زمان ارسال"
    override val deliveredAt = "زمان تحویل"
    override val statusSent = "ارسال شده"
    override val statusDelivered = "تحویل داده شده"
    override val statusPending = "در حال ارسال"
    override val statusFailed = "ناموفق"
    override val charsCount = "کاراکتر"
    override val partCount = "بخش"
    override val settingsTitle = "تنظیمات"
    override val languageSettings = "زبان (Language)"
    override val appearance = "ظاهر و پوسته"
    override val theme = "تم و رنگ‌بندی"
    override val fontSize = "اندازه قلم پیام‌ها"
    override val numbersAndDate = "اعداد و تقویم"
    override val persianDigits = "نمایش ارقام به فارسی (۱۲۳)"
    override val persianDigitsDesc = "تبدیل تمام اعداد، ساعت و شماره‌ها به فونت فارسی"
    override val calendarSystem = "تقویم و تاریخ برنامه"
    override val calendarShamsi = "شمسی (هجری خورشیدی)"
    override val calendarGregorian = "میلادی"
    override val calendarBoth = "هر دو (شمسی و میلادی)"
    override val deliveryReports = "گزارش تحویل پیامک (Delivery)"
    override val deliveryReportsDesc = "درخواست تایید تحویل پیامک از مخابرات"
    override val defaultSimCard = "سیم‌کارت پیش‌فرض ارسال"
    override val securityAndPrivacy = "امنیت و حریم خصوصی"
    override val appLock = "قفل برنامه با پین / اثر انگشت"
    override val appLockDesc = "درخواست رمز عبور هنگام باز کردن برنامه"
    override val notificationPrivacy = "حالت محرمانه اعلان‌ها"
    override val notificationPrivacyDesc = "مخفی کردن متن پیامک در صفحه قفل گوشی"
    override val autoDeleteOtp = "پاکسازی خودکار پیامک‌های رمز پویا"
    override val autoDeleteOtpDesc = "حذف هوشمند پیام‌های حاوی کد پس از ۲۴ ساعت"
    override val backupAndRestore = "پشتیبان‌گیری و بازیابی"
    override val exportJson = "خروجی پشتیبان (JSON)"
    override val exportJsonDesc = "پشتیبان‌گیری کامل از پوشه‌ها، قوانین و پاسخ‌های آماده"
    override val importJson = "بازیابی اطلاعات (JSON)"
    override val importJsonDesc = "بازگردانی فایل پشتیبان قبلی به شکل کاملاً آفلاین"
    override val aboutApp = "درباره SMSGRAM"
    override val version = "نسخه ۱.۰.۰ (آفلاین)"
    override val developerNote = "پیام‌رسان بومی پیامک با الهام از تجربه کاربری تلگرام"
    override val cancel = "انصراف"
    override val confirm = "تایید"
    override val save = "ذخیره"
    override val delete = "حذف"
    override val edit = "ویرایش"
    override val back = "بازگشت"
    override val close = "بستن"
    override val done = "انجام شد"
    override val success = "عملیات با موفقیت انجام شد"
    override val error = "خطایی رخ داد"
    override val enterPin = "رمز عبور را وارد کنید"
    override val setPin = "تعیین رمز عبور جدید"
    override val pinMismatch = "رمز وارد شده مطابقت ندارد"
    override val pinSaved = "رمز عبور با موفقیت تنظیم شد"
    override val permissionRequiredTitle = "دسترسی به پیامک‌ها مورد نیاز است"
    override val permissionRequiredDesc = "برای نمایش و ارسال پیامک‌های شما، برنامه نیازمند مجوز پیامک و مخاطبین است. اطلاعات شما ۱۰۰٪ در داخل دستگاه محفوظ می‌ماند."
    override val pin = "رمز عبور…"
    override val wrongPin = "رمز عبور نادرست است"
    override val unlock = "بازگشایی"
    override val biometricUnlock = "اثر انگشت"
    override val createRule = "ایجاد قانون فیلتر خودکار"
    override val deleteConversation = "حذف این گفتگو"
    override val pinnedMessage = "پیام سنجاق شده"
    override val unpin = "برداشتن سنجاق"
    override val scheduleSms = "زمان‌بندی ارسال پیامک"
    override val recipientNumber = "شماره گیرنده"
    override val messageBody = "متن پیامک (اختیاری)"
    override val startChat = "شروع گفتگو"
    override val select = "انتخاب"
    override val selectAll = "انتخاب همه"
    override val savedNotesTitle = "یادداشت‌های پیامکی"
    override val savedNotesSubtitle = "یادداشت‌ها و پیامک‌های ذخیره‌شده روی این دستگاه"
    override val noSavedNotes = "هنوز یادداشتی ذخیره نکرده‌اید"
    override val messagingCategory = "پیامک و ارسال"
    override val dataCategory = "داده‌ها و ذخیره‌سازی"
    override val onboardingWelcomeTitle = "به SMSGRAM خوش آمدید"
    override val onboardingWelcomeDesc = "مدیریت هوشمند پیامک‌ها با دسته‌بندی خودکار، امنیت بالا و رابط کاربری مدرن"
    override val clearTrash = "تخلیه سطل بازیافت"
    override val confirmClearTrash = "آیا از حذف دائمی تمام پیامک‌های سطل بازیافت اطمینان دارید؟"
    override val newNotePlaceholder = "متن یا یادداشت جدید…"
    override val changeSim = "تغییر سیم‌کارت"
    override val attachmentsAndTemplates = "پیوست / قالب‌ها"
    override val more = "بیشتر"
    override val carrierSms = "پیامک مخابراتی"
    override val tenMinutesLater = "۱۰ دقیقه بعد"
    override val thirtyMinutesLater = "۳۰ دقیقه بعد"
    override val oneHourLater = "۱ ساعت بعد"
    override val tomorrowSameTime = "فردا همین موقع"
    override val copyText = "کپی متن"
    override val pinConversation = "سنجاق گفتگو"
    override val unpinConversation = "برداشتن سنجاق"
    override val moveToFolder = "انتقال به پوشه"
    override val createFilterRule = "ایجاد فیلتر خودکار"
    override val setReminder = "تنظیم یادآور"
    override val deleteMessage = "حذف پیام"
    override val card = "کارت"
    override val shebaNumber = "شماره شبا"
    override val shebaCopied = "شماره شبا کپی شد 🏦"
    override val copySheba = "کپی شماره شبا"
    override val delivered = "تحویل داده شد"
    override val sent = "ارسال شد"
    override val selected = "انتخاب شده"
    override val notSelected = "انتخاب نشده"
    override val unknownMessage = "پیام ناشناس"
    override val dualSimManagement = "مدیریت دو سیم‌کارت"
    override val activeSimsOnDevice = "سیم‌کارت‌های فعال در دستگاه"
    override val invertSimSlots = "معکوس کردن سیم‌کارت‌ها (جابجایی سیم ۱ و ۲)"
    override val invertSimSlotsDesc = "در صورتی که گوشی شما سیم ۱ و ۲ را برعکس تشخیص می‌دهد، این گزینه را فعال کنید"
    override val contactsPermissionRequired = "دسترسی به مخاطبین برای انتخاب آسان شماره"
    override val grantContactsPermission = "فعال‌سازی دسترسی مخاطبین"
    override val grantPermission = "دادن دسترسی"
    override val searchContactHint = "جستجوی نام یا شماره مخاطب..."
    override val clearSearch = "پاک کردن"
    override val editSimName = "ویرایش نام"
    override val changeSimColor = "تغییر رنگ"
    override val defaultSimForSending = "سیم‌کارت پیش‌فرض برای ارسال پیامک"
    override val alwaysAskBeforeSending = "همیشه پیش از ارسال بپرس"
    override val manualSimSelectionDesc = "انتخاب دستی سیم‌کارت در هر پیام یا گفتگو"
    override val alwaysWithSim = "همیشه با %s"
    override val slotNumber = "اسلات شماره %d"
    override val simDisplayName = "نام نمایشی سیم‌کارت"
    override val enterCustomSimLabel = "یک برچسب دلخواه (مانند شخصی، کاری، شماره ۲) وارد کنید:"
    override val simNameSaved = "نام سیم‌کارت ذخیره شد ✅"
    override val selectSimColor = "انتخاب رنگ سیم‌کارت"
    override val simColorDesc = "رنگ مشخص‌کننده این سیم‌کارت در حباب‌ها و گفتگوها:"
    override val resetToDefaultColor = "بازنشانی به رنگ پیش‌فرض"
    override val fontsAndDialogs = "فونت برنامه و گفتگوها"
    override val haveCustomFont = "فونت دلخواه دارید؟"
    override val fontFormatsSupported = "فرمت‌های پشتیبانی‌شده: TTF و OTF (حداکثر ۱۰ مگابایت)"
    override val addFont = "+ افزودن فونت"
    override val defaultAppFonts = "فونت‌های پیش‌فرض برنامه"
    override val customFont = "فونت سفارشی"
    override val fontAppliedSuccess = "فونت '%s' با موفقیت بارگذاری و اعمال شد! ✨"
    override val fontLoadError = "خطا در بارگذاری فایل فونت. لطفاً فایل TTF معتبر انتخاب کنید."
    override val biometricSubtitle = "احراز هویت بیومتریک برای ورود به پیامک‌گرام"
    override val conversationMovedToTrash = "گفتگو به سطل زباله رفت"
    override val batchDeleteIrreversible = "آیا از حذف گفتگوهای انتخاب شده اطمینان دارید؟ پیام‌ها به سطل زباله منتقل می‌شوند و تا ۳۰ روز قابل بازیابی هستند."
    override val directTelegramContact = "ارتباط مستقیم در تلگرام"
    override val telegramAddressCopied = "آدرس تلگرام کپی شد: %s"
    override val smartDefaultFolder = "پوشه پیش‌فرض هوشمند"
    override val newRule = "قانون جدید"
    override val newTemplate = "الگوی جدید"
    override val emptyTrashTitle = "سطل بازیافت خالی است"
    override val emptyTrashSubtitle = "پیام‌های حذف شده در اینجا ذخیره می‌شوند و پس از ۳۰ روز به طور خودکار پاکسازی خواهند شد."
    override val deselectAll = "لغو همه"
    override val rulesEngineDesc = "قاعده‌های خودکار پیام‌ها را به پوشه‌های انتخابی شما منتقل می‌کنند."
    override val noRulesTitle = "هنوز قاعده‌ای تعریف نشده است"
    override val noRulesSubtitle = "با دکمهٔ بالا می‌توانید برای پوشه‌ها قاعده تعیین کنید."
    override val patternPrefix = "فرستنده"
    override val autoMoveToFolder = "انتقال خودکار به پوشه"
    override val rulePatternLabel = "شماره یا نام فرستنده (مثلاً: 1000)"
    override val ruleExplanationLabel = "توضیح قانون (اختیاری)"
    override val targetFolderLabel = "پوشه مقصد:"
    override val activeRulesCount = "قانون فعال"
    override val customFolder = "پوشه سفارشی"
    override val folderNameFa = "نام پوشه (فارسی)"
    override val folderNameEn = "نام انگلیسی (اختیاری)"
    override val foldersDesc = "پوشه‌ها به شما کمک می‌کنند پیام‌های بانکی، کدهای تایید، تبلیغاتی و شخصی را تفکیک کنید."
    override val addFolder = "افزودن پوشه جدید"
    override val blockSender = "مسدود کردن فرستنده (Blacklist)"
    override val blockSenderAdded = "%s به لیست مسدودها اضافه شد 🚫"
    override val deleteAllMessages = "حذف تمام پیام‌های گفتگو"
    override val scheduledTimeHeader = "زمان ارسال خودکار پیام:"
    override val autoRouteChatToFolder = "انتقال خودکار پیام‌های دریافتی از (%s) به پوشه:"
    override val ruleCreatedFromChat = "قانون ایجاد شده از گفتگو"
    override val routingRuleApplied = "قانون انتقال خودکار اعمال شد ✅"
    override val inSixHours = "۶ ساعت بعد"
    override val quickRepliesCount = "پاسخ آماده"
    override val quickRepliesDesc = "پاسخ‌های آماده برای ارسال سریع در گفتگوها"
    override val noQuickRepliesTitle = "هیچ پاسخ آماده‌ای تعریف نشده است"
    override val noQuickRepliesSubtitle = "با افزودن پاسخ‌های سریع می‌توانید در زمان خود صرفه‌جویی کنید."
    override val addQuickReplyTitle = "افزودن پاسخ آماده جدید"
    override val templateTitleLabel = "عنوان پاسخ (مثلاً: در جلسه)"
    override val templateContentLabel = "متن پاسخ پیامک"
    override val templateCategoryLabel = "دسته‌بندی (اختیاری)"
    override val scheduledSmsCount = "پیام زمان‌بندی شده"
    override val noScheduledSmsTitle = "هیچ پیام زمان‌بندی شده‌ای وجود ندارد"
    override val noScheduledSmsSubtitle = "هنگام ارسال پیامک می‌توانید زمان ارسال را برای آینده تعیین کنید."
    override val trashMessagesCount = "پیام"
    override val dateDeletedPrefix = "تاریخ حذف: "
    override val restoreMessage = "بازیابی پیام"
    override val permanentDelete = "حذف دائمی"
    override val blockedContactsCount = "شماره"
    override val noBlockedContactsTitle = "هیچ شماره مسدود شده‌ای وجود ندارد"
    override val noBlockedContactsSubtitle = "شماره‌های مسدود شده به صورت خودکار رد خواهند شد."
    override val blockContactDesc = "شماره‌ها و سرشماره‌های مسدود شده در اینجا نمایش داده می‌شوند."
    override val unblock = "رفع مسدودی"
    override val toLabel = "به:"
    override val newRecipientHint = "شماره همراه یا نام مخاطب..."
    override val chooseFromContacts = "انتخاب از مخاطبین"
    override val deviceContacts = "مخاطبین دستگاه"
    override val openPhoneContacts = "باز کردن دفترچه تلفن گوشی..."
    override val noContactsFound = "مخاطبی یافت نشد"
    override val sendWithSim = "ارسال با سیم‌کارت:"
    override val selectSimForSending = "انتخاب سیم‌کارت ارسال"
    override val chooseSimToSend = "سیم‌کارت مورد نظر برای ارسال پیام را انتخاب کنید:"
    override val activeSim = "فعال"
    override val rememberForConversation = "به‌خاطر سپردن برای این گفتگو"
    override val futureMessagesUseThisSim = "پیام‌های بعدی به این مخاطب همواره با این سیم‌کارت ارسال شوند"
    override val bankTransactionBadge = "پیامک بانکی و تراکنش"
    override val copyOtpCodeBadge = "کپی کد تایید: %s"
    override val otpCopiedToast = "کد تایید کپی شد: %s 📋"
    override val callSender = "تماس"
    override val moreOptions = "بیشتر"
    override val clearInput = "پاک کردن"
    override val persianEncoding = "فارسی"
    override val unicodeEncoding = "Unicode"
    override val scheduledToastSuccess = "پیام برای ارسال در زمان مشخص زمان‌بندی شد 📅"
    override val undo = "بازگردانی"
    override val conversationsSection = "گفتگوها"
    override val messagesSection = "پیام‌ها"
    override val selectedConversationsCount = "%s گفتگو انتخاب شد"
    override val unreadCountSuffix = "خوانده‌نشده"
    override val noSearchResultFor = "نتیجه‌ای برای «%s» پیدا نشد"
    override val noSearchResultSub = "لطفاً عبارت دیگری را جستجو کنید"
    override val routeIncomingToFolder = "پیام‌های دریافتی از (%s) به کدام پوشه هدایت شوند؟"
    override val ruleForSender = "قانون برای %s"
    override val selectFolderToMove = "یک پوشه برای انتقال این گفتگو انتخاب کنید:"
    override val moveSelectedConversations = "انتقال گفتگوهای انتخاب‌شده"
    override val targetFolderForCount = "پوشه مقصد برای %s گفتگوی انتخاب شده:"
    override val markAsRead = "علامت‌گذاری به‌عنوان خوانده‌شده"
    override val markAsUnread = "علامت‌گذاری به‌عنوان خوانده‌نشده"
    override val deleteConversationAction = "حذف گفتگو"
    override val pinLabel = "سنجاق"
    override val customFontNamed = "سفارشی: %s"
    override val resend = "ارسال مجدد"
    override val sendingStatus = "در حال ارسال…"
    override val failedToSend = "ارسال ناموفق"
    override val messageDeletedWithUndo = "پیام حذف شد"
    override val openAppSettings = "باز کردن تنظیمات برنامه"
    override val continueLater = "بعداً"
    override val swipeActionsHint = "راهنما: کشیدن به راست برای خوانده‌شده و به چپ برای حذف سریع با بازگردانی است."
    override val dismissHint = "متوجه شدم"

    // G2 - Pinned and Archive
    override val maxPinnedLimitReached = "حداکثر ۵ گفتگوی سنجاق‌شده"
    override val archived = "آرشیو"
    override val archive = "آرشیو"
    override val unarchive = "خروج از آرشیو"
    override val unarchiveOnNewMessage = "خروج از آرشیو با پیام جدید"
    override val unarchiveOnNewMessageDesc = "با دریافت پیام جدید، گفتگو خودکار از آرشیو خارج شود"
    override val mute = "بی‌صدا کردن"
    override val unmute = "با صدا کردن"
    override val muteFor1Hour = "۱ ساعت"
    override val muteFor8Hours = "۸ ساعت"
    override val muteFor1Week = "۱ هفته"
    override val muteAlways = "برای همیشه"

    // G4 - Drafts
    override val draftPrefix = "پیش‌نویس: "
    override val draftOnly = "پیش‌نویس"

    // G5 - In-app Banner
    override val newIncomingMessage = "پیام جدید"
    override val oneTimeCodeReceived = "کد یک‌بارمصرف دریافت شد"

    // G6 - Global Search
    override val searchContacts = "مخاطبین"
    override val searchConversations = "گفتگوها"
    override val searchMessages = "پیام‌ها"
    override val noMessagesHit = "پیامی یافت نشد"

    // G7 - Scheduled Screen
    override val scheduledToday = "امروز"
    override val scheduledLater = "بعداً"
    override val scheduledSent = "ارسال‌شده"
    override val scheduledNoticeDelay = "زمان‌بندی ممکن است چند دقیقه تأخیر داشته باشد"
    override val editTime = "ویرایش زمان"
    override val cancelScheduleConfirm = "آیا از لغو این پیام زمان‌بندی‌شده اطمینان دارید؟"

    // G8 - Spam and Blocked Center
    override val spamTab = "اسپم"
    override val blockedTab = "مسدودها"
    override val notSpamAction = "این اسپم نیست"
    override val senderInSpamList = "فرستنده در لیست اسپم"
    override val autoDeleteSpam = "حذف خودکار اسپم بعد از"
    override val autoDeleteSpamNever = "هرگز"
    override val autoDeleteSpam7Days = "۷ روز"
    override val autoDeleteSpam30Days = "۳۰ روز"

    // G9 - OTP Center
    override val otpCenterTitle = "مرکز کدهای یکبارمصرف"
    override val otpWindowHours = "بازه زمانی کدهای OTP"
    override val copyOtp = "کپی کد"
    override val otpReceivedAt = "دریافت شده در %s"
    override val noOtpCodes = "هیچ کد یکبارمصرفی در این بازه یافت نشد"

    // G10 - SIM
    override val chooseSim = "انتخاب سیم‌کارت"

    // G11 - Contact Info Screen
    override val contactInfoTitle = "اطلاعات تماس"
    override val searchInConversation = "جستجو در این گفتگو"
    override val totalMessagesStat = "تعداد پیام‌ها"
    override val firstMessageStat = "اولین پیام"
    override val lastMessageStat = "آخرین پیام"
    override val phoneNumbers = "شماره‌ها"
    override val addToContacts = "افزودن به مخاطبین"
    override val openInContacts = "نمایش در مخاطبین"

    // G12 - Settings Section
    override val chatsAndNotificationsSection = "گفتگوها و اعلان‌ها"

    // Scheduled SMS Professional
    override val scheduleSheetTitle = "زمان‌بندی پیام"
    override val scheduleQuick10Min = "۱۰ دقیقه"
    override val scheduleQuick1Hour = "۱ ساعت"
    override val scheduleQuickTonight20 = "امشب ۲۰:۰۰"
    override val scheduleQuickTomorrow09 = "فردا ۰۹:۰۰"
    override val scheduleQuickCustom = "انتخاب دستی"
    override val scheduleSendsAt = "ارسال: %s ساعت %s"
    override val scheduleButtonConfirm = "زمان‌بندی"
    override val scheduleErrorRecipient = "گیرنده نامعتبر است"
    override val scheduleErrorBody = "متن پیام نمی‌تواند خالی باشد"
    override val scheduleErrorTooSoon = "زمان ارسال باید حداقل ۱ دقیقه جلوتر باشد"
    override val scheduleErrorTooFar = "زمان ارسال نمی‌تواند بیش از ۱ سال آینده باشد"
    override val scheduledSmsScheduledSuccess = "پیام زمان‌بندی شد"
    override val viewAction = "مشاهده"
    override val sectionToday = "امروز"
    override val sectionLater = "بعداً"
    override val sectionHistory = "قبلی"
    override val statusPendingLabel = "در انتظار"
    override val statusSendingLabel = "در حال ارسال"
    override val statusSentLabel = "ارسال شد"
    override val statusFailedLabel = "ناموفق"
    override val statusCancelledLabel = "لغو شده"
    override val statusMissedLabel = "از دست رفته"
    override val sendNowAction = "ارسال فوری"
    override val deleteFromHistoryAction = "حذف از تاریخچه"
    override val exactAlarmPermissionBanner = "برای ارسال دقیق، اجازهٔ آلارم دقیق را فعال کنید"
    override val allowExactAlarmButton = "فعال‌سازی آلارم دقیق"
    override val scheduledForBubblePrefix = "برای %s"
    override val forceStopWarningNotice = "اگر برنامه را از تنظیمات Force Stop کنید، تا زمانی که دوباره باز نکنید پیام‌های زمان‌بندی‌شده ارسال نمی‌شوند."
    override val batteryRestrictionTitle = "برای ارسال دقیق، محدودیت باتری را برداریم؟"
    override val batteryRestrictionBody = "گوشی‌های برخی برندها برنامه‌ها را بیشتر از حد معمول متوقف می‌کنند."
    override val openSettingsButton = "باز کردن تنظیمات"
    override val laterButton = "بعداً"
    override val dontShowAgainButton = "دیگر نمایش نده"
    override val batteryStatusTitle = "وضعیت باتری"
    override val batteryStatusUnrestricted = "بدون محدودیت"
    override val batteryStatusRestricted = "محدود"
    override val scheduledFailedNotificationTitle = "پیام زمان‌بندی‌شده ارسال نشد"
    override val scheduledFailedNotificationBody = "یک پیام زمان‌بندی‌شده به دلیل خطا ارسال نشد"

    // Folder Tab Actions
    override val reorderFolders = "تغییر ترتیب"
    override val editFolder = "ویرایش پوشه"
    override val muteAll = "بی‌صدا کردن همه"
    override val markAllAsReadAction = "علامت‌گذاری همه به عنوان خوانده شده"
    override val deleteFolderAction = "حذف پوشه"
    override val hideFolder = "پنهان کردن پوشه"
    override val showHiddenFolders = "نمایش پوشه‌های پنهان"
    override val otpMuteWarning = "کدهای یکبارمصرف هم بیصدا میشوند."
    override val confirmMarkAllReadTitle = "علامت‌گذاری همه به عنوان خوانده شده"
    override val confirmMarkAllReadBody = "آیا از علامت‌گذاری تمام گفتگوهای این پوشه به عنوان خوانده شده اطمینان دارید؟"
    override val confirmDeleteFolderTitle = "حذف پوشه"
    override val confirmDeleteFolderBody = "با حذف این پوشه، گفتگوها و پیام‌های آن حذف نخواهند شد و به پوشه 'همه' بازمی‌گردند."
    override val confirmHideFolderTitle = "پنهان کردن پوشه"
    override val confirmHideFolderBody = "این پوشه سیستمی از نوار پوشه‌ها پنهان می‌شود. می‌توانید آن را از تنظیمات بازگردانید."
    override val conversationsAffectedSnackbar = "%d گفتگو به‌روزرسانی شد"
    override val folderRenamedSnackbar = "نام پوشه به‌روزرسانی شد"
    override val folderDeletedSnackbar = "پوشه حذف شد"
    override val folderHiddenSnackbar = "پوشه پنهان شد"
    override val unhideFolder = "نمایش مجدد"
    override val hiddenFoldersSection = "پوشه‌های پنهان"
    override val noHiddenFolders = "پوشه پنهانی وجود ندارد"
    override val doneAction = "پایان"
    override val muteDurationTitle = "بی‌صدا کردن به مدت..."
    override val mute1Hour = "۱ ساعت"
    override val mute8Hours = "۸ ساعت"
    override val mute1Week = "۱ هفته"

    // Rules Feature Strings
    override val rulesMyRules = "قاعده‌های من"
    override val rulesReadyMade = "آماده"
    override val rulesComingSoon = "به‌زودی"
    override val rulesNewRule = "قاعدهٔ جدید"
    override val rulesNoRulesTitle = "هنوز قاعده‌ای نساخته‌اید. با دکمهٔ بالا شروع کنید."
    override val rulesFirstTimeHelp = "قاعده یعنی: پیام‌هایی که فرستنده یا کلمهٔ خاصی دارند، به پوشهٔ مشخص بروند."
    override val rulesGotIt = "متوجه شدم"
    override val rulesAdvancedRule = "قاعدهٔ پیشرفته"
    override val rulesStateOn = "روشن"
    override val rulesStateOff = "خاموش"
    override val rulesConfirmDelete = "آیا از حذف این قاعده مطمئن هستید؟"
    override val rulesDeletedMessage = "قاعده حذف شد"
    override val rulesUndoAction = "بازگردانی"
    override val rulesStep1Title = "از چه کسی؟"
    override val rulesChoiceContact = "از یکی از مخاطبین"
    override val rulesChoiceNumberOrName = "از یک شماره یا نام"
    override val rulesChoiceAnyoneWithWord = "هر کسی، با یک کلمه"
    override val rulesNumberOrNameHint = "مثلاً: Snapp یا 1000"
    override val rulesStep2Title = "اگر پیام این کلمه‌ها را داشت"
    override val rulesKeywordHint = "مثلاً: فاکتور، پرداخت"
    override val rulesAddWord = "افزودن"
    override val rulesSkip = "رد کردن"
    override val rulesStep3Title = "به کدام پوشه برود؟"
    override val rulesCreateNewFolder = "ساخت پوشهٔ جدید"
    override val rulesStepSummaryTitle = "بررسی و ذخیره"
    override val rulesAffectsConversations = "این قاعده روی %s گفتگو اثر دارد."
    override val rulesSave = "ذخیره"
    override val rulesSaveAndApply = "ذخیره و اعمال روی پیام‌های قبلی"
    override val rulesConversationsMoved = "%s گفتگو جابه‌جا شد"
    override val rulesBack = "قبلی"
    override val rulesNext = "بعدی"
    override val rulesStepOfThree = "%s از ۳"
    override val rulesEditRule = "ویرایش قاعده"
    override val rulesApplyOlderButton = "اعمال قواعد روی پیام‌های قبلی"
    override val rulesApplyOlderConfirmDialog = "قواعد روی پیام‌های قبلی اجرا می‌شوند. پوشهٔ گفتگوهایی که دستی انتخاب کرده‌اید تغییر نمی‌کند."
    override val ruleSaveFailed = "ذخیرهٔ قاعده انجام نشد"
    override val rulesMinKeywordLengthHint = "حداقل دو حرف"
    override val rulesApplyFailed = "انجام نشد، دوباره تلاش کنید"
    override val notifChannelQuietName = "پیام‌های بی‌صدا"
    override val notifChannelQuietDesc = "اعلان‌های گفتگوهای بی‌صدا شده"
    override val notifChannelScheduledName = "پیام‌های زمان‌بندی‌شده"
    override val notifChannelScheduledDesc = "اعلان‌های وضعیت پیامک‌های زمان‌بندی‌شده"
    override val scheduledSmsMissedNotification = "%s پیام زمان‌بندی‌شده ارسال نشد"
    override val scheduledSmsFailedNotification = "پیام زمان‌بندی‌شده ارسال نشد"
    override val messageNotSent = "ارسال نشد"
    override val callContact = "تماس"
    override val unread = "خوانده‌نشده"
    override val otpReceivedBanner = "کد یک‌بارمصرف دریافت شد"
    override val newMessageBanner = "پیام جدید"
    override val templatesTabName = "قالب‌ها"
    override val noTemplatesMessage = "هنوز قالبی ندارید. با دکمهٔ «ساخت قالب» شروع کنید."
    override val createTemplateButton = "ساخت قالب"
    override val saveAsTemplate = "ذخیره به‌عنوان قالب"
    override val templateAlreadyExists = "این قالب قبلاً ذخیره شده است"
    override val deleteTemplateConfirm = "حذف شود؟"
    override val importFromFile = "وارد کردن از فایل"
    override val templatesFoundFormat = "%d قالب پیدا شد"
    override val addTemplatesDisabled = "افزودن (خاموش)"
    override val importInfoText = "این قالب‌ها خاموش اضافه می‌شوند. هر کدام را جدا روشن کنید."
    override val shareTemplatesButton = "اشتراک‌گذاری"
    override val removePersonalNumbersCheckbox = "حذف شماره‌های شخصی"
    override val removePersonalNumbersDesc = "شماره موبایل‌ها از فایل حذف می‌شوند تا حریم خصوصی حفظ شود."
    override val createFileAndSend = "ساخت فایل و ارسال"
    override val noTemplatesToShare = "قالبی برای اشتراک‌گذاری نیست"
    override val sort = "مرتب‌سازی"
    override val newest = "جدیدترین"
    override val oldest = "قدیمی‌ترین"
    override val show = "نمایش"
    override val allMessages = "همه پیام‌ها"
    override val order = "ترتیب"
    override val quickReply = "پاسخ سریع"
    override val copy_code = "کپی کد"
    override val copied = "کپی شد"
    override val mark_as_read = "خوانده‌شده"
    override val code_copied = "کد کپی شد"
    override val unknownNumber = "شماره ناشناس"
    override val addContact = "افزودن به مخاطبین"
    override val block = "مسدود"
    override val searchInChat = "جستجو در گفتگو"
    override val searchLoadedOnly = "جستجو فقط روی پیام‌های بارگذاری‌شده"
    override val themeClassic = "تلگرامی کلاسیک"
    override val themeAmoled = "مشکی خالص (AMOLED)"
    override val themeCyprus = "قبرسی و شنی"
    override val themeBridal = "شرابی و هلویی"
    override val themePumpkin = "کدو تنبل و زغالی"
    override val themeOcean = "اقیانوسی و ابری"
    override val themeOlive = "زیتونی و مریم‌گلی"
    override val themeDynamic = "رنگ پویای سیستم"
    override val switchToLight = "حالت روشن"
    override val switchToDark = "حالت تیره"
    override val accentColor = "رنگ تاکیدی"
    override val accentDefault = "پیش‌فرض"
    override val accentBlue = "آبی"
    override val accentGreen = "سبز"
    override val accentOrange = "نارنجی"
    override val accentRed = "قرمز"
    override val accentPurple = "بنفش"
    override val accentTeal = "فیروزه‌ای"
    override val accentPink = "صورتی"
}

object EnglishStrings : Strings {
    override val appName = "SMSGRAM"
    override val appSubtitle = "Native Telegram SMS Messenger"
    override val cellularSms = "Cellular SMS"
    override val scrollToBottom = "Scroll to bottom"
    override val blockedNumbers = "Blocked Numbers"
    override val allChats = "All Chats"
    override val personal = "Personal"
    override val bank = "Banking"
    override val advertising = "Ads & Promo"
    override val services = "Services"
    override val savedMessages = "Saved Messages"
    override val folders = "Folders"
    override val scheduledSms = "Scheduled SMS"
    override val cannedTemplates = "Quick Replies"
    override val rulesEngine = "Routing Rules"
    override val trash = "Trash Bin"
    override val settings = "Settings"
    override val language = "App Language"
    override val selectLanguage = "Select Language"
    override val nightMode = "Night Mode"
    override val dayMode = "Day Mode"
    override val defaultSmsApp = "Default SMS App"
    override val defaultSmsActive = "Default app is active"
    override val tapToSetDefault = "Tap to set as default"
    override val searchHint = "Search messages and contacts..."
    override val noMessagesFound = "No messages found"
    override val noMessagesSub = "Tap the + button to compose a new message"
    override val newMessage = "New Message"
    override val permissionRequired = "SMS Permissions Required"
    override val permissionDescription = "Please grant SMS permissions to read and send messages."
    override val grantPermissions = "Grant Permission"
    override val setAsDefault = "Set as Default"
    override val defaultPromptTitle = "Default SMS Messenger"
    override val defaultPromptDesc = "Set SMSGRAM as default to enjoy seamless SMS delivery."
    override val typeMessage = "Type an SMS..."
    override val send = "Send"
    override val schedule = "Schedule SMS"
    override val templates = "Quick Replies"
    override val sim1 = "SIM 1"
    override val sim2 = "SIM 2"
    override val today = "Today"
    override val yesterday = "Yesterday"
    override val copyCode = "Copy OTP Code"
    override val copyCard = "Copy Card Number"
    override val call = "Call"
    override val openLink = "Open Link"
    override val codeCopied = "OTP code copied"
    override val cardCopied = "Card number copied"
    override val textCopied = "Text copied"
    override val messageInfo = "Message Details"
    override val sentAt = "Sent At"
    override val deliveredAt = "Delivered At"
    override val statusSent = "Sent"
    override val statusDelivered = "Delivered"
    override val statusPending = "Sending..."
    override val statusFailed = "Failed"
    override val charsCount = "Chars"
    override val partCount = "Part"
    override val settingsTitle = "Settings"
    override val languageSettings = "Language"
    override val appearance = "Appearance & Theme"
    override val theme = "Theme & Colors"
    override val fontSize = "Message Font Size"
    override val numbersAndDate = "Numbers & Calendar"
    override val persianDigits = "Persian Numerals (۱۲۳)"
    override val persianDigitsDesc = "Format all digits and times in Persian script"
    override val calendarSystem = "App Calendar System"
    override val calendarShamsi = "Shamsi (Persian)"
    override val calendarGregorian = "Gregorian"
    override val calendarBoth = "Both (Shamsi & Gregorian)"
    override val deliveryReports = "Delivery Reports"
    override val deliveryReportsDesc = "Request delivery confirmation from carrier"
    override val defaultSimCard = "Default SIM for Sending"
    override val securityAndPrivacy = "Security & Privacy"
    override val appLock = "App Lock (PIN / Biometric)"
    override val appLockDesc = "Require passcode or fingerprint to open SMSGRAM"
    override val notificationPrivacy = "Notification Privacy"
    override val notificationPrivacyDesc = "Hide message preview on lock screen"
    override val autoDeleteOtp = "Auto-delete OTP Messages"
    override val autoDeleteOtpDesc = "Automatically purge verification codes after 24h"
    override val backupAndRestore = "Backup & Restore"
    override val exportJson = "Export Backup (JSON)"
    override val exportJsonDesc = "Full offline backup of folders, rules and templates"
    override val importJson = "Import Backup (JSON)"
    override val importJsonDesc = "Restore previous backup file 100% offline"
    override val aboutApp = "About SMSGRAM"
    override val version = "Version 1.0.0 (Offline)"
    override val developerNote = "Native Android SMS Messenger inspired by Telegram UX"
    override val cancel = "Cancel"
    override val confirm = "Confirm"
    override val save = "Save"
    override val delete = "Delete"
    override val edit = "Edit"
    override val back = "Back"
    override val close = "Close"
    override val done = "Done"
    override val success = "Operation completed successfully"
    override val error = "An error occurred"
    override val enterPin = "Enter Passcode"
    override val setPin = "Set New Passcode"
    override val pinMismatch = "PIN codes do not match"
    override val pinSaved = "PIN saved successfully"
    override val permissionRequiredTitle = "SMS Permissions Required"
    override val permissionRequiredDesc = "SMSGRAM requires SMS and Contacts permissions to display and send your messages. Your personal data stays 100% on your device."
    override val pin = "Passcode..."
    override val wrongPin = "Incorrect passcode"
    override val unlock = "Unlock"
    override val biometricUnlock = "Biometrics"
    override val createRule = "Create Routing Rule"
    override val deleteConversation = "Delete Conversation"
    override val pinnedMessage = "Pinned Message"
    override val unpin = "Unpin"
    override val scheduleSms = "Schedule SMS"
    override val recipientNumber = "Recipient Number"
    override val messageBody = "Message Text (Optional)"
    override val startChat = "Start Chat"
    override val select = "Select"
    override val selectAll = "Select All"
    override val savedNotesTitle = "Saved Notes"
    override val savedNotesSubtitle = "Notes and saved messages stored locally on this device"
    override val noSavedNotes = "No saved notes yet"
    override val messagingCategory = "Messaging & Delivery"
    override val dataCategory = "Data & Storage"
    override val onboardingWelcomeTitle = "Welcome to SMSGRAM"
    override val onboardingWelcomeDesc = "Smart SMS management with automatic categorization, privacy, and modern design"
    override val clearTrash = "Empty Trash"
    override val confirmClearTrash = "Are you sure you want to permanently delete all messages in Trash?"
    override val newNotePlaceholder = "New note or text..."
    override val changeSim = "Switch SIM"
    override val attachmentsAndTemplates = "Attachments / Templates"
    override val more = "More"
    override val carrierSms = "Carrier SMS"
    override val tenMinutesLater = "In 10 minutes"
    override val thirtyMinutesLater = "In 30 minutes"
    override val oneHourLater = "In 1 hour"
    override val tomorrowSameTime = "Tomorrow at this time"
    override val copyText = "Copy Text"
    override val pinConversation = "Pin Conversation"
    override val unpinConversation = "Unpin Conversation"
    override val moveToFolder = "Move to Folder"
    override val createFilterRule = "Create Filter Rule"
    override val setReminder = "Set Reminder"
    override val deleteMessage = "Delete Message"
    override val card = "Card"
    override val shebaNumber = "IBAN / Sheba"
    override val shebaCopied = "IBAN copied 🏦"
    override val copySheba = "Copy IBAN"
    override val delivered = "Delivered"
    override val sent = "Sent"
    override val selected = "Selected"
    override val notSelected = "Not Selected"
    override val unknownMessage = "Unknown Message"
    override val dualSimManagement = "Dual SIM Management"
    override val activeSimsOnDevice = "Active SIMs on Device"
    override val invertSimSlots = "Invert SIM 1 & SIM 2"
    override val invertSimSlotsDesc = "Enable this if your device reverses SIM 1 and SIM 2 detection"
    override val contactsPermissionRequired = "Contacts access to easily pick phone numbers"
    override val grantContactsPermission = "Grant Contacts Permission"
    override val grantPermission = "Grant Access"
    override val searchContactHint = "Search contact name or number..."
    override val clearSearch = "Clear"
    override val editSimName = "Edit Name"
    override val changeSimColor = "Change Color"
    override val defaultSimForSending = "Default SIM for Sending SMS"
    override val alwaysAskBeforeSending = "Always ask before sending"
    override val manualSimSelectionDesc = "Manually select SIM for each message or chat"
    override val alwaysWithSim = "Always with %s"
    override val slotNumber = "Slot %d"
    override val simDisplayName = "SIM Display Name"
    override val enterCustomSimLabel = "Enter a custom label (e.g. Personal, Work, SIM 2):"
    override val simNameSaved = "SIM name saved ✅"
    override val selectSimColor = "Select SIM Color"
    override val simColorDesc = "Color indicator for this SIM in message bubbles:"
    override val resetToDefaultColor = "Reset to default color"
    override val fontsAndDialogs = "App & Chat Fonts"
    override val haveCustomFont = "Have a custom font?"
    override val fontFormatsSupported = "Supported formats: TTF and OTF (max 10MB)"
    override val addFont = "+ Add Font"
    override val defaultAppFonts = "Default App Fonts"
    override val customFont = "Custom Font"
    override val fontAppliedSuccess = "Font '%s' successfully loaded and applied! ✨"
    override val fontLoadError = "Error loading font file. Please select a valid TTF/OTF file."
    override val biometricSubtitle = "Biometric authentication to unlock SMSGRAM"
    override val conversationMovedToTrash = "Conversation moved to Trash"
    override val batchDeleteIrreversible = "Are you sure you want to delete the selected conversations? Messages will be moved to Trash and kept for 30 days."
    override val directTelegramContact = "Direct Telegram Support"
    override val telegramAddressCopied = "Telegram link copied: %s"
    override val smartDefaultFolder = "Smart Default Folder"
    override val newRule = "New Rule"
    override val newTemplate = "New Template"
    override val emptyTrashTitle = "Trash Bin is Empty"
    override val emptyTrashSubtitle = "Deleted messages appear here and will be permanently purged after 30 days."
    override val deselectAll = "Deselect All"
    override val rulesEngineDesc = "Automatic rules route incoming messages to your chosen folders."
    override val noRulesTitle = "No Routing Rules Configured"
    override val noRulesSubtitle = "Tap the add button to configure rules for folders."
    override val patternPrefix = "Sender"
    override val autoMoveToFolder = "Auto-route to folder"
    override val rulePatternLabel = "Sender number or name (e.g. 1000)"
    override val ruleExplanationLabel = "Rule description (Optional)"
    override val targetFolderLabel = "Target Folder:"
    override val activeRulesCount = "active rules"
    override val customFolder = "Custom Folder"
    override val folderNameFa = "Folder Name (Persian)"
    override val folderNameEn = "Folder Name (English)"
    override val foldersDesc = "Folders help you organize banking, OTP verification, ads, and personal messages."
    override val addFolder = "Add New Folder"
    override val blockSender = "Block Sender (Blacklist)"
    override val blockSenderAdded = "%s added to blacklist 🚫"
    override val deleteAllMessages = "Delete All Messages"
    override val scheduledTimeHeader = "Scheduled send time:"
    override val autoRouteChatToFolder = "Automatically route messages from (%s) to folder:"
    override val ruleCreatedFromChat = "Rule created from chat"
    override val routingRuleApplied = "Routing rule applied ✅"
    override val inSixHours = "In 6 hours"
    override val quickRepliesCount = "quick replies"
    override val quickRepliesDesc = "Canned quick replies for rapid messaging in conversations"
    override val noQuickRepliesTitle = "No Quick Replies Configured"
    override val noQuickRepliesSubtitle = "Save time by adding pre-written messages and canned responses."
    override val addQuickReplyTitle = "Add Quick Reply"
    override val templateTitleLabel = "Reply Title (e.g., In a meeting)"
    override val templateContentLabel = "SMS Message Content"
    override val templateCategoryLabel = "Category (Optional)"
    override val scheduledSmsCount = "scheduled SMS"
    override val noScheduledSmsTitle = "No Scheduled Messages"
    override val noScheduledSmsSubtitle = "You can schedule messages to be sent automatically at a future time."
    override val trashMessagesCount = "messages"
    override val dateDeletedPrefix = "Deleted at: "
    override val restoreMessage = "Restore Message"
    override val permanentDelete = "Delete Permanently"
    override val blockedContactsCount = "numbers"
    override val noBlockedContactsTitle = "No Blocked Contacts"
    override val noBlockedContactsSubtitle = "Incoming messages from blocked numbers will be automatically filtered."
    override val blockContactDesc = "Blocked numbers and sender prefixes are listed here."
    override val unblock = "Unblock"
    override val toLabel = "To:"
    override val newRecipientHint = "Phone number or contact name..."
    override val chooseFromContacts = "Choose from Contacts"
    override val deviceContacts = "Device Contacts"
    override val openPhoneContacts = "Open phone contacts..."
    override val noContactsFound = "No contacts found"
    override val sendWithSim = "Send with SIM:"
    override val selectSimForSending = "Select SIM Card"
    override val chooseSimToSend = "Choose the SIM card to use for sending this SMS:"
    override val activeSim = "Active"
    override val rememberForConversation = "Remember for this conversation"
    override val futureMessagesUseThisSim = "Future messages to this contact will automatically use this SIM"
    override val bankTransactionBadge = "Banking & Transaction SMS"
    override val copyOtpCodeBadge = "Copy OTP Code: %s"
    override val otpCopiedToast = "Verification code copied: %s 📋"
    override val callSender = "Call"
    override val moreOptions = "More"
    override val clearInput = "Clear"
    override val persianEncoding = "Persian"
    override val unicodeEncoding = "Unicode"
    override val scheduledToastSuccess = "SMS scheduled successfully 📅"
    override val undo = "Undo"
    override val conversationsSection = "Conversations"
    override val messagesSection = "Messages"
    override val selectedConversationsCount = "%s conversations selected"
    override val unreadCountSuffix = "unread"
    override val noSearchResultFor = "No results found for \"%s\""
    override val noSearchResultSub = "Please try searching for another term"
    override val routeIncomingToFolder = "Route incoming messages from (%s) to which folder?"
    override val ruleForSender = "Rule for %s"
    override val selectFolderToMove = "Select a folder to move this conversation to:"
    override val moveSelectedConversations = "Move Selected Conversations"
    override val targetFolderForCount = "Target folder for %s selected conversation(s):"
    override val markAsRead = "Mark as read"
    override val markAsUnread = "Mark as unread"
    override val deleteConversationAction = "Delete conversation"
    override val pinLabel = "Pin"
    override val customFontNamed = "Custom: %s"
    override val resend = "Resend"
    override val sendingStatus = "Sending…"
    override val failedToSend = "Failed to send"
    override val messageDeletedWithUndo = "Message deleted"
    override val openAppSettings = "Open App Settings"
    override val continueLater = "Later"
    override val swipeActionsHint = "Tip: Swipe right to mark read, and swipe left to delete with Undo."
    override val dismissHint = "Got it"

    // G2 - Pinned and Archive
    override val maxPinnedLimitReached = "Maximum 5 pinned conversations"
    override val archived = "Archived"
    override val archive = "Archive"
    override val unarchive = "Unarchive"
    override val unarchiveOnNewMessage = "Unarchive on New Message"
    override val unarchiveOnNewMessageDesc = "Automatically unarchive conversation when a new message arrives"
    override val mute = "Mute"
    override val unmute = "Unmute"
    override val muteFor1Hour = "1 hour"
    override val muteFor8Hours = "8 hours"
    override val muteFor1Week = "1 week"
    override val muteAlways = "Always"

    // G4 - Drafts
    override val draftPrefix = "Draft: "
    override val draftOnly = "Draft"

    // G5 - In-app Banner
    override val newIncomingMessage = "New message"
    override val oneTimeCodeReceived = "One-time code received"

    // G6 - Global Search
    override val searchContacts = "Contacts"
    override val searchConversations = "Conversations"
    override val searchMessages = "Messages"
    override val noMessagesHit = "No messages found"

    // G7 - Scheduled Screen
    override val scheduledToday = "Today"
    override val scheduledLater = "Later"
    override val scheduledSent = "Sent"
    override val scheduledNoticeDelay = "Scheduled time may be delayed by a few minutes"
    override val editTime = "Edit time"
    override val cancelScheduleConfirm = "Are you sure you want to cancel this scheduled message?"

    // G8 - Spam and Blocked Center
    override val spamTab = "Spam"
    override val blockedTab = "Blocked"
    override val notSpamAction = "Not spam"
    override val senderInSpamList = "Sender in spam list"
    override val autoDeleteSpam = "Auto-delete spam after"
    override val autoDeleteSpamNever = "Never"
    override val autoDeleteSpam7Days = "7 days"
    override val autoDeleteSpam30Days = "30 days"

    // G9 - OTP Center
    override val otpCenterTitle = "OTP Center"
    override val otpWindowHours = "OTP Codes Window"
    override val copyOtp = "Copy code"
    override val otpReceivedAt = "Received at %s"
    override val noOtpCodes = "No one-time codes found in this window"

    // G10 - SIM
    override val chooseSim = "Choose SIM"

    // G11 - Contact Info Screen
    override val contactInfoTitle = "Contact Info"
    override val searchInConversation = "Search in this conversation"
    override val totalMessagesStat = "Total messages"
    override val firstMessageStat = "First message"
    override val lastMessageStat = "Last message"
    override val phoneNumbers = "Phone numbers"
    override val addToContacts = "Add to contacts"
    override val openInContacts = "Open in contacts"

    // G12 - Settings Section
    override val chatsAndNotificationsSection = "Chats and notifications"

    // Scheduled SMS Professional
    override val scheduleSheetTitle = "Schedule message"
    override val scheduleQuick10Min = "10 min"
    override val scheduleQuick1Hour = "1 hour"
    override val scheduleQuickTonight20 = "Tonight 20:00"
    override val scheduleQuickTomorrow09 = "Tomorrow 09:00"
    override val scheduleQuickCustom = "Custom"
    override val scheduleSendsAt = "Sends: %s at %s"
    override val scheduleButtonConfirm = "Schedule"
    override val scheduleErrorRecipient = "Invalid recipient"
    override val scheduleErrorBody = "Message body cannot be empty"
    override val scheduleErrorTooSoon = "Schedule time must be at least 1 minute in the future"
    override val scheduleErrorTooFar = "Schedule time cannot exceed 1 year"
    override val scheduledSmsScheduledSuccess = "Message scheduled"
    override val viewAction = "View"
    override val sectionToday = "Today"
    override val sectionLater = "Later"
    override val sectionHistory = "History"
    override val statusPendingLabel = "Pending"
    override val statusSendingLabel = "Sending"
    override val statusSentLabel = "Sent"
    override val statusFailedLabel = "Failed"
    override val statusCancelledLabel = "Cancelled"
    override val statusMissedLabel = "Missed"
    override val sendNowAction = "Send now"
    override val deleteFromHistoryAction = "Delete from history"
    override val exactAlarmPermissionBanner = "Allow exact alarms for on-time sending"
    override val allowExactAlarmButton = "Open Settings"
    override val scheduledForBubblePrefix = "Scheduled for %s"
    override val forceStopWarningNotice = "If you Force Stop the app from settings, scheduled messages will not send until you open the app again."
    override val batteryRestrictionTitle = "Remove battery restriction for on-time sending?"
    override val batteryRestrictionBody = "Some phone brands stop apps more aggressively than usual."
    override val openSettingsButton = "Open settings"
    override val laterButton = "Later"
    override val dontShowAgainButton = "Don't show again"
    override val batteryStatusTitle = "Battery status"
    override val batteryStatusUnrestricted = "Unrestricted"
    override val batteryStatusRestricted = "Restricted"
    override val scheduledFailedNotificationTitle = "A scheduled message failed to send"
    override val scheduledFailedNotificationBody = "A scheduled message failed to send due to an error"

    // Folder Tab Actions
    override val reorderFolders = "Reorder"
    override val editFolder = "Edit folder"
    override val muteAll = "Mute All"
    override val markAllAsReadAction = "Mark all as read"
    override val deleteFolderAction = "Delete folder"
    override val hideFolder = "Hide folder"
    override val showHiddenFolders = "Show hidden folders"
    override val otpMuteWarning = "One-time codes will also be silenced."
    override val confirmMarkAllReadTitle = "Mark all as read"
    override val confirmMarkAllReadBody = "Are you sure you want to mark all conversations in this folder as read?"
    override val confirmDeleteFolderTitle = "Delete folder"
    override val confirmDeleteFolderBody = "Deleting this folder will not delete conversations or messages. Conversations will return to 'All'."
    override val confirmHideFolderTitle = "Hide folder"
    override val confirmHideFolderBody = "This system folder will be hidden from the tab bar. You can restore it from Settings."
    override val conversationsAffectedSnackbar = "%d conversation(s) updated"
    override val folderRenamedSnackbar = "Folder updated"
    override val folderDeletedSnackbar = "Folder deleted"
    override val folderHiddenSnackbar = "Folder hidden"
    override val unhideFolder = "Unhide"
    override val hiddenFoldersSection = "Hidden Folders"
    override val noHiddenFolders = "No hidden folders"
    override val doneAction = "Done"
    override val muteDurationTitle = "Mute for..."
    override val mute1Hour = "1 hour"
    override val mute8Hours = "8 hours"
    override val mute1Week = "1 week"

    // Rules Feature Strings
    override val rulesMyRules = "My rules"
    override val rulesReadyMade = "Ready-made"
    override val rulesComingSoon = "Coming soon"
    override val rulesNewRule = "New rule"
    override val rulesNoRulesTitle = "You have no rules yet. Tap the button above to start."
    override val rulesFirstTimeHelp = "A rule means: messages from a certain sender or with certain words go to a chosen folder."
    override val rulesGotIt = "Got it"
    override val rulesAdvancedRule = "Advanced rule"
    override val rulesStateOn = "On"
    override val rulesStateOff = "Off"
    override val rulesConfirmDelete = "Are you sure you want to delete this rule?"
    override val rulesDeletedMessage = "Rule deleted"
    override val rulesUndoAction = "Undo"
    override val rulesStep1Title = "From whom?"
    override val rulesChoiceContact = "From a contact"
    override val rulesChoiceNumberOrName = "From a number or name"
    override val rulesChoiceAnyoneWithWord = "Anyone, with a word"
    override val rulesNumberOrNameHint = "e.g. Snapp or 1000"
    override val rulesStep2Title = "If the message has these words"
    override val rulesKeywordHint = "e.g. invoice, payment"
    override val rulesAddWord = "Add"
    override val rulesSkip = "Skip"
    override val rulesStep3Title = "Which folder should it go to?"
    override val rulesCreateNewFolder = "Create a new folder"
    override val rulesStepSummaryTitle = "Review and save"
    override val rulesAffectsConversations = "This rule affects %s conversations."
    override val rulesSave = "Save"
    override val rulesSaveAndApply = "Save and apply to older messages"
    override val rulesConversationsMoved = "%s conversations moved"
    override val rulesBack = "Back"
    override val rulesNext = "Next"
    override val rulesStepOfThree = "%s of 3"
    override val rulesEditRule = "Edit rule"
    override val rulesApplyOlderButton = "Apply rules to older messages"
    override val rulesApplyOlderConfirmDialog = "Rules will run on older messages. Folders you picked by hand won't change."
    override val ruleSaveFailed = "Could not save the rule"
    override val rulesMinKeywordLengthHint = "At least two characters"
    override val rulesApplyFailed = "Could not finish. Please try again."
    override val notifChannelQuietName = "Silent Messages"
    override val notifChannelQuietDesc = "Notifications for muted chats"
    override val notifChannelScheduledName = "Scheduled Messages"
    override val notifChannelScheduledDesc = "Notifications for scheduled SMS"
    override val scheduledSmsMissedNotification = "%s scheduled messages were not sent"
    override val scheduledSmsFailedNotification = "Scheduled message was not sent"
    override val messageNotSent = "Not sent"
    override val callContact = "Call"
    override val unread = "Unread"
    override val otpReceivedBanner = "One-time code received"
    override val newMessageBanner = "New message"
    override val templatesTabName = "Templates"
    override val noTemplatesMessage = "You have no templates yet. Start with the 'Create template' button."
    override val createTemplateButton = "Create template"
    override val saveAsTemplate = "Save as template"
    override val templateAlreadyExists = "This template is already saved"
    override val deleteTemplateConfirm = "Delete template?"
    override val importFromFile = "Import from file"
    override val templatesFoundFormat = "%d template(s) found"
    override val addTemplatesDisabled = "Add (Off)"
    override val importInfoText = "These templates are added turned off. Turn each one on separately."
    override val shareTemplatesButton = "Share"
    override val removePersonalNumbersCheckbox = "Remove personal numbers"
    override val removePersonalNumbersDesc = "Mobile numbers are removed from the file to preserve privacy."
    override val createFileAndSend = "Create file and send"
    override val noTemplatesToShare = "No templates to share"
    override val sort = "Sort"
    override val newest = "Newest"
    override val oldest = "Oldest"
    override val show = "Show"
    override val allMessages = "All messages"
    override val order = "Order"
    override val quickReply = "Quick reply"
    override val copy_code = "Copy code"
    override val copied = "Copied"
    override val mark_as_read = "Mark as read"
    override val code_copied = "Code copied"
    override val unknownNumber = "Unknown number"
    override val addContact = "Add contact"
    override val block = "Block"
    override val searchInChat = "Search in chat"
    override val searchLoadedOnly = "Search covers loaded messages only"
    override val themeClassic = "Classic Telegram"
    override val themeAmoled = "Pure AMOLED"
    override val themeCyprus = "Cyprus & Sand"
    override val themeBridal = "Bridal & Skin"
    override val themePumpkin = "Pumpkin & Charcoal"
    override val themeOcean = "Ocean & Cloud"
    override val themeOlive = "Olive & Sage"
    override val themeDynamic = "System Dynamic"
    override val switchToLight = "Switch to light"
    override val switchToDark = "Switch to dark"
    override val accentColor = "Accent color"
    override val accentDefault = "Default"
    override val accentBlue = "Blue"
    override val accentGreen = "Green"
    override val accentOrange = "Orange"
    override val accentRed = "Red"
    override val accentPurple = "Purple"
    override val accentTeal = "Teal"
    override val accentPink = "Pink"
}

fun getStrings(language: AppLanguage): Strings {
    return when (language) {
        AppLanguage.PERSIAN -> PersianStrings
        AppLanguage.ENGLISH -> EnglishStrings
    }
}

fun getLayoutDirection(language: AppLanguage): LayoutDirection {
    return when (language) {
        AppLanguage.PERSIAN -> LayoutDirection.Rtl
        AppLanguage.ENGLISH -> LayoutDirection.Ltr
    }
}

val LocalAppStrings = compositionLocalOf<Strings> { EnglishStrings }
val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

@Composable
fun appStrings(): Strings = LocalAppStrings.current
