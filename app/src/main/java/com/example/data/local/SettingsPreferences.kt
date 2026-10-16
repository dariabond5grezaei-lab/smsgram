package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.data.model.AppFont
import com.example.data.model.AppLanguage
import com.example.data.model.AccentColor
import com.example.data.model.CalendarSystem
import com.example.data.model.ThemeFamily
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class SettingsPreferences(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("smsgram_prefs", Context.MODE_PRIVATE)

    private val _languageFlow = MutableStateFlow(getAppLanguage())
    val languageFlow: StateFlow<AppLanguage> = _languageFlow.asStateFlow()

    private val _themeFamilyFlow = MutableStateFlow(getThemeFamily())
    val themeFamilyFlow: StateFlow<ThemeFamily> = _themeFamilyFlow.asStateFlow()

    private val _isDarkFlow = MutableStateFlow(isDark())
    val isDarkFlow: StateFlow<Boolean> = _isDarkFlow.asStateFlow()

    private val _accentColorFlow = MutableStateFlow(getAccentColor())
    val accentColorFlow: StateFlow<AccentColor> = _accentColorFlow.asStateFlow()

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    private val _fontSizeFlow = MutableStateFlow(getFontSize())
    val fontSizeFlow: StateFlow<Int> = _fontSizeFlow.asStateFlow()

    private val _persianDigitsFlow = MutableStateFlow(isPersianDigits())
    val persianDigitsFlow: StateFlow<Boolean> = _persianDigitsFlow.asStateFlow()

    private val _calendarSystemFlow = MutableStateFlow(getCalendarSystem())
    val calendarSystemFlow: StateFlow<CalendarSystem> = _calendarSystemFlow.asStateFlow()

    private val _deliveryReportsFlow = MutableStateFlow(isDeliveryReportsEnabled())
    val deliveryReportsFlow: StateFlow<Boolean> = _deliveryReportsFlow.asStateFlow()

    private val _appLockedFlow = MutableStateFlow(isAppLockEnabled())
    val appLockedFlow: StateFlow<Boolean> = _appLockedFlow.asStateFlow()

    private val _fontIdFlow = MutableStateFlow(getFontId())
    val fontIdFlow: StateFlow<String> = _fontIdFlow.asStateFlow()

    private val _customFontNameFlow = MutableStateFlow(getCustomFontName())
    val customFontNameFlow: StateFlow<String> = _customFontNameFlow.asStateFlow()

    fun getAppLanguage(): AppLanguage {
        if (!prefs.contains(KEY_APP_LANGUAGE)) {
            // First run: detect device locale
            val defaultLocale = java.util.Locale.getDefault().language
            val detected = if (defaultLocale == "fa" || defaultLocale.startsWith("fa")) {
                AppLanguage.PERSIAN
            } else {
                AppLanguage.ENGLISH
            }
            // Persist detected initial language
            prefs.edit().putString(KEY_APP_LANGUAGE, detected.name).apply()
            return detected
        }
        val name = prefs.getString(KEY_APP_LANGUAGE, AppLanguage.PERSIAN.name)
        return try {
            AppLanguage.valueOf(name ?: AppLanguage.PERSIAN.name)
        } catch (e: Exception) {
            AppLanguage.PERSIAN
        }
    }

    fun setAppLanguage(lang: AppLanguage) {
        prefs.edit().putString(KEY_APP_LANGUAGE, lang.name).apply()
        _languageFlow.value = lang
    }

    fun getThemeFamily(): ThemeFamily {
        if (!prefs.contains(KEY_THEME_FAMILY)) {
            if (prefs.contains(KEY_THEME_MODE)) {
                val oldName = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK_SLATE.name)
                val oldMode = try {
                    ThemeMode.valueOf(oldName ?: ThemeMode.DARK_SLATE.name)
                } catch (_: Exception) {
                    ThemeMode.DARK_SLATE
                }
                val family = ThemeFamily.familyOf(oldMode)
                val dark = oldMode.isDark
                prefs.edit()
                    .putString(KEY_THEME_FAMILY, family.name)
                    .putBoolean(KEY_THEME_DARK, dark)
                    .apply()
                return family
            } else {
                prefs.edit()
                    .putString(KEY_THEME_FAMILY, ThemeFamily.CLASSIC.name)
                    .putBoolean(KEY_THEME_DARK, true)
                    .apply()
                return ThemeFamily.CLASSIC
            }
        }
        val name = prefs.getString(KEY_THEME_FAMILY, ThemeFamily.CLASSIC.name)
        return try {
            ThemeFamily.valueOf(name ?: ThemeFamily.CLASSIC.name)
        } catch (_: Exception) {
            ThemeFamily.CLASSIC
        }
    }

    fun setThemeFamily(family: ThemeFamily) {
        prefs.edit().putString(KEY_THEME_FAMILY, family.name).apply()
        _themeFamilyFlow.value = family
        _themeModeFlow.value = getThemeMode()
    }

    fun isDark(): Boolean {
        if (!prefs.contains(KEY_THEME_DARK)) {
            getThemeFamily()
        }
        return prefs.getBoolean(KEY_THEME_DARK, true)
    }

    fun setDark(dark: Boolean) {
        prefs.edit().putBoolean(KEY_THEME_DARK, dark).apply()
        _isDarkFlow.value = dark
        _themeModeFlow.value = getThemeMode()
    }

    fun toggleDark() {
        setDark(!isDark())
    }

    fun getAccentColor(): AccentColor {
        val name = prefs.getString(KEY_ACCENT_COLOR, AccentColor.DEFAULT.name)
        return try {
            AccentColor.valueOf(name ?: AccentColor.DEFAULT.name)
        } catch (_: Exception) {
            AccentColor.DEFAULT
        }
    }

    fun setAccentColor(accent: AccentColor) {
        prefs.edit().putString(KEY_ACCENT_COLOR, accent.name).apply()
        _accentColorFlow.value = accent
    }

    fun getThemeMode(): ThemeMode {
        val family = getThemeFamily()
        val dark = isDark()
        if (family == ThemeFamily.DYNAMIC) return ThemeMode.DYNAMIC
        return if (dark) family.darkMode ?: ThemeMode.DARK_SLATE else family.lightMode ?: ThemeMode.DAY
    }

    fun setThemeMode(mode: ThemeMode) {
        val family = ThemeFamily.familyOf(mode)
        setThemeFamily(family)
        setDark(mode.isDark)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeModeFlow.value = mode
    }

    fun getFontSize(): Int {
        return prefs.getInt(KEY_FONT_SIZE, 15)
    }

    fun setFontSize(size: Int) {
        prefs.edit().putInt(KEY_FONT_SIZE, size).apply()
        _fontSizeFlow.value = size
    }

    fun getFontId(): String {
        return prefs.getString(KEY_SELECTED_FONT_ID, "vazirmatn") ?: "vazirmatn"
    }

    fun setFontId(fontId: String) {
        prefs.edit().putString(KEY_SELECTED_FONT_ID, fontId).apply()
        _fontIdFlow.value = fontId
    }

    fun getCustomFontName(): String {
        return prefs.getString(KEY_CUSTOM_FONT_NAME, "فونت سفارشی") ?: "فونت سفارشی"
    }

    fun setCustomFontName(name: String) {
        prefs.edit().putString(KEY_CUSTOM_FONT_NAME, name).apply()
        _customFontNameFlow.value = name
    }

    fun saveCustomFontFromUri(uri: Uri, displayName: String): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val destFile = File(context.filesDir, "custom_font.ttf")
            val outputStream = FileOutputStream(destFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            setCustomFontName(displayName)
            setFontId("custom")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isPersianDigits(): Boolean {
        return prefs.getBoolean(KEY_PERSIAN_DIGITS, true)
    }

    fun setPersianDigits(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERSIAN_DIGITS, enabled).apply()
        _persianDigitsFlow.value = enabled
    }

    fun getCalendarSystem(): CalendarSystem {
        val name = prefs.getString(KEY_CALENDAR_SYSTEM, CalendarSystem.SHAMSI.name)
        return CalendarSystem.fromName(name)
    }

    fun setCalendarSystem(system: CalendarSystem) {
        prefs.edit().putString(KEY_CALENDAR_SYSTEM, system.name).apply()
        _calendarSystemFlow.value = system
    }

    fun isDeliveryReportsEnabled(): Boolean {
        return prefs.getBoolean(KEY_DELIVERY_REPORTS, true)
    }

    fun setDeliveryReports(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DELIVERY_REPORTS, enabled).apply()
        _deliveryReportsFlow.value = enabled
    }

    private val _defaultSimIdFlow = MutableStateFlow(getDefaultSimId())
    val defaultSimIdFlow: StateFlow<Int> = _defaultSimIdFlow.asStateFlow()

    fun getDefaultSimId(): Int {
        return prefs.getInt(KEY_DEFAULT_SIM_ID, -1)
    }

    fun setDefaultSimId(subId: Int) {
        prefs.edit().putInt(KEY_DEFAULT_SIM_ID, subId).apply()
        _defaultSimIdFlow.value = subId
    }

    fun getSimCustomLabel(subId: Int): String {
        return prefs.getString("pref_sim_label_$subId", "") ?: ""
    }

    fun setSimCustomLabel(subId: Int, label: String) {
        prefs.edit().putString("pref_sim_label_$subId", label).apply()
    }

    fun getSimCustomColor(subId: Int): String? {
        return prefs.getString("pref_sim_color_$subId", null)
    }

    fun setSimCustomColor(subId: Int, colorHex: String?) {
        if (colorHex != null) {
            prefs.edit().putString("pref_sim_color_$subId", colorHex).apply()
        } else {
            prefs.edit().remove("pref_sim_color_$subId").apply()
        }
    }

    fun getThreadPreferredSim(threadId: Long): Int {
        return prefs.getInt("pref_thread_sim_$threadId", -1)
    }

    fun setThreadPreferredSim(threadId: Long, subId: Int) {
        prefs.edit().putInt("pref_thread_sim_$threadId", subId).apply()
    }

    private val _simInvertedFlow = MutableStateFlow(isSimInverted())
    val simInvertedFlow: StateFlow<Boolean> = _simInvertedFlow.asStateFlow()

    fun isSimInverted(): Boolean {
        return prefs.getBoolean(KEY_SIM_INVERTED, false)
    }

    fun setSimInverted(inverted: Boolean) {
        prefs.edit().putBoolean(KEY_SIM_INVERTED, inverted).apply()
        _simInvertedFlow.value = inverted
    }

    fun isDefaultsSeeded(): Boolean {
        return prefs.getBoolean("defaults_seeded_v1", false)
    }

    fun markDefaultsSeeded() {
        prefs.edit().putBoolean("defaults_seeded_v1", true).apply()
    }

    fun isNotificationPrivacy(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_PRIVACY, false)
    }

    fun setNotificationPrivacy(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_PRIVACY, enabled).apply()
    }

    fun getAutoDeleteOtpHours(): Int {
        return prefs.getInt(KEY_AUTO_DELETE_OTP_HOURS, 0)
    }

    fun setAutoDeleteOtpHours(hours: Int) {
        prefs.edit().putInt(KEY_AUTO_DELETE_OTP_HOURS, hours).apply()
    }

    fun isAppLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
        _appLockedFlow.value = enabled
    }

    fun getAppLockPin(): String {
        return prefs.getString(KEY_APP_LOCK_PIN, "") ?: ""
    }

    fun setAppLockPin(pin: String) {
        if (pin.isEmpty()) {
            prefs.edit().putString(KEY_APP_LOCK_PIN, "").apply()
            return
        }
        val salt = ByteArray(16)
        java.security.SecureRandom().nextBytes(salt)
        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hashHex = computePbkdf2Hash(pin, salt)
        val storedValue = "pbkdf2\$$saltHex\$$hashHex"
        prefs.edit().putString(KEY_APP_LOCK_PIN, storedValue).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val stored = getAppLockPin()
        if (stored.isEmpty()) return true

        if (stored.startsWith("pbkdf2$")) {
            val parts = stored.split("$")
            if (parts.size == 3) {
                val saltHex = parts[1]
                val expectedHashHex = parts[2]
                val salt = try {
                    saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                } catch (e: Exception) {
                    return false
                }
                val actualHashHex = computePbkdf2Hash(pin, salt)
                return java.security.MessageDigest.isEqual(
                    actualHashHex.toByteArray(Charsets.UTF_8),
                    expectedHashHex.toByteArray(Charsets.UTF_8)
                )
            }
            return false
        } else {
            // Legacy SHA-256 with "smsgram_salt_" or plaintext
            val legacyHashed = legacySha256Pin(pin)
            val matched = (stored == legacyHashed || stored == pin)
            if (matched) {
                // Upgrade to PBKDF2 format
                setAppLockPin(pin)
                return true
            }
            return false
        }
    }

    private fun computePbkdf2Hash(pin: String, salt: ByteArray): String {
        val spec = javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, 120_000, 256)
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun legacySha256Pin(pin: String): String {
        if (pin.isEmpty()) return ""
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest("smsgram_salt_$pin".toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun isUseBiometric(): Boolean {
        return prefs.getBoolean(KEY_USE_BIOMETRIC, true)
    }

    fun setUseBiometric(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_BIOMETRIC, enabled).apply()
    }

    fun getAutoLockTimeoutMinutes(): Int {
        return prefs.getInt(KEY_AUTO_LOCK_TIMEOUT_MINUTES, 0) // 0 = immediate
    }

    fun setAutoLockTimeoutMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_AUTO_LOCK_TIMEOUT_MINUTES, minutes).apply()
    }

    fun isHideContentInRecents(): Boolean {
        return prefs.getBoolean(KEY_HIDE_CONTENT_IN_RECENTS, false)
    }

    fun setHideContentInRecents(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_CONTENT_IN_RECENTS, enabled).apply()
    }

    fun recordPauseTime() {
        prefs.edit().putLong(KEY_LAST_APP_PAUSED_TIME, System.currentTimeMillis()).apply()
    }

    fun shouldLockOnResume(): Boolean {
        if (!isAppLockEnabled()) return false
        val timeoutMin = getAutoLockTimeoutMinutes()
        if (timeoutMin == 0) return true
        if (timeoutMin < 0) return false // never auto-lock
        val lastPaused = prefs.getLong(KEY_LAST_APP_PAUSED_TIME, 0L)
        if (lastPaused == 0L) return true
        val elapsedMs = System.currentTimeMillis() - lastPaused
        return elapsedMs >= (timeoutMin * 60 * 1000L)
    }

    /**
     * 100% offline JSON export of all settings, folders, rules, and templates
     */
    suspend fun exportToJson(db: AppDatabase): String {
        val root = JSONObject()

        val settingsObj = JSONObject().apply {
            put(KEY_THEME_FAMILY, getThemeFamily().name)
            put(KEY_THEME_DARK, isDark())
            put(KEY_ACCENT_COLOR, getAccentColor().name)
            put(KEY_THEME_MODE, getThemeMode().name)
            put(KEY_FONT_SIZE, getFontSize())
            put(KEY_SELECTED_FONT_ID, getFontId())
            put(KEY_CUSTOM_FONT_NAME, getCustomFontName())
            put(KEY_PERSIAN_DIGITS, isPersianDigits())
            put(KEY_CALENDAR_SYSTEM, getCalendarSystem().name)
            put(KEY_DELIVERY_REPORTS, isDeliveryReportsEnabled())
            put(KEY_DEFAULT_SIM_ID, getDefaultSimId())
            put(KEY_NOTIFICATION_PRIVACY, isNotificationPrivacy())
            put(KEY_AUTO_DELETE_OTP_HOURS, getAutoDeleteOtpHours())
        }
        root.put("settings", settingsObj)

        val foldersList = db.folderDao().getAllFoldersList()
        val foldersArray = JSONArray()
        for (f in foldersList) {
            val fObj = JSONObject().apply {
                put("id", f.id)
                put("nameFa", f.nameFa)
                put("nameEn", f.nameEn)
                put("iconKey", f.iconKey)
                put("colorHex", f.colorHex)
                put("isSystem", f.isSystem)
                put("sortOrder", f.sortOrder)
            }
            foldersArray.put(fObj)
        }
        root.put("folders", foldersArray)

        val rulesList = db.senderRuleDao().getActiveRules()
        val rulesArray = JSONArray()
        for (r in rulesList) {
            val rObj = JSONObject().apply {
                put("senderPattern", r.senderPattern)
                put("targetFolderId", r.targetFolderId)
                put("isRegex", r.isRegex)
                put("explanation", r.explanation)
            }
            rulesArray.put(rObj)
        }
        root.put("rules", rulesArray)

        val templatesList = db.cannedTemplateDao().getAllTemplatesList()
        val templatesArray = JSONArray()
        for (t in templatesList) {
            val tObj = JSONObject().apply {
                put("title", t.title)
                put("content", t.content)
                put("category", t.category)
            }
            templatesArray.put(tObj)
        }
        root.put("templates", templatesArray)

        // Conversation metadata (custom folders, pins, locks, wallpapers)
        val convMetaList = db.conversationMetadataDao().getAllMetadataList()
        val metaArray = JSONArray()
        for (m in convMetaList) {
            val mObj = JSONObject().apply {
                put("threadId", m.threadId)
                put("address", m.address)
                put("customFolderId", m.customFolderId)
                put("isPinned", m.isPinned)
                put("isLocked", m.isLocked)
                put("customBgType", m.customBgType)
                put("customBgValue", m.customBgValue)
                if (m.fontSizeOverride != null) put("fontSizeOverride", m.fontSizeOverride)
            }
            metaArray.put(mObj)
        }
        root.put("conversationMetadata", metaArray)

        // Blocked contacts
        val blockedList = db.blockedContactDao().getAllBlockedSync()
        val blockedArray = JSONArray()
        for (b in blockedList) {
            val bObj = JSONObject().apply {
                put("address", b.address)
                put("contactName", b.contactName)
                put("reason", b.reason)
                put("blockedAtEpoch", b.blockedAtEpoch)
            }
            blockedArray.put(bObj)
        }
        root.put("blockedContacts", blockedArray)

        // Saved notes
        val notesList = db.savedNoteDao().getAllSavedNotesList()
        val notesArray = JSONArray()
        for (n in notesList) {
            val nObj = JSONObject().apply {
                put("text", n.text)
                put("dateEpoch", n.dateEpoch)
            }
            notesArray.put(nObj)
        }
        root.put("savedNotes", notesArray)

        return root.toString(2)
    }

    /**
     * 100% offline JSON import
     */
    suspend fun importFromJson(jsonString: String, db: AppDatabase): Boolean {
        return try {
            val root = JSONObject(jsonString)

            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                if (s.has(KEY_THEME_FAMILY)) {
                    val familyName = s.getString(KEY_THEME_FAMILY)
                    val family = try { ThemeFamily.valueOf(familyName) } catch (_: Exception) { ThemeFamily.CLASSIC }
                    setThemeFamily(family)
                } else if (s.has(KEY_THEME_MODE)) {
                    val modeName = s.getString(KEY_THEME_MODE)
                    val mode = try { ThemeMode.valueOf(modeName) } catch (_: Exception) { ThemeMode.DARK_SLATE }
                    setThemeFamily(ThemeFamily.familyOf(mode))
                    setDark(mode.isDark)
                }
                if (s.has(KEY_THEME_DARK)) {
                    setDark(s.getBoolean(KEY_THEME_DARK))
                }
                if (s.has(KEY_ACCENT_COLOR)) {
                    val accentName = s.getString(KEY_ACCENT_COLOR)
                    val accent = try { AccentColor.valueOf(accentName) } catch (_: Exception) { AccentColor.DEFAULT }
                    setAccentColor(accent)
                }
                if (s.has(KEY_FONT_SIZE)) setFontSize(s.getInt(KEY_FONT_SIZE))
                if (s.has(KEY_SELECTED_FONT_ID)) setFontId(s.getString(KEY_SELECTED_FONT_ID))
                if (s.has(KEY_CUSTOM_FONT_NAME)) setCustomFontName(s.getString(KEY_CUSTOM_FONT_NAME))
                if (s.has(KEY_PERSIAN_DIGITS)) setPersianDigits(s.getBoolean(KEY_PERSIAN_DIGITS))
                if (s.has(KEY_CALENDAR_SYSTEM)) setCalendarSystem(CalendarSystem.fromName(s.getString(KEY_CALENDAR_SYSTEM)))
                if (s.has(KEY_DELIVERY_REPORTS)) setDeliveryReports(s.getBoolean(KEY_DELIVERY_REPORTS))
                if (s.has(KEY_DEFAULT_SIM_ID)) setDefaultSimId(s.getInt(KEY_DEFAULT_SIM_ID))
                if (s.has(KEY_NOTIFICATION_PRIVACY)) setNotificationPrivacy(s.getBoolean(KEY_NOTIFICATION_PRIVACY))
                if (s.has(KEY_AUTO_DELETE_OTP_HOURS)) setAutoDeleteOtpHours(s.getInt(KEY_AUTO_DELETE_OTP_HOURS))
            }

            if (root.has("folders")) {
                val fArray = root.getJSONArray("folders")
                val folders = mutableListOf<FolderEntity>()
                for (i in 0 until fArray.length()) {
                    val obj = fArray.getJSONObject(i)
                    folders.add(
                        FolderEntity(
                            id = obj.getString("id"),
                            nameFa = obj.getString("nameFa"),
                            nameEn = obj.getString("nameEn"),
                            iconKey = obj.optString("iconKey", "folder"),
                            colorHex = obj.optString("colorHex", "#5288C1"),
                            isSystem = obj.optBoolean("isSystem", false),
                            sortOrder = obj.optInt("sortOrder", i)
                        )
                    )
                }
                db.folderDao().insertAll(folders)
            }

            if (root.has("rules")) {
                val rArray = root.getJSONArray("rules")
                for (i in 0 until rArray.length()) {
                    val obj = rArray.getJSONObject(i)
                    db.senderRuleDao().insertRule(
                        SenderRuleEntity(
                            senderPattern = obj.getString("senderPattern"),
                            targetFolderId = obj.getString("targetFolderId"),
                            isRegex = obj.optBoolean("isRegex", false),
                            isEnabled = true,
                            explanation = obj.optString("explanation", "")
                        )
                    )
                }
            }

            if (root.has("templates")) {
                val tArray = root.getJSONArray("templates")
                val templates = mutableListOf<CannedTemplateEntity>()
                for (i in 0 until tArray.length()) {
                    val obj = tArray.getJSONObject(i)
                    templates.add(
                        CannedTemplateEntity(
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            category = obj.optString("category", "general")
                        )
                    )
                }
                db.cannedTemplateDao().insertAll(templates)
            }

            if (root.has("conversationMetadata")) {
                val metaArray = root.getJSONArray("conversationMetadata")
                for (i in 0 until metaArray.length()) {
                    val obj = metaArray.getJSONObject(i)
                    val meta = ConversationMetadataEntity(
                        threadId = obj.getLong("threadId"),
                        address = obj.optString("address", ""),
                        customFolderId = if (obj.isNull("customFolderId")) null else obj.optString("customFolderId"),
                        isPinned = obj.optBoolean("isPinned", false),
                        isLocked = obj.optBoolean("isLocked", false),
                        customBgType = obj.optString("customBgType", "DEFAULT"),
                        customBgValue = obj.optString("customBgValue", ""),
                        fontSizeOverride = if (obj.has("fontSizeOverride")) obj.getInt("fontSizeOverride") else null
                    )
                    db.conversationMetadataDao().saveMetadata(meta)
                }
            }

            if (root.has("blockedContacts")) {
                val blockedArray = root.getJSONArray("blockedContacts")
                for (i in 0 until blockedArray.length()) {
                    val obj = blockedArray.getJSONObject(i)
                    db.blockedContactDao().block(
                        BlockedContactEntity(
                            address = obj.getString("address"),
                            contactName = if (obj.isNull("contactName")) null else obj.optString("contactName"),
                            reason = obj.optString("reason", "مزاحم / هرزنامه"),
                            blockedAtEpoch = obj.optLong("blockedAtEpoch", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("savedNotes")) {
                val notesArray = root.getJSONArray("savedNotes")
                for (i in 0 until notesArray.length()) {
                    val obj = notesArray.getJSONObject(i)
                    db.savedNoteDao().insert(
                        SavedNoteEntity(
                            text = obj.getString("text"),
                            dateEpoch = obj.optLong("dateEpoch", System.currentTimeMillis())
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    // ----------------------------------------------------
    // Option 1 & 2: Spam Blocking & Keyword Routing & Swipe Actions
    // ----------------------------------------------------

    fun isSpamBlockerEnabled(): Boolean {
        return prefs.getBoolean(KEY_SPAM_BLOCKER_ENABLED, true)
    }

    fun setSpamBlockerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SPAM_BLOCKER_ENABLED, enabled).apply()
    }

    fun getCustomSpamKeywords(): Set<String> {
        return prefs.getStringSet(KEY_CUSTOM_SPAM_KEYWORDS, emptySet()) ?: emptySet()
    }

    fun setCustomSpamKeywords(keywords: Set<String>) {
        prefs.edit().putStringSet(KEY_CUSTOM_SPAM_KEYWORDS, keywords).apply()
    }

    fun getMatchedSpamKeyword(body: String, customKeywordsLower: Set<String>? = null): String? {
        if (!isSpamBlockerEnabled()) return null
        val lowercaseBody = body.lowercase()
        for (keyword in BUILT_IN_SPAM_KEYWORDS) {
            if (lowercaseBody.contains(keyword)) return keyword
        }
        val custom = customKeywordsLower ?: getCustomSpamKeywords().map { it.lowercase() }.toSet()
        for (keyword in custom) {
            if (keyword.isNotBlank() && lowercaseBody.contains(keyword)) return keyword
        }
        return null
    }

    fun isSpamSms(body: String, sender: String? = null): Boolean {
        if (!isSpamBlockerEnabled()) return false
        if (sender != null && isSenderInSpamAllowlist(sender)) return false
        return getMatchedSpamKeyword(body) != null
    }

    /**
     * Optimized batch variant that accepts pre-fetched custom keywords to avoid repeated SharedPreferences lookups in loops
     */
    fun isSpamWithPreloaded(body: String, customKeywordsLower: Set<String>, sender: String? = null): Boolean {
        if (!isSpamBlockerEnabled()) return false
        if (sender != null && isSenderInSpamAllowlist(sender)) return false
        return getMatchedSpamKeyword(body, customKeywordsLower) != null
    }

    fun getKeywordRoutingRules(): Map<String, String> {
        val json = prefs.getString(KEY_KEYWORD_ROUTING_RULES, "") ?: ""
        if (json.isBlank()) return emptyMap()
        return try {
            val map = mutableMapOf<String, String>()
            val obj = JSONObject(json)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.getString(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun setKeywordRoutingRules(rules: Map<String, String>) {
        try {
            val obj = JSONObject()
            for ((key, value) in rules) {
                obj.put(key, value)
            }
            prefs.edit().putString(KEY_KEYWORD_ROUTING_RULES, obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSwipeLeftAction(): String {
        return prefs.getString(KEY_SWIPE_LEFT_ACTION, "delete") ?: "delete"
    }

    fun setSwipeLeftAction(action: String) {
        prefs.edit().putString(KEY_SWIPE_LEFT_ACTION, action).apply()
    }

    fun getSwipeRightAction(): String {
        return prefs.getString(KEY_SWIPE_RIGHT_ACTION, "read") ?: "read"
    }

    fun setSwipeRightAction(action: String) {
        prefs.edit().putString(KEY_SWIPE_RIGHT_ACTION, action).apply()
    }

    // G2: Unarchive on new message
    fun isUnarchiveOnNewMessage(): Boolean {
        return prefs.getBoolean(KEY_UNARCHIVE_ON_NEW_MESSAGE, false)
    }

    fun setUnarchiveOnNewMessage(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_UNARCHIVE_ON_NEW_MESSAGE, enabled).apply()
    }

    // G8: Spam allowlist
    fun getSpamAllowlist(): Set<String> {
        return prefs.getStringSet(KEY_SPAM_ALLOWLIST, emptySet()) ?: emptySet()
    }

    fun addToSpamAllowlist(sender: String) {
        val current = getSpamAllowlist().toMutableSet()
        current.add(sender.trim())
        prefs.edit().putStringSet(KEY_SPAM_ALLOWLIST, current).apply()
    }

    fun isSenderInSpamAllowlist(sender: String): Boolean {
        val clean = sender.trim()
        val list = getSpamAllowlist()
        return list.contains(clean) || list.any { clean.endsWith(it) || it.endsWith(clean) }
    }

    // G8: Auto-delete spam after days (0 = Never, 7 = 7 days, 30 = 30 days)
    fun getAutoDeleteSpamDays(): Int {
        return prefs.getInt(KEY_AUTO_DELETE_SPAM_DAYS, 0)
    }

    fun setAutoDeleteSpamDays(days: Int) {
        prefs.edit().putInt(KEY_AUTO_DELETE_SPAM_DAYS, days).apply()
    }

    // S13: Battery Banner Persistence
    fun isBatteryBannerDismissed(): Boolean {
        return try {
            prefs.getBoolean(KEY_BATTERY_BANNER_DISMISSED, false)
        } catch (_: Exception) {
            false
        }
    }

    fun dismissBatteryBanner() {
        try {
            prefs.edit().putBoolean(KEY_BATTERY_BANNER_DISMISSED, true).apply()
        } catch (_: Exception) {}
    }

    fun isRulesHelpSeen(): Boolean {
        return prefs.getBoolean(KEY_RULES_HELP_SEEN, false)
    }

    fun markRulesHelpSeen() {
        try {
            prefs.edit().putBoolean(KEY_RULES_HELP_SEEN, true).apply()
        } catch (_: Exception) {}
    }

    fun getUnknownDismissedThreads(): Set<String> {
        return prefs.getStringSet(KEY_UNKNOWN_DISMISSED_THREADS, emptySet()) ?: emptySet()
    }

    fun dismissUnknownSenderThread(threadId: String) {
        val current = getUnknownDismissedThreads().toMutableSet()
        current.add(threadId)
        prefs.edit().putStringSet(KEY_UNKNOWN_DISMISSED_THREADS, current).apply()
    }

    companion object {
        private const val KEY_UNKNOWN_DISMISSED_THREADS = "unknown_dismissed_threads"
        private const val KEY_RULES_HELP_SEEN = "pref_rules_help_seen"
        private const val KEY_ONBOARDING_COMPLETED = "pref_onboarding_completed"
        private const val KEY_APP_LANGUAGE = "pref_app_language"
        private const val KEY_THEME_FAMILY = "pref_theme_family"
        private const val KEY_THEME_DARK = "pref_theme_dark"
        private const val KEY_ACCENT_COLOR = "pref_accent_color"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_FONT_SIZE = "pref_font_size"
        private const val KEY_SELECTED_FONT_ID = "pref_selected_font_id"
        private const val KEY_CUSTOM_FONT_NAME = "pref_custom_font_name"
        private const val KEY_PERSIAN_DIGITS = "pref_persian_digits"
        private const val KEY_CALENDAR_SYSTEM = "pref_calendar_system"
        private const val KEY_DELIVERY_REPORTS = "pref_delivery_reports"
        private const val KEY_DEFAULT_SIM_ID = "pref_default_sim_id"
        private const val KEY_SIM_INVERTED = "pref_sim_inverted"
        private const val KEY_NOTIFICATION_PRIVACY = "pref_notification_privacy"
        private const val KEY_AUTO_DELETE_OTP_HOURS = "pref_auto_delete_otp_hours"
        private const val KEY_APP_LOCK_ENABLED = "pref_app_lock_enabled"
        private const val KEY_APP_LOCK_PIN = "pref_app_lock_pin"
        private const val KEY_USE_BIOMETRIC = "pref_use_biometric"
        private const val KEY_AUTO_LOCK_TIMEOUT_MINUTES = "pref_auto_lock_timeout_minutes"
        private const val KEY_HIDE_CONTENT_IN_RECENTS = "pref_hide_content_in_recents"
        private const val KEY_LAST_APP_PAUSED_TIME = "pref_last_app_paused_time"

        private const val KEY_SPAM_BLOCKER_ENABLED = "pref_spam_blocker_enabled"
        private const val KEY_CUSTOM_SPAM_KEYWORDS = "pref_custom_spam_keywords"
        private const val KEY_KEYWORD_ROUTING_RULES = "pref_keyword_routing_rules"
        private const val KEY_SWIPE_LEFT_ACTION = "pref_swipe_left_action"
        private const val KEY_SWIPE_RIGHT_ACTION = "pref_swipe_right_action"
        private const val KEY_UNARCHIVE_ON_NEW_MESSAGE = "pref_unarchive_on_new_message"
        private const val KEY_SPAM_ALLOWLIST = "spam_allowlist"
        private const val KEY_AUTO_DELETE_SPAM_DAYS = "pref_auto_delete_spam_days"
        private const val KEY_BATTERY_BANNER_DISMISSED = "battery_banner_dismissed"

        val BUILT_IN_SPAM_KEYWORDS = listOf(
            "ثبت نام سهام عدالت", "ابلاغیه قضایی", "قرعه کشی", "کلیک کنید", "برنده شدید",
            "تور مسافرتی", "برداشت سود", "لینک زیر", "ad.is/", "bit.ly/", "goo.gl/",
            "shrtco.de", "t.me/", "t.co/"
        )
    }
}
