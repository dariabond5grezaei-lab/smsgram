package com.example.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.SmsGramApp
import com.example.ui.components.BottomTab
import com.example.ui.components.IncomingBanner
import com.example.ui.components.SmsGramBottomBar
import com.example.ui.screens.BlockedContactsScreen
import com.example.ui.screens.CannedTemplatesScreen
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewMessageScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.SavedMessagesScreen
import com.example.ui.screens.ScheduledSmsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SmsViewModel

@Composable
fun SmsGramAppShell(
    viewModel: SmsViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val rawConversations by viewModel.rawConversations.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val totalUnread = androidx.compose.runtime.remember(rawConversations) {
        rawConversations.sumOf { it.unreadCount }
    }

    val currentTab = when (currentScreen) {
        is ScreenDestination.Home -> BottomTab.CHATS
        is ScreenDestination.Folders -> BottomTab.FOLDERS
        is ScreenDestination.SavedMessages -> BottomTab.NOTES
        is ScreenDestination.Settings -> BottomTab.SETTINGS
        else -> null
    }

    val preferences = androidx.compose.runtime.remember {
        viewModel.getApplication<SmsGramApp>().preferences
    }
    val isPrivacyMode = androidx.compose.runtime.remember {
        preferences.isNotificationPrivacy()
    }
    val isAppLockActive = androidx.compose.runtime.remember {
        preferences.isAppLockEnabled()
    }

    Scaffold(
        bottomBar = {
            if (currentTab != null && !isSelectionMode) {
                SmsGramBottomBar(
                    currentTab = currentTab,
                    unreadChatsCount = totalUnread,
                    onTabSelected = { tab ->
                        when (tab) {
                            BottomTab.CHATS -> viewModel.navigateTo(ScreenDestination.Home)
                            BottomTab.FOLDERS -> viewModel.navigateTo(ScreenDestination.Folders)
                            BottomTab.NOTES -> viewModel.navigateTo(ScreenDestination.SavedMessages)
                            BottomTab.SETTINGS -> viewModel.navigateTo(ScreenDestination.Settings)
                        }
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "screen_transitions"
            ) { dest ->
                when (dest) {
                    is ScreenDestination.Home -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onOpenConversation = { threadId, address, contactName ->
                                viewModel.navigateTo(
                                    ScreenDestination.Conversation(threadId, address, contactName)
                                )
                            },
                            onComposeNew = {
                                viewModel.navigateTo(ScreenDestination.NewMessage())
                            }
                        )
                    }
                    is ScreenDestination.Conversation -> {
                        ConversationScreen(
                            threadId = dest.threadId,
                            address = dest.address,
                            contactName = dest.contactName,
                            initialBody = dest.initialBody,
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is ScreenDestination.NewMessage -> {
                        NewMessageScreen(
                            viewModel = viewModel,
                            initialRecipient = dest.initialRecipient,
                            initialBody = dest.initialBody,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is ScreenDestination.SavedMessages -> {
                        SavedMessagesScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is ScreenDestination.Folders -> {
                        FoldersScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is ScreenDestination.Rules -> {
                        RulesScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is ScreenDestination.ScheduledSms -> {
                        ScheduledSmsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is ScreenDestination.Trash -> {
                        TrashScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is ScreenDestination.CannedTemplates -> {
                        CannedTemplatesScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is ScreenDestination.BlockedContacts -> {
                        BlockedContactsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    is ScreenDestination.Settings -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }
            }

            IncomingBanner(
                isPrivacyMode = isPrivacyMode,
                isAppLockActive = isAppLockActive,
                onOpenThread = { threadId, sender ->
                    viewModel.navigateTo(
                        ScreenDestination.Conversation(threadId, sender, null)
                    )
                }
            )
        }
    }
}
