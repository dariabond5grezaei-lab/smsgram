package com.example

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.core.os.LocaleListCompat
import com.example.data.local.AppDatabase
import com.example.data.local.CannedTemplateEntity
import com.example.data.local.FolderEntity
import com.example.data.local.SenderRuleEntity
import com.example.data.local.SettingsPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.Folder
import com.example.data.repository.SmsRepository
import com.example.domain.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsGramApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val preferences: SettingsPreferences by lazy {
        SettingsPreferences(this)
    }

    val smsRepository: SmsRepository by lazy {
        SmsRepository(this, database)
    }

    override fun onCreate() {
        super.onCreate()
        applySavedLocale()
        NotificationHelper.setupNotificationChannels(this)
        seedInitialData()

        // Reconcile scheduled SMS alarms on app launch/update/process recovery
        CoroutineScope(Dispatchers.IO).launch {
            com.example.domain.scheduling.SmsScheduler.reconcileAll(this@SmsGramApp, System.currentTimeMillis())
        }

        try {
            contentResolver.registerContentObserver(
                android.provider.Telephony.Sms.CONTENT_URI,
                true,
                object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        super.onChange(selfChange)
                        com.example.domain.util.SmsChangeBus.notifyChanged()
                    }
                }
            )
            contentResolver.registerContentObserver(
                android.provider.ContactsContract.Contacts.CONTENT_URI,
                true,
                object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        super.onChange(selfChange)
                        com.example.domain.util.SmsChangeBus.notifyContactsChanged()
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applySavedLocale() {
        try {
            val savedLang = preferences.getAppLanguage()
            val tag = if (savedLang == AppLanguage.PERSIAN) "fa" else "en"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = getSystemService(android.app.LocaleManager::class.java)
                localeManager?.applicationLocales = android.os.LocaleList.forLanguageTags(tag)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun seedInitialData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // a. Read SharedPreferences "smsgram_prefs". If key "legacy_folders_cleaned_v2" is absent: deleteLegacyFolders() once, then set key true
                val sp = getSharedPreferences("smsgram_prefs", android.content.Context.MODE_PRIVATE)
                if (!sp.getBoolean("legacy_folders_cleaned_v2", false)) {
                    database.folderDao().deleteLegacyFolders()
                    sp.edit().putBoolean("legacy_folders_cleaned_v2", true).apply()
                }

                // b. Every start: folderDao().insertIgnore(FolderEntity.SYSTEM_FOLDERS)
                database.folderDao().insertIgnore(FolderEntity.SYSTEM_FOLDERS)

                // c. Move template seeding, default sender-rule seeding and purge calls into nested launch {}
                launch {
                    try {
                        if (!preferences.isDefaultsSeeded()) {
                            val currentTemplates = database.cannedTemplateDao().getAllTemplatesList()
                            val currentRules = database.senderRuleDao().getAllRulesList()

                            if (currentTemplates.isEmpty() && currentRules.isEmpty()) {
                                val isFa = preferences.getAppLanguage() == AppLanguage.PERSIAN
                                val defaultTemplates = if (isFa) {
                                    listOf(
                                        CannedTemplateEntity(title = "در جلسه هستم", content = "سلام، در جلسه کاری هستم. در اولین فرصت با شما تماس می‌گیرم.", category = "کاری"),
                                        CannedTemplateEntity(title = "در حال رانندگی", content = "در حال رانندگی هستم، به زودی با شما تماس می‌گیرم.", category = "شخصی"),
                                        CannedTemplateEntity(title = "درخواست شماره کارت", content = "سلام، لطفاً شماره کارت بانکی و نام صاحب حساب را ارسال بفرمایید.", category = "مالی"),
                                        CannedTemplateEntity(title = "تایید واریز", content = "با سلام، مبلغ مورد نظر به حساب شما واریز شد. لطفاً بررسی بفرمایید.", category = "مالی"),
                                        CannedTemplateEntity(title = "لوکیشن / آدرس", content = "سلام، آدرس دقیق مقصد را برایم پیامک کنید.", category = "عمومی")
                                    )
                                } else {
                                    listOf(
                                        CannedTemplateEntity(title = "In a meeting", content = "Hi, I'm currently in a meeting. Will call you back shortly.", category = "Work"),
                                        CannedTemplateEntity(title = "Driving", content = "I'm driving right now, will call you soon.", category = "Personal"),
                                        CannedTemplateEntity(title = "Send details", content = "Please text me the details or address.", category = "General"),
                                        CannedTemplateEntity(title = "Received", content = "Got it, thank you!", category = "General")
                                    )
                                }
                                database.cannedTemplateDao().insertAll(defaultTemplates)

                                val defaultRules = listOf(
                                    SenderRuleEntity(
                                        senderPattern = "580855",
                                        targetFolderId = "advertising",
                                        explanation = "سامانه پیامک تبلیغاتی ۵۸۰۸۵۵"
                                    ),
                                    SenderRuleEntity(
                                        senderPattern = "9821*",
                                        targetFolderId = "advertising",
                                        explanation = "خطوط تبلیغاتی ثابت تهران"
                                    ),
                                    SenderRuleEntity(
                                        senderPattern = "Bank*",
                                        targetFolderId = "bank",
                                        explanation = "پیامک‌های تراکنش بانکی"
                                    ),
                                    SenderRuleEntity(
                                        senderPattern = "1000*",
                                        targetFolderId = "services",
                                        explanation = "سامانه‌های خدماتی و اداری"
                                    )
                                )
                                for (rule in defaultRules) {
                                    database.senderRuleDao().insertRule(rule)
                                }
                            }
                            preferences.markDefaultsSeeded()
                        }

                        // Background purge maintenance
                        val otpHours = preferences.getAutoDeleteOtpHours()
                        if (otpHours > 0) {
                            smsRepository.purgeExpiredOtps(otpHours)
                        }
                        smsRepository.purgeExpiredTrash(30)
                    } catch (e: Exception) {
                        Log.e("SmsGramApp", "Error during data seeding or purge", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsGramApp", "Error in seedInitialData", e)
            }
        }
    }
}
