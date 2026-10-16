package com.example.ui.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SmsGramApp
import com.example.data.local.AppDatabase
import com.example.data.local.CannedTemplateEntity
import com.example.data.local.ConversationMetadataEntity
import com.example.data.local.FolderEntity
import com.example.data.local.MessageMetadataEntity
import com.example.data.local.MessageReminderEntity
import com.example.data.local.ScheduleStatus
import com.example.data.local.ScheduledSmsEntity
import com.example.data.local.SenderRuleEntity
import com.example.data.local.RuleTemplateEntity
import com.example.data.local.SettingsPreferences
import com.example.data.local.TrashEntity
import com.example.data.model.ConversationItem
import com.example.data.model.DualSimInfo
import com.example.data.model.SmsMessageItem
import com.example.data.model.ThemeMode
import com.example.data.repository.SmsRepository
import com.example.domain.rules.RuleEngine
import com.example.domain.util.SenderNormalizer
import com.example.receiver.MessageReminderReceiver
import com.example.receiver.ScheduledSmsReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ScreenDestination {
    object Home : ScreenDestination()
    data class Conversation(val threadId: Long, val address: String, val contactName: String?, val initialBody: String? = null) : ScreenDestination()
    data class NewMessage(val initialRecipient: String? = null, val initialBody: String? = null) : ScreenDestination()
    object SavedMessages : ScreenDestination()
    object Folders : ScreenDestination()
    object Rules : ScreenDestination()
    object ScheduledSms : ScreenDestination()
    object Trash : ScreenDestination()
    object CannedTemplates : ScreenDestination()
    object BlockedContacts : ScreenDestination()
    object Settings : ScreenDestination()
}

class SmsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SmsGramApp
    val repository: SmsRepository = app.smsRepository
    val database: AppDatabase = app.database
    val preferences: SettingsPreferences = app.preferences

    init {
        viewModelScope.launch {
            com.example.domain.util.SmsChangeBus.changes
                .debounce(300)
                .collect {
                    repository.invalidateConversationsCache()
                    refreshConversations(forceRefresh = true)
                }
        }
        viewModelScope.launch {
            com.example.domain.util.SmsChangeBus.contactsChanges
                .debounce(300)
                .collect {
                    repository.invalidateContactsCache()
                    loadDeviceContacts()
                }
        }
    }

    // Onboarding State
    private val _isOnboardingCompleted = MutableStateFlow(preferences.isOnboardingCompleted())
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean) {
        preferences.setOnboardingCompleted(completed)
        _isOnboardingCompleted.value = completed
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Home)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Permissions State
    private val _hasPermissions = MutableStateFlow(repository.hasSmsPermissions())
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    fun checkPermissions() {
        _hasPermissions.value = repository.hasSmsPermissions()
    }

    // App Lock State
    private val _isUnlocked = MutableStateFlow(!preferences.isAppLockEnabled())
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Global Selection Mode State (Lifting state so bottom bar can be hidden globally)
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedThreads = MutableStateFlow<Set<Long>>(emptySet())
    val selectedThreads: StateFlow<Set<Long>> = _selectedThreads.asStateFlow()

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedThreads.value = emptySet()
        }
    }

    fun toggleThreadSelection(threadId: Long) {
        val current = _selectedThreads.value.toMutableSet()
        if (current.contains(threadId)) {
            current.remove(threadId)
        } else {
            current.add(threadId)
        }
        _selectedThreads.value = current
        _isSelectionMode.value = current.isNotEmpty()
    }

    fun clearSelection() {
        _selectedThreads.value = emptySet()
        _isSelectionMode.value = false
    }

    fun selectAllThreads(threadIds: List<Long>) {
        _selectedThreads.value = threadIds.toSet()
        _isSelectionMode.value = true
    }

    // Device Contacts for Contact Picker / New Message
    private val _deviceContacts = MutableStateFlow<List<com.example.data.model.ContactEntry>>(emptyList())
    val deviceContacts: StateFlow<List<com.example.data.model.ContactEntry>> = _deviceContacts.asStateFlow()

    fun loadDeviceContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = repository.getDeviceContacts()
                _deviceContacts.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Active Folder Tab
    private val _selectedFolderId = MutableStateFlow("all")
    val selectedFolderId: StateFlow<String> = _selectedFolderId.asStateFlow()

    // All Conversations Raw
    private val _rawConversations = MutableStateFlow<List<ConversationItem>>(emptyList())
    val rawConversations: StateFlow<List<ConversationItem>> = _rawConversations.asStateFlow()

    private val _isLoadingConversations = MutableStateFlow(false)
    val isLoadingConversations: StateFlow<Boolean> = _isLoadingConversations.asStateFlow()

    // Active Conversation Messages
    private val _activeMessages = MutableStateFlow<List<SmsMessageItem>>(emptyList())
    val activeMessages: StateFlow<List<SmsMessageItem>> = _activeMessages.asStateFlow()

    // Active Conversation Metadata
    private val _activeConversationMetadata = MutableStateFlow<ConversationMetadataEntity?>(null)
    val activeConversationMetadata: StateFlow<ConversationMetadataEntity?> = _activeConversationMetadata.asStateFlow()

    // Active Conversation Pinned Message
    private val _activePinnedMessage = MutableStateFlow<MessageMetadataEntity?>(null)
    val activePinnedMessage: StateFlow<MessageMetadataEntity?> = _activePinnedMessage.asStateFlow()

    // Dual SIM Cards
    private val _activeSims = MutableStateFlow<List<DualSimInfo>>(emptyList())
    val activeSims: StateFlow<List<DualSimInfo>> = _activeSims.asStateFlow()

    private val _selectedSimIndex = MutableStateFlow(0)
    val selectedSimIndex: StateFlow<Int> = _selectedSimIndex.asStateFlow()

    // Flow from Room for Folders, Rules, Templates, Scheduled, Trash
    val foldersFlow: StateFlow<List<FolderEntity>> = database.folderDao().getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rulesFlow: StateFlow<List<SenderRuleEntity>> = database.senderRuleDao().getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templatesFlow: StateFlow<List<CannedTemplateEntity>> = database.cannedTemplateDao().getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledFlow: StateFlow<List<ScheduledSmsEntity>> = database.scheduledSmsDao().getAllScheduled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledActive: StateFlow<List<ScheduledSmsEntity>> = database.scheduledSmsDao().observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledHistory: StateFlow<List<ScheduledSmsEntity>> = database.scheduledSmsDao()
        .observeHistory(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationAddress = MutableStateFlow<String>("")
    val currentConversationAddress: StateFlow<String> = _currentConversationAddress.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pendingForCurrentConversation: StateFlow<List<ScheduledSmsEntity>> = _currentConversationAddress
        .flatMapLatest { addr: String ->
            if (addr.isBlank()) {
                flowOf(emptyList())
            } else {
                database.scheduledSmsDao().observePendingForRecipient(addr)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashFlow: StateFlow<List<TrashEntity>> = database.trashDao().getAllTrash()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedNotesFlow: StateFlow<List<com.example.data.local.SavedNoteEntity>> = database.savedNoteDao().getAllSavedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSavedNote(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            database.savedNoteDao().insert(
                com.example.data.local.SavedNoteEntity(text = text.trim())
            )
        }
    }

    fun deleteSavedNote(id: Long) {
        viewModelScope.launch {
            database.savedNoteDao().delete(id)
        }
    }

    fun clearAllSavedNotes() {
        viewModelScope.launch {
            database.savedNoteDao().clearAll()
        }
    }

    val blockedContactsFlow: StateFlow<List<com.example.data.local.BlockedContactEntity>> = database.blockedContactDao().getAllBlocked()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun blockContact(address: String, contactName: String? = null, reason: String? = "مزاحم / هرزنامه") {
        viewModelScope.launch {
            database.blockedContactDao().block(
                com.example.data.local.BlockedContactEntity(
                    address = address,
                    contactName = contactName,
                    reason = reason
                )
            )
        }
    }

    fun unblockContact(address: String) {
        viewModelScope.launch {
            database.blockedContactDao().unblockByAddress(address)
        }
    }

    // Settings
    val languageFlow: StateFlow<com.example.data.model.AppLanguage> = preferences.languageFlow
    val themeFamilyFlow: StateFlow<com.example.data.model.ThemeFamily> = preferences.themeFamilyFlow
    val isDarkFlow: StateFlow<Boolean> = preferences.isDarkFlow
    val accentColorFlow: StateFlow<com.example.data.model.AccentColor> = preferences.accentColorFlow
    val themeModeFlow: StateFlow<ThemeMode> = preferences.themeModeFlow

    fun setThemeFamily(family: com.example.data.model.ThemeFamily) {
        preferences.setThemeFamily(family)
    }

    fun setDark(dark: Boolean) {
        preferences.setDark(dark)
    }

    fun toggleDark() {
        preferences.toggleDark()
    }

    fun setAccentColor(accent: com.example.data.model.AccentColor) {
        preferences.setAccentColor(accent)
    }

    fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeMode(mode)
    }
    val fontSizeFlow: StateFlow<Int> = preferences.fontSizeFlow
    val fontIdFlow: StateFlow<String> = preferences.fontIdFlow
    val customFontNameFlow: StateFlow<String> = preferences.customFontNameFlow
    val persianDigitsFlow: StateFlow<Boolean> = preferences.persianDigitsFlow
    val calendarSystemFlow: StateFlow<com.example.data.model.CalendarSystem> = preferences.calendarSystemFlow

    fun setCalendarSystem(system: com.example.data.model.CalendarSystem) {
        preferences.setCalendarSystem(system)
    }

    val deliveryReportsFlow: StateFlow<Boolean> = preferences.deliveryReportsFlow
    val defaultSimIdFlow: StateFlow<Int> = preferences.defaultSimIdFlow
    val isSimInverted: StateFlow<Boolean> = preferences.simInvertedFlow

    val spamBlockerEnabledFlow = MutableStateFlow(preferences.isSpamBlockerEnabled())
    val customSpamKeywordsFlow = MutableStateFlow(preferences.getCustomSpamKeywords())
    val keywordRoutingRulesFlow = MutableStateFlow(preferences.getKeywordRoutingRules())
    val swipeLeftActionFlow = MutableStateFlow(preferences.getSwipeLeftAction())
    val swipeRightActionFlow = MutableStateFlow(preferences.getSwipeRightAction())

    private val isRefreshing = java.util.concurrent.atomic.AtomicBoolean(false)
    private var conversationLoadJob: Job? = null
    private var pinnedMessageJob: Job? = null

    fun setLanguage(lang: com.example.data.model.AppLanguage) {
        preferences.setAppLanguage(lang)
    }

    fun setFont(fontId: String) {
        preferences.setFontId(fontId)
    }

    fun setSpamBlockerEnabled(enabled: Boolean) {
        preferences.setSpamBlockerEnabled(enabled)
        spamBlockerEnabledFlow.value = enabled
        refreshConversations(forceRefresh = true)
    }

    fun setCustomSpamKeywords(keywords: Set<String>) {
        preferences.setCustomSpamKeywords(keywords)
        customSpamKeywordsFlow.value = keywords
        refreshConversations(forceRefresh = true)
    }

    fun setKeywordRoutingRules(rules: Map<String, String>) {
        preferences.setKeywordRoutingRules(rules)
        keywordRoutingRulesFlow.value = rules
        refreshConversations(forceRefresh = true)
    }

    fun setSwipeLeftAction(action: String) {
        preferences.setSwipeLeftAction(action)
        swipeLeftActionFlow.value = action
    }

    fun setSwipeRightAction(action: String) {
        preferences.setSwipeRightAction(action)
        swipeRightActionFlow.value = action
    }

    fun uploadCustomFont(uri: android.net.Uri, displayName: String): Boolean {
        return preferences.saveCustomFontFromUri(uri, displayName)
    }

    init {
        initSystemFolders()
        refreshAll()
    }

    private fun initSystemFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existing = database.folderDao().getAllFoldersList()
                if (existing.isEmpty() || existing.none { it.id == "bank" }) {
                    database.folderDao().insertAll(FolderEntity.SYSTEM_FOLDERS)
                }
                val updated = database.folderDao().getAllFoldersList()
                if (updated.none { it.id == "spam" }) {
                    database.folderDao().insertFolder(
                        FolderEntity(
                            id = "spam",
                            nameFa = "هرزنامه و اسپم",
                            nameEn = "Spam",
                            iconKey = "block",
                            colorHex = "#FF3B30",
                            isSystem = true,
                            sortOrder = 6
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var lastRefreshTimestamp = 0L

    fun refreshAll(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastRefreshTimestamp) < 2500L) {
            return
        }
        lastRefreshTimestamp = now
        try {
            _hasPermissions.value = repository.hasSmsPermissions()
            refreshConversations(forceRefresh = force)
            if (_activeSims.value.isEmpty()) {
                refreshSims()
            }
            loadDeviceContacts()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerOtpPurge() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val hours = preferences.getAutoDeleteOtpHours()
                if (hours > 0 && repository.hasSmsPermissions()) {
                    repository.purgeExpiredOtps(hours)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unlockApp(pin: String): Boolean {
        if (preferences.verifyPin(pin)) {
            _isUnlocked.value = true
            return true
        }
        return false
    }

    fun unlockWithBiometrics() {
        _isUnlocked.value = true
    }

    fun lockApp() {
        if (preferences.isAppLockEnabled()) {
            _isUnlocked.value = false
        }
    }

    fun openConversationForRecipient(
        recipient: String,
        initialBody: String? = null
    ) {
        val normalizedRecipient = recipient.trim()

        if (normalizedRecipient.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cleanDigits = com.example.domain.util.SmartTextParser.normalizeDigits(normalizedRecipient)
                    .replace(" ", "")
                    .replace("-", "")
                    .replace("(", "")
                    .replace(")", "")

                // First check if there is an existing conversation in loaded conversations
                val existing = _rawConversations.value.find { conv ->
                    val addr = conv.address.replace(" ", "").replace("-", "")
                    addr.equals(cleanDigits, ignoreCase = true) ||
                    (cleanDigits.length >= 10 && addr.length >= 10 && cleanDigits.takeLast(10) == addr.takeLast(10))
                }

                val threadId = existing?.threadId ?: repository.getOrCreateThreadId(cleanDigits)
                val contactName = existing?.contactName ?: repository.getContactInfo(cleanDigits).first

                withContext(Dispatchers.Main) {
                    navigateTo(
                        ScreenDestination.Conversation(
                            threadId = threadId,
                            address = if (cleanDigits.isNotBlank()) cleanDigits else normalizedRecipient,
                            contactName = contactName,
                            initialBody = initialBody
                        )
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                withContext(Dispatchers.Main) {
                    navigateTo(
                        ScreenDestination.NewMessage(
                            initialRecipient = normalizedRecipient,
                            initialBody = initialBody
                        )
                    )
                }
            }
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
        if (destination is ScreenDestination.Conversation) {
            loadConversation(destination.threadId)
        }
    }

    fun navigateBack(): Boolean {
        val current = _currentScreen.value
        return if (current !is ScreenDestination.Home) {
            _currentScreen.value = ScreenDestination.Home
            true
        } else {
            false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFolder(folderId: String) {
        _selectedFolderId.value = folderId
    }

    fun setSelectedSimIndex(index: Int) {
        if (index in _activeSims.value.indices) {
            _selectedSimIndex.value = index
        }
    }

    fun refreshConversations(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!repository.hasSmsPermissions()) return@launch
            if (!isRefreshing.compareAndSet(false, true)) return@launch

            _isLoadingConversations.value = true
            try {
                // Fetch conversations, metadata, and rules concurrently
                val listDeferred = async { repository.getConversations(forceRefresh) }
                val metadataDeferred = async { database.conversationMetadataDao().getAllMetadataList() }
                val rulesDeferred = async { database.senderRuleDao().getActiveRules() }

                val list = listDeferred.await()
                val metadataList = metadataDeferred.await()
                val rules = rulesDeferred.await()

                val metadataMap = metadataList.associateBy { it.threadId }

                // Precompute settings outside the per-conversation iteration loop (Hotspot #4)
                val kwRules = preferences.getKeywordRoutingRules()
                val customSpamKeywordsLower = preferences.getCustomSpamKeywords().map { it.lowercase() }.toSet()
                val isSpamEnabled = preferences.isSpamBlockerEnabled()

                val enriched = list.map { conv ->
                    val customMeta = metadataMap[conv.threadId]
                    var folder = customMeta?.customFolderId

                    // Check if sender/body matches any active rule
                    if (folder == null && rules.isNotEmpty()) {
                        val isContact = !conv.contactName.isNullOrBlank() || repository.getContactInfo(conv.address).first != null
                        folder = RuleEngine.firstMatchingFolder(rules, conv.address, conv.snippet, isContact)
                    }

                    // Evaluate Option 2 (Spam) and Option 4 (Keyword routing)
                    if (folder == null) {
                        val isContact = !conv.contactName.isNullOrBlank() || repository.getContactInfo(conv.address).first != null
                        if (isSpamEnabled && !isContact && preferences.isSpamWithPreloaded(conv.snippet, customSpamKeywordsLower)) {
                            folder = "spam"
                        } else if (kwRules.isNotEmpty()) {
                            for ((kw, targetFolder) in kwRules) {
                                if (conv.snippet.contains(kw, ignoreCase = true)) {
                                    folder = targetFolder
                                    break
                                }
                            }
                        }
                    }

                    // Automatic fallback categorization if not matched by rule:
                    if (folder == null) {
                        val norm = SenderNormalizer.normalize(conv.address)
                        folder = when {
                            // Bank keywords or numbers
                            conv.snippet.contains("بانک") || conv.snippet.contains("واریز") ||
                                    conv.snippet.contains("برداشت") || conv.snippet.contains("مانده حساب") -> "bank"
                            // Advertising shortcodes (e.g. 5000, 9821, etc.)
                            norm.startsWith("5") || norm.startsWith("9821") || norm.startsWith("2000") -> "advertising"
                            // Services shortcodes (e.g. 1000, 700, etc.)
                            norm.startsWith("1000") || norm.startsWith("700") -> "services"
                            // Personal numbers (09...)
                            norm.startsWith("09") -> "personal"
                            else -> "all"
                        }
                    }

                    val isManual = (customMeta?.customFolderId != null && customMeta.customFolderId != "all")

                    conv.copy(
                        customFolderId = folder,
                        isManuallyCategorized = isManual,
                        isPinned = customMeta?.isPinned ?: false,
                        pinnedAt = customMeta?.pinnedAt ?: 0L,
                        isMuted = customMeta?.isMuted ?: false,
                        muteUntilEpoch = customMeta?.muteUntilEpoch,
                        draftText = customMeta?.draftText,
                        draftUpdatedAt = customMeta?.draftUpdatedAt ?: 0L,
                        isArchived = customMeta?.isArchived ?: false
                    )
                }
                _rawConversations.value = enriched
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingConversations.value = false
                isRefreshing.set(false)
            }
        }
    }

    fun restoreThreads(threadIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = database.trashDao().getTrashByThreadIds(threadIds)
            for (item in items) {
                repository.restoreFromTrash(item.id)
            }
            refreshConversations(forceRefresh = true)
        }
    }

    fun toggleThreadReadStatus(threadId: Long, markAsRead: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (markAsRead) {
                repository.markThreadAsRead(threadId)
            } else {
                repository.markThreadAsUnread(threadId)
            }
            refreshConversations(forceRefresh = true)
        }
    }

    fun toggleSimInverted() {
        val current = preferences.isSimInverted()
        preferences.setSimInverted(!current)
        refreshSims()
    }

    fun refreshSims() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rawSims = repository.getActiveSimCards()
                val isInverted = preferences.isSimInverted()
                val orderedSims = if (isInverted && rawSims.size > 1) {
                    listOf(
                        rawSims[1].copy(slotIndex = 0),
                        rawSims[0].copy(slotIndex = 1)
                    )
                } else {
                    rawSims
                }
                val enriched = orderedSims.map { sim ->
                    val label = preferences.getSimCustomLabel(sim.subscriptionId)
                    val color = preferences.getSimCustomColor(sim.subscriptionId)
                    sim.copy(
                        customLabel = label,
                        customColorHex = color
                    )
                }
                _activeSims.value = enriched

                // Set initial selected SIM based on default preference if valid
                val defaultSubId = preferences.getDefaultSimId()
                if (defaultSubId != -1) {
                    val foundIdx = enriched.indexOfFirst { it.subscriptionId == defaultSubId }
                    if (foundIdx != -1) {
                        _selectedSimIndex.value = foundIdx
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSimCustomLabel(subId: Int, label: String) {
        preferences.setSimCustomLabel(subId, label)
        refreshSims()
    }

    fun setSimCustomColor(subId: Int, colorHex: String?) {
        preferences.setSimCustomColor(subId, colorHex)
        refreshSims()
    }

    fun setDefaultSim(subId: Int) {
        preferences.setDefaultSimId(subId)
        refreshSims()
    }

    fun setThreadPreferredSim(threadId: Long, subId: Int) {
        preferences.setThreadPreferredSim(threadId, subId)
        val foundIdx = _activeSims.value.indexOfFirst { it.subscriptionId == subId }
        if (foundIdx != -1) {
            _selectedSimIndex.value = foundIdx
        }
    }

    fun getThreadPreferredSim(threadId: Long): Int {
        return preferences.getThreadPreferredSim(threadId)
    }

    fun loadConversation(threadId: Long) {
        conversationLoadJob?.cancel()
        conversationLoadJob = viewModelScope.launch(Dispatchers.IO) {
            val msgsDeferred = async { repository.getMessagesForThread(threadId) }
            val metaDeferred = async { database.conversationMetadataDao().getMetadata(threadId) }

            val msgs = msgsDeferred.await()
            val meta = metaDeferred.await()

            _activeMessages.value = msgs
            _activeConversationMetadata.value = meta
            _currentConversationAddress.value = meta?.address.orEmpty()
            repository.markThreadAsRead(threadId)

            // Local update of conversation list without triggering full telephony rescan
            _rawConversations.update { list ->
                list.map { conv ->
                    if (conv.threadId == threadId && conv.unreadCount > 0) {
                        conv.copy(unreadCount = 0)
                    } else conv
                }
            }
        }

        // Hotspot #5 Fix: Separate pinned message collection job so it doesn't hold up loadConversation
        pinnedMessageJob?.cancel()
        pinnedMessageJob = viewModelScope.launch(Dispatchers.IO) {
            database.messageMetadataDao().getPinnedMessageForThread(threadId).collect { pinned ->
                _activePinnedMessage.value = pinned
            }
        }
    }

    fun sendMessage(recipient: String, body: String, subId: Int = -1) {
        viewModelScope.launch {
            val actualSubId = if (subId != -1) subId else {
                val currentSim = _activeSims.value.getOrNull(_selectedSimIndex.value)
                currentSim?.subscriptionId ?: -1
            }

            val success = repository.sendSms(
                recipient = recipient,
                body = body,
                subId = actualSubId,
                deliveryReport = preferences.isDeliveryReportsEnabled()
            )

            if (success) {
                // G4: Clear draft on successful send
                val current = _currentScreen.value
                if (current is ScreenDestination.Conversation) {
                    clearDraft(current.threadId)
                    loadConversation(current.threadId)
                } else {
                    refreshConversations()
                }
            }
        }
    }

    private data class PendingDeleteTask(val message: SmsMessageItem, val job: kotlinx.coroutines.Job)
    private val pendingDeletions = java.util.concurrent.ConcurrentHashMap<Long, PendingDeleteTask>()
    private val processScope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    fun deleteMessage(message: SmsMessageItem) {
        viewModelScope.launch {
            repository.deleteMessage(message)
            loadConversation(message.threadId)
        }
    }

    fun deleteMessageWithUndo(message: SmsMessageItem, onDeleted: (undoAction: () -> Unit) -> Unit) {
        // Instantly remove from UI for fluid responsiveness
        _activeMessages.value = _activeMessages.value.filter { it.id != message.id }

        val job = viewModelScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(5000L)
            pendingDeletions.remove(message.id)
            repository.deleteMessage(message)
        }
        val task = PendingDeleteTask(message, job)
        pendingDeletions.put(message.id, task)?.job?.cancel()

        onDeleted {
            val removed = pendingDeletions.remove(message.id)
            removed?.job?.cancel()
            viewModelScope.launch {
                loadConversation(message.threadId)
                refreshConversations()
            }
        }
    }

    fun resendMessage(message: SmsMessageItem, onError: (() -> Unit)? = null) {
        if (!message.isSentByUser || (message.status != 64 && message.status != 32)) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.sendSms(message.address, message.body, message.subId)
            if (success) {
                repository.removeSystemSms(message.id)
                loadConversation(message.threadId)
                refreshConversations()
            } else {
                withContext(Dispatchers.Main) {
                    onError?.invoke()
                }
            }
        }
    }

    fun deleteConversation(threadId: Long) {
        viewModelScope.launch {
            clearDraft(threadId)
            repository.deleteThread(threadId)
            refreshConversations()
            if (_currentScreen.value is ScreenDestination.Conversation) {
                _currentScreen.value = ScreenDestination.Home
            }
        }
    }

    fun pinMessage(message: SmsMessageItem, isPinned: Boolean) {
        viewModelScope.launch {
            if (isPinned) {
                database.messageMetadataDao().unpinAllInThread(message.threadId)
            }
            database.messageMetadataDao().saveMetadata(
                MessageMetadataEntity(
                    smsId = message.id,
                    threadId = message.threadId,
                    isPinned = isPinned
                )
            )
            loadConversation(message.threadId)
        }
    }

    fun setConversationPinned(threadId: Long, isPinned: Boolean, onLimitReached: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (isPinned) {
                val currentPinnedCount = _rawConversations.value.count { it.isPinned }
                if (currentPinnedCount >= 5) {
                    onLimitReached?.invoke()
                    return@launch
                }
            }
            repository.setPinned(threadId, isPinned)
            refreshConversations()
        }
    }

    fun setConversationMuted(threadId: Long, muted: Boolean, untilEpoch: Long?) {
        viewModelScope.launch {
            repository.setMuted(threadId, muted, untilEpoch)
            refreshConversations()
        }
    }

    fun setConversationArchived(threadId: Long, archived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(threadId, archived)
            refreshConversations()
        }
    }

    private var draftSaveJob: Job? = null
    fun onDraftChanged(threadId: Long, text: String?) {
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(500)
            repository.setDraft(threadId, text)
            // also update in-memory rawConversations if present
            _rawConversations.value = _rawConversations.value.map {
                if (it.threadId == threadId) it.copy(draftText = text, draftUpdatedAt = System.currentTimeMillis()) else it
            }
        }
    }

    fun clearDraft(threadId: Long) {
        viewModelScope.launch {
            repository.setDraft(threadId, null)
            _rawConversations.value = _rawConversations.value.map {
                if (it.threadId == threadId) it.copy(draftText = null) else it
            }
        }
    }

    fun setConversationCustomFolder(threadId: Long, folderId: String?) {
        viewModelScope.launch {
            val existing = database.conversationMetadataDao().getMetadata(threadId)
            if (existing != null) {
                database.conversationMetadataDao().saveMetadata(existing.copy(customFolderId = folderId))
            } else {
                database.conversationMetadataDao().saveMetadata(
                    ConversationMetadataEntity(threadId = threadId, customFolderId = folderId)
                )
            }
            refreshConversations()
        }
    }

    fun batchMarkAsRead(threadIds: List<Long>) {
        viewModelScope.launch {
            threadIds.forEach { threadId ->
                repository.markThreadAsRead(threadId)
            }
            refreshConversations()
        }
    }

    fun batchMarkAsUnread(threadIds: List<Long>) {
        viewModelScope.launch {
            threadIds.forEach { threadId ->
                repository.markThreadAsUnread(threadId)
            }
            refreshConversations()
        }
    }

    fun batchTogglePin(threadIds: List<Long>, isPinned: Boolean) {
        viewModelScope.launch {
            threadIds.forEach { threadId ->
                val existing = database.conversationMetadataDao().getMetadata(threadId)
                if (existing != null) {
                    database.conversationMetadataDao().saveMetadata(existing.copy(isPinned = isPinned))
                } else {
                    database.conversationMetadataDao().saveMetadata(
                        ConversationMetadataEntity(threadId = threadId, isPinned = isPinned)
                    )
                }
            }
            refreshConversations()
        }
    }

    fun batchMoveToFolder(threadIds: List<Long>, folderId: String?) {
        viewModelScope.launch {
            threadIds.forEach { threadId ->
                val existing = database.conversationMetadataDao().getMetadata(threadId)
                if (existing != null) {
                    database.conversationMetadataDao().saveMetadata(existing.copy(customFolderId = folderId))
                } else {
                    database.conversationMetadataDao().saveMetadata(
                        ConversationMetadataEntity(threadId = threadId, customFolderId = folderId)
                    )
                }
            }
            refreshConversations()
        }
    }

    fun batchDeleteConversations(threadIds: List<Long>) {
        viewModelScope.launch {
            threadIds.forEach { threadId ->
                repository.deleteThread(threadId)
            }
            refreshConversations()
        }
    }

    fun createSenderRule(senderPattern: String, targetFolderId: String, explanation: String = "") {
        viewModelScope.launch {
            database.senderRuleDao().insertRule(
                SenderRuleEntity(
                    senderPattern = senderPattern,
                    targetFolderId = targetFolderId,
                    explanation = explanation
                )
            )
            refreshConversations()
        }
    }

    fun toggleRule(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                database.senderRuleDao().toggleRule(ruleId, isEnabled)
                refreshConversations()
            } catch (e: Exception) {
                android.util.Log.e("SmsViewModel", "Failed to toggle rule $ruleId", e)
            }
        }
    }

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            try {
                database.senderRuleDao().deleteRule(ruleId)
                refreshConversations()
            } catch (e: Exception) {
                android.util.Log.e("SmsViewModel", "Failed to delete rule $ruleId", e)
            }
        }
    }

    fun saveRule(
        rule: SenderRuleEntity,
        onComplete: ((Long) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val newId = if (rule.id > 0) {
                    database.senderRuleDao().updateRule(rule)
                    rule.id
                } else {
                    database.senderRuleDao().insertRule(rule)
                }
                refreshConversations()
                onComplete?.invoke(newId)
            } catch (e: Exception) {
                android.util.Log.e("SmsViewModel", "Failed to save rule", e)
                onError?.invoke("rule_save_failed")
            }
        }
    }

    fun restoreRule(rule: SenderRuleEntity) {
        viewModelScope.launch {
            try {
                database.senderRuleDao().insertRule(rule)
                refreshConversations()
            } catch (e: Exception) {
                android.util.Log.e("SmsViewModel", "Failed to restore rule", e)
            }
        }
    }

    fun applyRulesToOlderMessages(onComplete: (com.example.domain.rules.ApplyResult) -> Unit) {
        viewModelScope.launch {
            val app = getApplication<SmsGramApp>()
            val result = com.example.domain.rules.RuleApplier.applyRules(app)
            refreshConversations()
            onComplete(result)
        }
    }

    fun isRulesHelpSeen(): Boolean {
        return preferences.isRulesHelpSeen()
    }

    fun markRulesHelpSeen() {
        preferences.markRulesHelpSeen()
    }

    fun setCurrentConversationAddress(address: String) {
        _currentConversationAddress.value = address
    }

    fun scheduleSms(
        recipient: String,
        body: String,
        epochTime: Long,
        subId: Int = -1,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val validation = com.example.domain.scheduling.ScheduleValidator.validate(recipient, body, epochTime, now)
            if (validation is com.example.domain.scheduling.ValidationResult.Error) {
                val errorKey = com.example.domain.scheduling.SchedulingErrorMapper.mapValidationErrorKey(validation.error)
                withContext(Dispatchers.Main) {
                    onError?.invoke(errorKey)
                }
                return@launch
            }

            val entity = ScheduledSmsEntity(
                recipient = recipient,
                body = body,
                scheduledEpoch = epochTime,
                subId = subId,
                status = ScheduleStatus.PENDING,
                createdEpoch = now,
                updatedEpoch = now
            )
            val id = database.scheduledSmsDao().insert(entity)
            val insertedItem = entity.copy(id = id)

            val scheduleResult = com.example.domain.scheduling.SmsScheduler.schedule(app, insertedItem)
            when (scheduleResult) {
                is com.example.domain.scheduling.SmsScheduler.ScheduleResult.Exact,
                is com.example.domain.scheduling.SmsScheduler.ScheduleResult.Approximate -> {
                    withContext(Dispatchers.Main) {
                        onSuccess?.invoke()
                    }
                }
                is com.example.domain.scheduling.SmsScheduler.ScheduleResult.Failed -> {
                    database.scheduledSmsDao().markFailed(id, "alarm_failed", now)
                    val errorKey = com.example.domain.scheduling.SchedulingErrorMapper.mapScheduleResultKey(scheduleResult) ?: "schedule_error_failed"
                    withContext(Dispatchers.Main) {
                        onError?.invoke(errorKey)
                    }
                }
            }
        }
    }

    fun cancelScheduledSms(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            com.example.domain.scheduling.SmsScheduler.cancel(app, id)
            database.scheduledSmsDao().markCancelled(id, System.currentTimeMillis())
        }
    }

    fun editScheduledSms(
        id: Long,
        epochTime: Long,
        body: String,
        subId: Int = -1,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = database.scheduledSmsDao().getById(id) ?: return@launch
            val now = System.currentTimeMillis()
            val validation = com.example.domain.scheduling.ScheduleValidator.validate(item.recipient, body, epochTime, now)
            if (validation is com.example.domain.scheduling.ValidationResult.Error) {
                val errorKey = com.example.domain.scheduling.SchedulingErrorMapper.mapValidationErrorKey(validation.error)
                withContext(Dispatchers.Main) {
                    onError?.invoke(errorKey)
                }
                return@launch
            }

            com.example.domain.scheduling.SmsScheduler.cancel(app, id)
            database.scheduledSmsDao().updateSchedule(id, epochTime, body, subId, now)

            val updatedItem = item.copy(
                scheduledEpoch = epochTime,
                body = body,
                subId = subId,
                status = ScheduleStatus.PENDING,
                updatedEpoch = now
            )
            val scheduleResult = com.example.domain.scheduling.SmsScheduler.schedule(app, updatedItem)
            when (scheduleResult) {
                is com.example.domain.scheduling.SmsScheduler.ScheduleResult.Exact,
                is com.example.domain.scheduling.SmsScheduler.ScheduleResult.Approximate -> {
                    withContext(Dispatchers.Main) {
                        onSuccess?.invoke()
                    }
                }
                is com.example.domain.scheduling.SmsScheduler.ScheduleResult.Failed -> {
                    database.scheduledSmsDao().markFailed(id, "alarm_failed", now)
                    val errorKey = com.example.domain.scheduling.SchedulingErrorMapper.mapScheduleResultKey(scheduleResult) ?: "schedule_error_failed"
                    withContext(Dispatchers.Main) {
                        onError?.invoke(errorKey)
                    }
                }
            }
        }
    }

    fun sendScheduledNow(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = database.scheduledSmsDao().getById(id) ?: return@launch
            val now = System.currentTimeMillis()
            com.example.domain.scheduling.SmsScheduler.cancel(app, id)
            database.scheduledSmsDao().updateSchedule(id, now, item.body, item.subId, now)
            val updated = item.copy(scheduledEpoch = now, status = ScheduleStatus.PENDING, updatedEpoch = now)
            com.example.domain.scheduling.SmsScheduler.schedule(app, updated, targetEpoch = now)
        }
    }

    fun deleteScheduledFromHistory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.scheduledSmsDao().delete(id)
        }
    }

    fun restoreFromTrash(trashId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(trashId)
            refreshConversations()
        }
    }

    fun deletePermanently(trashId: Long) {
        viewModelScope.launch {
            database.trashDao().deleteFromTrash(trashId)
        }
    }

    fun clearAllTrash() {
        viewModelScope.launch {
            database.trashDao().clearAllTrash()
        }
    }

    fun addFolder(nameFa: String, nameEn: String, iconKey: String = "folder", colorHex: String = "#5288C1") {
        viewModelScope.launch {
            val id = "folder_${System.currentTimeMillis()}"
            database.folderDao().insertFolder(
                FolderEntity(
                    id = id,
                    nameFa = nameFa,
                    nameEn = nameEn,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    isSystem = false,
                    sortOrder = 10
                )
            )
        }
    }

    fun threadIdsForFolder(folderId: String): List<Long> {
        val conversations = _rawConversations.value
        return conversations.filter { conv ->
            if (folderId == "all") {
                !conv.isManuallyCategorized && conv.customFolderId != "spam"
            } else {
                conv.customFolderId == folderId
            }
        }.map { it.threadId }
    }

    fun muteFolder(folderId: String, durationMs: Long?, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetThreads = threadIdsForFolder(folderId)
            val now = System.currentTimeMillis()
            val muteUntil = when {
                durationMs == null -> null
                durationMs < 0 -> Long.MAX_VALUE
                else -> now + durationMs
            }
            val isMuted = durationMs != null

            try {
                val allMeta = database.conversationMetadataDao().getAllMetadataList().associateBy { it.threadId }
                targetThreads.forEach { threadId ->
                    val existing = allMeta[threadId]
                    if (existing != null) {
                        database.conversationMetadataDao().saveMetadata(
                            existing.copy(isMuted = isMuted, muteUntilEpoch = muteUntil)
                        )
                    } else {
                        database.conversationMetadataDao().saveMetadata(
                            ConversationMetadataEntity(threadId = threadId, isMuted = isMuted, muteUntilEpoch = muteUntil)
                        )
                    }
                }
                refreshConversations()
                withContext(Dispatchers.Main) {
                    onComplete(targetThreads.size)
                }
            } catch (e: Exception) {
                android.util.Log.e("SmsViewModel", "Failed to mute folder $folderId", e)
            }
        }
    }

    fun deleteFolder(folderId: String, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetThreads = threadIdsForFolder(folderId)
            try {
                val allMetadata = database.conversationMetadataDao().getAllMetadataList()
                allMetadata.forEach { meta ->
                    if (meta.customFolderId == folderId) {
                        database.conversationMetadataDao().saveMetadata(meta.copy(customFolderId = null))
                    }
                }
                database.folderDao().deleteCustom(folderId)
                if (_selectedFolderId.value == folderId) {
                    _selectedFolderId.value = "all"
                }
                refreshConversations()
                withContext(Dispatchers.Main) {
                    onComplete(targetThreads.size)
                }
            } catch (e: Exception) {
                android.util.Log.e("SmsViewModel", "Failed to delete folder $folderId", e)
            }
        }
    }

    fun markFolderAsRead(folderId: String, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = _rawConversations.value.filter { conv ->
                if (folderId == "all") {
                    !conv.isManuallyCategorized && conv.customFolderId != "spam"
                } else {
                    conv.customFolderId == folderId
                }
            }
            var count = 0
            list.forEach { conv ->
                if (conv.unreadCount > 0) {
                    repository.markThreadAsRead(conv.threadId)
                    count++
                }
            }
            refreshConversations()
            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    fun setFolderHidden(folderId: String, isHidden: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.folderDao().setHidden(folderId, isHidden)
                if (isHidden && _selectedFolderId.value == folderId) {
                    _selectedFolderId.value = "all"
                }
                refreshConversations()
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                android.util.Log.e("SmsViewModel", "Failed to hide folder $folderId", e)
            }
        }
    }

    fun renameFolder(folderId: String, nameFa: String, nameEn: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.folderDao().setName(folderId, nameFa, nameEn)
                refreshConversations()
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                android.util.Log.e("SmsViewModel", "Failed to rename folder $folderId", e)
            }
        }
    }

    fun setFolderOrder(orders: List<Pair<String, Int>>, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.folderDao().setOrder(orders)
                refreshConversations()
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                android.util.Log.e("SmsViewModel", "Failed to set folder order", e)
            }
        }
    }

    fun addCannedTemplate(title: String, content: String, category: String = "general") {
        viewModelScope.launch {
            database.cannedTemplateDao().insertTemplate(
                CannedTemplateEntity(
                    title = title,
                    content = content,
                    category = category
                )
            )
        }
    }

    fun deleteCannedTemplate(id: Long) {
        viewModelScope.launch {
            database.cannedTemplateDao().deleteTemplate(id)
        }
    }

    fun scheduleMessageReminder(
        smsId: Long,
        threadId: Long,
        address: String,
        snippet: String,
        triggerEpoch: Long
    ) {
        viewModelScope.launch {
            val reminder = MessageReminderEntity(
                smsId = smsId,
                threadId = threadId,
                senderAddress = address,
                messageSnippet = snippet,
                reminderEpoch = triggerEpoch,
                isFired = false
            )
            val id = database.messageReminderDao().insertReminder(reminder)

            val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(app, MessageReminderReceiver::class.java).apply {
                putExtra("reminder_id", id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                app,
                (id + 9000).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpoch, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpoch, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpoch, pendingIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setConversationCustomization(
        threadId: Long,
        address: String,
        bgType: String,
        bgValue: String,
        fontSizeOverride: Int?
    ) {
        viewModelScope.launch {
            val current = database.conversationMetadataDao().getMetadata(threadId)
            val updated = current?.copy(
                customBgType = bgType,
                customBgValue = bgValue,
                fontSizeOverride = fontSizeOverride
            ) ?: ConversationMetadataEntity(
                threadId = threadId,
                address = address,
                customBgType = bgType,
                customBgValue = bgValue,
                fontSizeOverride = fontSizeOverride
            )
            database.conversationMetadataDao().saveMetadata(updated)
            _activeConversationMetadata.value = updated
        }
    }

    suspend fun exportSettingsJson(): String {
        return preferences.exportToJson(database)
    }

    suspend fun importSettingsJson(json: String): Boolean {
        val result = preferences.importFromJson(json, database)
        if (result) {
            refreshAll()
        }
        return result
    }

    // Rule Templates State & Methods
    val ruleTemplates: StateFlow<List<RuleTemplateEntity>> = database.ruleTemplateDao().getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val senderRules: StateFlow<List<SenderRuleEntity>> = database.senderRuleDao().getAllRules()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun saveRuleAsTemplate(rule: SenderRuleEntity): Boolean {
        val firstKeyword = rule.keywords.split("\n", ",").map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: ""
        val rawName = rule.senderPattern.ifBlank { firstKeyword }.ifBlank { "Rule" }
        val cleanName = com.example.domain.templates.TemplateValidator.sanitizeString(rawName, 40)
        val cleanDescription = com.example.domain.templates.TemplateValidator.sanitizeString(rule.explanation, 120)
        val cleanSender = com.example.domain.templates.TemplateValidator.sanitizeString(rule.senderPattern, 100)
        val cleanFolder = com.example.domain.templates.TemplateValidator.sanitizeString(rule.targetFolderId, 50).ifBlank { "shopping" }
        val cleanKeywords = com.example.domain.templates.TemplateValidator.sanitizeKeywords(rule.keywords)
        val checksum = com.example.domain.templates.TemplateValidator.computeChecksum(cleanName, cleanSender, cleanFolder, cleanKeywords)

        if (database.ruleTemplateDao().existsByChecksum(checksum)) {
            return false
        }
        val now = System.currentTimeMillis()
        val template = com.example.data.local.RuleTemplateEntity(
            id = java.util.UUID.randomUUID().toString(),
            name = cleanName,
            description = cleanDescription,
            senderPattern = cleanSender,
            targetFolderId = cleanFolder,
            keywords = cleanKeywords,
            isRegex = rule.isRegex,
            origin = "user",
            isEnabledByDefault = false,
            checksum = checksum,
            createdAt = now,
            updatedAt = now
        )
        database.ruleTemplateDao().insert(template)
        return true
    }

    suspend fun upsertRuleTemplate(
        id: String?,
        name: String,
        description: String,
        senderPattern: String,
        targetFolderId: String,
        keywords: String,
        isRegex: Boolean
    ): Boolean {
        val cleanName = com.example.domain.templates.TemplateValidator.sanitizeString(name, 40)
        if (cleanName.isEmpty()) return false
        val cleanDesc = com.example.domain.templates.TemplateValidator.sanitizeString(description, 120)
        val cleanSender = com.example.domain.templates.TemplateValidator.sanitizeString(senderPattern, 100)
        val cleanFolder = com.example.domain.templates.TemplateValidator.sanitizeString(targetFolderId, 50).ifBlank { "shopping" }
        val cleanKeywords = com.example.domain.templates.TemplateValidator.sanitizeKeywords(keywords)
        val checksum = com.example.domain.templates.TemplateValidator.computeChecksum(cleanName, cleanSender, cleanFolder, cleanKeywords)

        val now = System.currentTimeMillis()
        val templateId = id ?: java.util.UUID.randomUUID().toString()
        val existing = id?.let { database.ruleTemplateDao().getById(it) }

        val template = com.example.data.local.RuleTemplateEntity(
            id = templateId,
            name = cleanName,
            description = cleanDesc,
            senderPattern = cleanSender,
            targetFolderId = cleanFolder,
            keywords = cleanKeywords,
            isRegex = isRegex,
            origin = existing?.origin ?: "user",
            isEnabledByDefault = existing?.isEnabledByDefault ?: false,
            checksum = checksum,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        database.ruleTemplateDao().insert(template)
        return true
    }

    suspend fun deleteRuleTemplate(id: String) {
        val allRules = database.senderRuleDao().getAllRulesList()
        for (r in allRules) {
            if (r.templateId == id) {
                database.senderRuleDao().deleteRule(r.id)
            }
        }
        database.ruleTemplateDao().delete(id)
    }

    suspend fun toggleTemplateEnabled(template: com.example.data.local.RuleTemplateEntity, enabled: Boolean) {
        val allRules = database.senderRuleDao().getAllRulesList()
        val linkedRule = allRules.find { it.templateId == template.id }

        if (enabled) {
            if (linkedRule != null) {
                database.senderRuleDao().setEnabled(linkedRule.id, true)
            } else {
                val newRule = SenderRuleEntity(
                    senderPattern = template.senderPattern,
                    targetFolderId = template.targetFolderId,
                    isRegex = template.isRegex,
                    isEnabled = true,
                    explanation = template.name,
                    keywords = template.keywords,
                    source = "template",
                    templateId = template.id,
                    createdAt = System.currentTimeMillis()
                )
                database.senderRuleDao().insertRule(newRule)
            }
        } else {
            if (linkedRule != null) {
                database.senderRuleDao().setEnabled(linkedRule.id, false)
            }
        }
    }

    suspend fun importTemplatesList(templates: List<com.example.data.local.RuleTemplateEntity>) {
        for (t in templates) {
            if (!database.ruleTemplateDao().existsByChecksum(t.checksum)) {
                database.ruleTemplateDao().insert(t)
            }
        }
    }

    suspend fun createTemplateShareFile(context: Context, templateIds: Set<String>, removePersonalNumbers: Boolean): android.net.Uri? = withContext(Dispatchers.IO) {
        try {
            val templatesDir = java.io.File(context.cacheDir, "templates")
            if (!templatesDir.exists()) {
                templatesDir.mkdirs()
            } else {
                val oneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                templatesDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.lastModified() < oneDayAgo) {
                        file.delete()
                    }
                }
            }

            val all = database.ruleTemplateDao().getAllList()
            val selected = all.filter { templateIds.contains(it.id) }
            if (selected.isEmpty()) return@withContext null

            val jsonContent = com.example.domain.templates.TemplateCodec.encode(selected, removePersonalNumbers)
            val timeStr = android.text.format.DateFormat.format("yyyyMMdd_HHmm", System.currentTimeMillis()).toString()
            val file = java.io.File(templatesDir, "smsgram_templates_$timeStr.smsgram.json")
            file.writeText(jsonContent, Charsets.UTF_8)

            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            android.util.Log.e("SmsViewModel", "Error creating template share file", e)
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        val pending = pendingDeletions.values.toList()
        pendingDeletions.clear()
        for (task in pending) {
            task.job.cancel()
            processScope.launch {
                try {
                    repository.deleteMessage(task.message)
                } catch (e: Exception) {
                    android.util.Log.e("SmsViewModel", "Error deleting pending message on exit", e)
                }
            }
        }
    }
}
