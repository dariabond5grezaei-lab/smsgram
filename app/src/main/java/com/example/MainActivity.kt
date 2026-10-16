package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.components.LockScreen
import com.example.ui.locale.EnglishStrings
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.LocalAppStrings
import com.example.ui.locale.PersianStrings
import com.example.ui.locale.appStrings
import com.example.ui.screens.BlockedContactsScreen
import com.example.ui.screens.CannedTemplatesScreen
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewMessageScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.SavedMessagesScreen
import com.example.ui.screens.ScheduledSmsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.shell.SmsGramAppShell
import com.example.ui.theme.SmsGramTheme
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SmsViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: SmsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val themeFamily by viewModel.themeFamilyFlow.collectAsState()
            val isDark by viewModel.isDarkFlow.collectAsState()
            val accentColor by viewModel.accentColorFlow.collectAsState()
            val fontId by viewModel.fontIdFlow.collectAsState()
            val isUnlocked by viewModel.isUnlocked.collectAsState()
            val hasPermissions by viewModel.hasPermissions.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

            // Role launcher for Default SMS App
            val roleLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                viewModel.refreshAll()
            }

            // Request necessary permissions launcher
            val permissionsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                viewModel.checkPermissions()
                viewModel.refreshAll()

                // Check and request Default SMS App role safely
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val roleManager = getSystemService(android.app.role.RoleManager::class.java)
                        if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_SMS) &&
                            !roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_SMS)
                        ) {
                            val roleIntent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_SMS)
                            if (roleIntent != null) {
                                roleLauncher.launch(roleIntent)
                            }
                        }
                    } else {
                        val defaultSmsPackage = android.provider.Telephony.Sms.getDefaultSmsPackage(this@MainActivity)
                        if (defaultSmsPackage != packageName) {
                            val roleIntent = Intent(android.provider.Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                                putExtra(android.provider.Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                            }
                            roleLauncher.launch(roleIntent)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val language by viewModel.languageFlow.collectAsState()
            val isRtl = (language == AppLanguage.PERSIAN)
            val currentStrings = if (language == AppLanguage.PERSIAN) PersianStrings else EnglishStrings

            CompositionLocalProvider(
                LocalAppLanguage provides language,
                LocalAppStrings provides currentStrings
            ) {
                SmsGramTheme(
                    family = themeFamily,
                    isDark = isDark,
                    accent = accentColor,
                    fontId = fontId,
                    isRtl = isRtl
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                    if (!isOnboardingCompleted) {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onFinished = {
                                viewModel.setOnboardingCompleted(true)
                                viewModel.refreshAll()
                            }
                        )
                    } else if (!isUnlocked) {
                        // Locked Screen
                        LockScreen(
                            useBiometrics = viewModel.preferences.isUseBiometric(),
                            onUnlockWithPin = { pin -> viewModel.unlockApp(pin) },
                            onUnlockSuccess = { viewModel.unlockWithBiometrics() }
                        )
                    } else if (!hasPermissions) {
                        // Permission Request Fallback
                        PermissionRequiredScreen(
                            onRequestPermissions = {
                                val needed = mutableListOf(
                                    Manifest.permission.READ_SMS,
                                    Manifest.permission.SEND_SMS,
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.RECEIVE_MMS,
                                    Manifest.permission.RECEIVE_WAP_PUSH,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.READ_CONTACTS
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    needed.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                permissionsLauncher.launch(needed.toTypedArray())
                            }
                        )
                    } else {
                        // Normal App Navigation inside Shell
                        BackHandler(enabled = currentScreen !is ScreenDestination.Home) {
                            viewModel.navigateBack()
                        }

                        SmsGramAppShell(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

    override fun onResume() {
        super.onResume()
        if (viewModel.preferences.shouldLockOnResume()) {
            viewModel.lockApp()
        }
        applyWindowSecurityFlags()
        viewModel.checkPermissions()
        viewModel.refreshAll()
    }

    override fun onPause() {
        super.onPause()
        viewModel.preferences.recordPauseTime()
    }

    internal fun applyWindowSecurityFlags() {
        try {
            val shouldSecure = viewModel.preferences.isHideContentInRecents()
            if (shouldSecure) {
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        try {
            val navigateTo = intent.getStringExtra("navigate_to")
            if (navigateTo == "scheduled_sms") {
                intent.removeExtra("navigate_to")
                viewModel.navigateTo(ScreenDestination.ScheduledSms)
                return
            }

            val threadId = intent.getLongExtra("threadId", -1L)
            val address = intent.getStringExtra("address")

            if (threadId != -1L && !address.isNullOrBlank()) {
                intent.removeExtra("threadId")
                intent.removeExtra("address")
                viewModel.navigateTo(
                    ScreenDestination.Conversation(
                        threadId = threadId,
                        address = address,
                        contactName = null
                    )
                )
                return
            }

            val action = intent.action

            if (
                action != Intent.ACTION_SENDTO &&
                action != Intent.ACTION_SEND &&
                action != Intent.ACTION_VIEW
            ) {
                return
            }

            var targetRecipient: String? = null
            var targetBody: String? = null

            val uri = intent.data

            if (uri != null) {
                val scheme = uri.scheme?.lowercase()

                if (
                    scheme == "sms" ||
                    scheme == "smsto" ||
                    scheme == "mms" ||
                    scheme == "mmsto"
                ) {
                    val ssp = uri.schemeSpecificPart ?: ""
                    val rawNumber = ssp
                        .substringBefore("?")
                        .removePrefix("/")
                        .trim()

                    if (rawNumber.isNotBlank()) {
                        targetRecipient = android.net.Uri.decode(rawNumber)
                    }

                    // Safe extraction of body query parameter without throwing UnsupportedOperationException on opaque URIs
                    targetBody = try {
                        if (uri.isHierarchical) {
                            uri.getQueryParameter("body")
                        } else if (ssp.contains("?")) {
                            val queryString = ssp.substringAfter("?")
                            queryString.split("&")
                                .find { it.startsWith("body=", ignoreCase = true) }
                                ?.substringAfter("body=")
                                ?.let { android.net.Uri.decode(it) }
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                } else if (scheme == "tel") {
                    val ssp = uri.schemeSpecificPart ?: ""
                    val rawNumber = ssp.removePrefix("/").trim()
                    if (rawNumber.isNotBlank()) {
                        targetRecipient = android.net.Uri.decode(rawNumber)
                    }
                } else if (scheme == "content") {
                    // Handle content:// URIs from Contacts or MMS-SMS
                    try {
                        val uriStr = uri.toString().lowercase()
                        if (uriStr.contains("mms-sms") || uriStr.contains("conversations")) {
                            val tid = uri.lastPathSegment?.toLongOrNull()
                            if (tid != null && tid > 0) {
                                viewModel.navigateTo(
                                    ScreenDestination.Conversation(
                                        threadId = tid,
                                        address = "",
                                        contactName = null,
                                        initialBody = targetBody
                                    )
                                )
                                return
                            }
                        }

                        // Try direct query on the URI for Phone.NUMBER
                        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            var phoneCol = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (phoneCol == -1) {
                                phoneCol = cursor.getColumnIndex("data1")
                            }
                            if (phoneCol == -1) {
                                phoneCol = cursor.getColumnIndex("number")
                            }

                            if (phoneCol != -1 && cursor.moveToFirst()) {
                                targetRecipient = cursor.getString(phoneCol)
                            } else if (cursor.moveToFirst()) {
                                // If this is a Contacts entity, retrieve ID and lookup phone number
                                val idCol = cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
                                if (idCol != -1) {
                                    val contactId = cursor.getLong(idCol)
                                    contentResolver.query(
                                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER),
                                        "${android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                        arrayOf(contactId.toString()),
                                        null
                                    )?.use { phoneCursor ->
                                        if (phoneCursor.moveToFirst()) {
                                            targetRecipient = phoneCursor.getString(0)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        // ignore content query errors safely
                    }
                }
            }

            if (targetBody.isNullOrBlank()) {
                targetBody =
                    intent.getStringExtra(Intent.EXTRA_TEXT)
                        ?: intent.getStringExtra("sms_body")
                        ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)
            }

            if (targetRecipient.isNullOrBlank()) {
                targetRecipient =
                    intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                        ?: intent.getStringExtra("address")
                        ?: intent.getStringExtra("recipient")
                        ?: intent.getStringExtra("phone")
                        ?: intent.getStringArrayExtra(Intent.EXTRA_EMAIL)?.firstOrNull()
            }

            if (!targetRecipient.isNullOrBlank()) {
                val cleanRecipient = com.example.domain.util.SmartTextParser.normalizeDigits(targetRecipient)
                    .removePrefix("smsto:")
                    .removePrefix("sms:")
                    .removePrefix("tel:")
                    .replace(" ", "")
                    .replace("-", "")
                    .replace("(", "")
                    .replace(")", "")
                    .trim()

                if (cleanRecipient.isNotBlank()) {
                    viewModel.openConversationForRecipient(
                        recipient = cleanRecipient,
                        initialBody = targetBody
                    )
                } else {
                    viewModel.navigateTo(
                        ScreenDestination.NewMessage(
                            initialRecipient = null,
                            initialBody = targetBody
                        )
                    )
                }
            } else if (!targetBody.isNullOrBlank()) {
                viewModel.navigateTo(
                    ScreenDestination.NewMessage(
                        initialRecipient = null,
                        initialBody = targetBody
                    )
                )
            } else if (action == Intent.ACTION_SENDTO || action == Intent.ACTION_SEND) {
                viewModel.navigateTo(
                    ScreenDestination.NewMessage(
                        initialRecipient = null,
                        initialBody = null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun PermissionRequiredScreen(onRequestPermissions: () -> Unit) {
    val strings = appStrings()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = strings.permissionRequiredTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = strings.permissionRequiredDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRequestPermissions) {
                Text(strings.grantPermissions, fontWeight = FontWeight.Bold)
            }
        }
    }
}
