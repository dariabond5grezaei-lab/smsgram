package com.example.ui.screens

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.example.data.model.AppLanguage
import com.example.data.model.ConversationItem
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.LocaleUtils
import com.example.ui.components.BottomTab
import com.example.ui.components.ConversationListItem
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.domain.util.orderConversations
import com.example.ui.components.ConversationSkeletonList
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EditFolderDialog
import com.example.ui.components.FolderReorderDialog
import com.example.ui.components.FolderTabsRow
import com.example.ui.components.MuteFolderDialog
import com.example.ui.components.SmsGramBottomBar
import com.example.ui.components.SmsGramEmptyState
import com.example.ui.components.SmsGramSearchBar
import com.example.ui.locale.appStrings
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SmsViewModel
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SmsViewModel,
    onOpenConversation: (Long, String, String?) -> Unit,
    onComposeNew: () -> Unit
) {
    val context = LocalContext.current
    val strings = appStrings()

    val currentLanguage by viewModel.languageFlow.collectAsState()
    val conversations by viewModel.rawConversations.collectAsState()
    val folders by viewModel.foldersFlow.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val usePersianDigits by viewModel.persianDigitsFlow.collectAsState()

    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedThreads by viewModel.selectedThreads.collectAsState()
    val isDark by viewModel.isDarkFlow.collectAsState()

    var sortBy by remember { mutableStateOf("newest") } // newest, oldest, name
    var sortMenuOpen by remember { mutableStateOf(false) }
    var filterUnreadOnly by remember { mutableStateOf(false) }

    // Dialog state for Creating Rule from conversation item
    var ruleDialogAddress by remember { mutableStateOf<String?>(null) }
    var ruleTargetFolderId by remember { mutableStateOf("advertising") }

    // Dialog state for Moving conversation to folder
    var moveDialogThreadId by remember { mutableStateOf<Long?>(null) }
    var moveTargetFolderId by remember { mutableStateOf<String?>("all") }
    var isBatchMoveDialogOpen by remember { mutableStateOf(false) }
    var isBatchDeleteDialogOpen by remember { mutableStateOf(false) }
    var showSwipeHint by remember {
        mutableStateOf(!context.getSharedPreferences("smsgram_prefs", Context.MODE_PRIVATE).getBoolean("has_seen_swipe_hint", false))
    }

    // Check if SMSGRAM is Default SMS App
    val isDefaultSmsApp = remember(conversations) {
        val defaultPackage = Telephony.Sms.getDefaultSmsPackage(context)
        defaultPackage == context.packageName
    }

    val defaultRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshAll()
    }

    // Filter conversations by search, folder, sorting, and unread filter
    val filteredConversations = remember(conversations, selectedFolderId, searchQuery, sortBy, filterUnreadOnly) {
        conversations.filter { conv ->
            val matchesFolder = if (selectedFolderId == "all") {
                !conv.isManuallyCategorized && conv.customFolderId != "spam"
            } else {
                conv.customFolderId == selectedFolderId
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                conv.displayName.contains(searchQuery, ignoreCase = true) ||
                        conv.address.contains(searchQuery, ignoreCase = true) ||
                        conv.snippet.contains(searchQuery, ignoreCase = true)
            }
            val matchesUnread = if (filterUnreadOnly) conv.unreadCount > 0 else true
            matchesFolder && matchesSearch && matchesUnread
        }.sortedWith { a, b ->
            // Pinned chats always stay on top
            if (a.isPinned && !b.isPinned) -1
            else if (!a.isPinned && b.isPinned) 1
            else {
                when (sortBy) {
                    "oldest" -> a.dateEpoch.compareTo(b.dateEpoch)
                    "name" -> {
                        val nameA = a.displayName.ifBlank { a.address }
                        val nameB = b.displayName.ifBlank { b.address }
                        nameA.compareTo(nameB, ignoreCase = true)
                    }
                    else -> b.dateEpoch.compareTo(a.dateEpoch) // newest
                }
            }
        }
    }

    // Folder unread counts
    val folderCounts = remember(conversations) {
        val map = mutableMapOf<String, Int>()
        map["all"] = conversations.filter { !it.isManuallyCategorized && it.customFolderId != "spam" }.sumOf { it.unreadCount }
        for (conv in conversations) {
            val f = conv.customFolderId ?: "all"
            if (f != "all") {
                map[f] = (map[f] ?: 0) + conv.unreadCount
            }
        }
        map
    }

    val totalUnread = conversations.sumOf { it.unreadCount }

    val isLoadingConversations by viewModel.isLoadingConversations.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var editingFolder by remember { mutableStateOf<com.example.data.local.FolderEntity?>(null) }
    var mutingFolderId by remember { mutableStateOf<String?>(null) }
    var markingReadFolderId by remember { mutableStateOf<String?>(null) }
    var deletingFolderId by remember { mutableStateOf<String?>(null) }
    var hidingFolderId by remember { mutableStateOf<String?>(null) }
    var showReorderDialog by remember { mutableStateOf(false) }

    // Split search results into Conversations and Messages
    val searchConversations = remember(filteredConversations, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else filteredConversations.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true)
        }
    }

    val searchMessages = remember(filteredConversations, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else filteredConversations.filter {
            it.snippet.contains(searchQuery, ignoreCase = true) &&
                    !it.displayName.contains(searchQuery, ignoreCase = true) &&
                    !it.address.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (isSelectionMode) {
                        // Telegram & Material 3 Contextual Action Top Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.clearSelection() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.cancel,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            val countDigits = if (usePersianDigits) JalaliCalendar.toPersianDigits(selectedThreads.size.toString()) else selectedThreads.size.toString()
                            val countLabel = String.format(strings.selectedConversationsCount, countDigits)
                            Text(
                                text = countLabel,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            val containsUnread = selectedThreads.any { tId ->
                                conversations.find { it.threadId == tId }?.unreadCount ?: 0 > 0
                            }
                            val containsUnpinned = selectedThreads.any { tId ->
                                !(conversations.find { it.threadId == tId }?.isPinned ?: false)
                            }
                            val isAnySelected = selectedThreads.isNotEmpty()

                            // Pin / Unpin
                            IconButton(
                                onClick = {
                                    if (isAnySelected) {
                                        viewModel.batchTogglePin(selectedThreads.toList(), containsUnpinned)
                                        viewModel.clearSelection()
                                    }
                                },
                                enabled = isAnySelected,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = if (containsUnpinned) strings.pinConversation else strings.unpinConversation,
                                    tint = if (isAnySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Mark as Read / Unread
                            IconButton(
                                onClick = {
                                    if (isAnySelected) {
                                        if (containsUnread) {
                                            viewModel.batchMarkAsRead(selectedThreads.toList())
                                        } else {
                                            viewModel.batchMarkAsUnread(selectedThreads.toList())
                                        }
                                        viewModel.clearSelection()
                                    }
                                },
                                enabled = isAnySelected,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = if (containsUnread) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread,
                                    contentDescription = if (containsUnread) strings.markAsRead else strings.markAsUnread,
                                    tint = if (isAnySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Move to Folder
                            IconButton(
                                onClick = {
                                    if (isAnySelected) {
                                        isBatchMoveDialogOpen = true
                                    }
                                },
                                enabled = isAnySelected,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DriveFileMove,
                                    contentDescription = strings.folders,
                                    tint = if (isAnySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Delete with Undo
                            IconButton(
                                onClick = {
                                    if (isAnySelected) {
                                        val idsToDelete = selectedThreads.toList()
                                        val count = idsToDelete.size
                                        viewModel.batchDeleteConversations(idsToDelete)
                                        viewModel.clearSelection()
                                        scope.launch {
                                            val msg = LocaleUtils.formatPluralConversations(count, currentLanguage == AppLanguage.PERSIAN)
                                            val action = strings.undo
                                            val result = snackbarHostState.showSnackbar(
                                                message = msg,
                                                actionLabel = action,
                                                duration = SnackbarDuration.Long
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.restoreThreads(idsToDelete)
                                            }
                                        }
                                    }
                                },
                                enabled = isAnySelected,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = strings.delete,
                                    tint = if (isAnySelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Select All / Deselect All
                            val isAllSelected = selectedThreads.size == filteredConversations.size && filteredConversations.isNotEmpty()
                            IconButton(
                                onClick = {
                                    if (isAllSelected) {
                                        viewModel.clearSelection()
                                    } else {
                                        viewModel.selectAllThreads(filteredConversations.map { it.threadId })
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAllSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isAllSelected) strings.deselectAll else strings.selectAll,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // Telegram Top Bar: "Edit" on left, centered Title with inline unread badge, New Message icon on right
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Leading action: Select
                            Text(
                                text = strings.select,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.setSelectionMode(true)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Centered Title with inline badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = strings.allChats,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (totalUnread > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val unreadCount = if (usePersianDigits) JalaliCalendar.toPersianDigits(totalUnread.toString()) else totalUnread.toString()
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = unreadCount,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Sun / Moon Dark Mode Toggle Button
                            IconButton(
                                onClick = { viewModel.toggleDark() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                androidx.compose.animation.AnimatedContent(
                                    targetState = isDark,
                                    transitionSpec = {
                                        (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) +
                                                androidx.compose.animation.scaleIn(initialScale = 0.8f, animationSpec = androidx.compose.animation.core.tween(250)))
                                            .togetherWith(
                                                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250)) +
                                                        androidx.compose.animation.scaleOut(targetScale = 0.8f, animationSpec = androidx.compose.animation.core.tween(250))
                                            )
                                    },
                                    label = "SunMoonToggle"
                                ) { darkState ->
                                    if (darkState) {
                                        Icon(
                                            imageVector = Icons.Default.WbSunny,
                                            contentDescription = strings.switchToLight,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.NightsStay,
                                            contentDescription = strings.switchToDark,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // Sort button & menu
                            Box {
                                IconButton(
                                    onClick = { sortMenuOpen = !sortMenuOpen },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_sort_descending),
                                        contentDescription = strings.sort,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = sortMenuOpen,
                                    onDismissRequest = { sortMenuOpen = false }
                                ) {
                                    // Group 1: Filter / Show
                                    Text(
                                        text = strings.show,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )

                                    DropdownMenuItem(
                                        text = { Text(strings.allMessages, fontSize = 16.sp) },
                                        onClick = {
                                            filterUnreadOnly = false
                                            sortMenuOpen = false
                                        },
                                        leadingIcon = if (!filterUnreadOnly) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    )

                                    DropdownMenuItem(
                                        text = { Text(strings.unread, fontSize = 16.sp) },
                                        onClick = {
                                            filterUnreadOnly = true
                                            sortMenuOpen = false
                                        },
                                        leadingIcon = if (filterUnreadOnly) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    // Group 2: Sort / Order
                                    Text(
                                        text = strings.order,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )

                                    DropdownMenuItem(
                                        text = { Text(strings.newest, fontSize = 16.sp) },
                                        onClick = {
                                            sortBy = "newest"
                                            sortMenuOpen = false
                                        },
                                        leadingIcon = if (sortBy == "newest") {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    )

                                    DropdownMenuItem(
                                        text = { Text(strings.oldest, fontSize = 16.sp) },
                                        onClick = {
                                            sortBy = "oldest"
                                            sortMenuOpen = false
                                        },
                                        leadingIcon = if (sortBy == "oldest") {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    )
                                }
                            }

                            // Trailing action: New Message Pencil Icon
                            IconButton(
                                onClick = onComposeNew,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = strings.newMessage,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Design System Search Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            SmsGramSearchBar(
                                query = searchQuery,
                                onQueryChange = { viewModel.setSearchQuery(it) },
                                placeholder = strings.searchHint
                            )
                        }

                        // iOS Horizontal Folder Tabs (Only shown if folders exist)
                        if (folders.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            ) {
                                FolderTabsRow(
                                    folders = folders,
                                    selectedFolderId = selectedFolderId,
                                    folderCounts = folderCounts,
                                    usePersianNumerals = usePersianDigits,
                                    onSelectFolder = { folderId ->
                                        viewModel.selectFolder(folderId)
                                    },
                                    onReorderFolders = {
                                        showReorderDialog = true
                                    },
                                    onEditFolder = { folder ->
                                        editingFolder = folder
                                    },
                                    onMuteAll = { folderId ->
                                        mutingFolderId = folderId
                                    },
                                    onMarkFolderAsRead = { folderId ->
                                        markingReadFolderId = folderId
                                    },
                                    onDeleteFolder = { folderId ->
                                        deletingFolderId = folderId
                                    },
                                    onHideFolder = { folderId ->
                                        hidingFolderId = folderId
                                    }
                                )
                            }
                        }

                        // Render Folder Action Dialogs
                        editingFolder?.let { folder ->
                            EditFolderDialog(
                                folder = folder,
                                onDismiss = { editingFolder = null },
                                onConfirm = { nameFa, nameEn ->
                                    viewModel.renameFolder(folder.id, nameFa, nameEn) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(strings.folderRenamedSnackbar)
                                        }
                                    }
                                    editingFolder = null
                                }
                            )
                        }

                        mutingFolderId?.let { folderId ->
                            MuteFolderDialog(
                                folderId = folderId,
                                onDismiss = { mutingFolderId = null },
                                onConfirm = { durationMs ->
                                    viewModel.muteFolder(folderId, durationMs) { count ->
                                        scope.launch {
                                            val msg = String.format(strings.conversationsAffectedSnackbar, count)
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                    mutingFolderId = null
                                }
                            )
                        }

                        markingReadFolderId?.let { folderId ->
                            ConfirmActionDialog(
                                title = strings.confirmMarkAllReadTitle,
                                body = strings.confirmMarkAllReadBody,
                                onDismiss = { markingReadFolderId = null },
                                onConfirm = {
                                    viewModel.markFolderAsRead(folderId) { count ->
                                        scope.launch {
                                            val msg = String.format(strings.conversationsAffectedSnackbar, count)
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                    markingReadFolderId = null
                                }
                            )
                        }

                        deletingFolderId?.let { folderId ->
                            ConfirmActionDialog(
                                title = strings.confirmDeleteFolderTitle,
                                body = strings.confirmDeleteFolderBody,
                                isDanger = true,
                                onDismiss = { deletingFolderId = null },
                                onConfirm = {
                                    viewModel.deleteFolder(folderId) { count ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(strings.folderDeletedSnackbar)
                                        }
                                    }
                                    deletingFolderId = null
                                }
                            )
                        }

                        hidingFolderId?.let { folderId ->
                            ConfirmActionDialog(
                                title = strings.confirmHideFolderTitle,
                                body = strings.confirmHideFolderBody,
                                isDanger = true,
                                onDismiss = { hidingFolderId = null },
                                onConfirm = {
                                    viewModel.setFolderHidden(folderId, isHidden = true) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(strings.folderHiddenSnackbar)
                                        }
                                    }
                                    hidingFolderId = null
                                }
                            )
                        }

                        if (showReorderDialog) {
                            FolderReorderDialog(
                                folders = folders,
                                onDismiss = { showReorderDialog = false },
                                onSaveOrder = { orders ->
                                    viewModel.setFolderOrder(orders)
                                    showReorderDialog = false
                                }
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                // Elevated Floating Dock for Selection Actions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(
                            0.5.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 560.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        // Mark as Read / Unread
                        val containsUnread = remember(selectedThreads, conversations) {
                            selectedThreads.any { tId ->
                                conversations.find { it.threadId == tId }?.unreadCount ?: 0 > 0
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = selectedThreads.isNotEmpty()) {
                                    if (containsUnread) {
                                        viewModel.batchMarkAsRead(selectedThreads.toList())
                                    } else {
                                        viewModel.batchMarkAsUnread(selectedThreads.toList())
                                    }
                                    viewModel.clearSelection()
                                }
                                .padding(4.dp)
                        ) {
                            val markLabel = if (containsUnread) strings.markAsRead else strings.markAsUnread
                            val isEnabled = selectedThreads.isNotEmpty()
                            val icon = if (containsUnread) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread
                            Icon(
                                imageVector = icon,
                                contentDescription = markLabel,
                                tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = markLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        // Pin / Unpin
                        val containsUnpinned = remember(selectedThreads, conversations) {
                            selectedThreads.any { tId ->
                                !(conversations.find { it.threadId == tId }?.isPinned ?: false)
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = selectedThreads.isNotEmpty()) {
                                    viewModel.batchTogglePin(selectedThreads.toList(), containsUnpinned)
                                    viewModel.clearSelection()
                                }
                                .padding(4.dp)
                        ) {
                            val pinLabel = if (containsUnpinned) strings.pinConversation else strings.unpinConversation
                            val isEnabled = selectedThreads.isNotEmpty()
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = pinLabel,
                                tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = pinLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        // Move to Folder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = selectedThreads.isNotEmpty()) {
                                    isBatchMoveDialogOpen = true
                                }
                                .padding(4.dp)
                        ) {
                            val folderLabel = strings.folders
                            val isEnabled = selectedThreads.isNotEmpty()
                            Icon(
                                imageVector = Icons.Default.DriveFileMove,
                                contentDescription = folderLabel,
                                tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = folderLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        // Delete with Undo Snackbar
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = selectedThreads.isNotEmpty()) {
                                    val idsToDelete = selectedThreads.toList()
                                    val count = idsToDelete.size
                                    viewModel.batchDeleteConversations(idsToDelete)
                                    viewModel.clearSelection()
                                    scope.launch {
                                        val msg = LocaleUtils.formatPluralConversations(count, currentLanguage == AppLanguage.PERSIAN)
                                        val action = strings.undo
                                        val result = snackbarHostState.showSnackbar(
                                            message = msg,
                                            actionLabel = action,
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreThreads(idsToDelete)
                                        }
                                    }
                                }
                                .padding(4.dp)
                        ) {
                            val deleteLabel = strings.delete
                            val isEnabled = selectedThreads.isNotEmpty()
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = deleteLabel,
                                tint = if (isEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = deleteLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Default SMS App Banner if not set
            if (!isDefaultSmsApp) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.setAsDefault,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = strings.defaultPromptDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                requestDefaultSmsApp(context, defaultRoleLauncher)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(strings.setAsDefault, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Swipe Gestures Discovery Hint Banner (shown only on first run)
            if (showSwipeHint && conversations.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.swipeActionsHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = {
                                showSwipeHint = false
                                context.getSharedPreferences("smsgram_prefs", Context.MODE_PRIVATE)
                                    .edit().putBoolean("has_seen_swipe_hint", true).apply()
                            }
                        ) {
                            Text(strings.dismissHint, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Pull to Refresh & Conversation List
            PullToRefreshBox(
                isRefreshing = isLoadingConversations,
                onRefresh = { viewModel.refreshConversations(forceRefresh = true) },
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                if (isLoadingConversations && conversations.isEmpty()) {
                    ConversationSkeletonList(count = 7)
                } else if (filteredConversations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (searchQuery.isNotEmpty()) {
                            val title = String.format(strings.noSearchResultFor, searchQuery)
                            val subtitle = strings.noSearchResultSub
                            SmsGramEmptyState(
                                icon = Icons.Default.Search,
                                title = title,
                                subtitle = subtitle
                            )
                        } else {
                            SmsGramEmptyState(
                                icon = Icons.AutoMirrored.Filled.Chat,
                                title = strings.noMessagesFound,
                                subtitle = strings.noMessagesSub
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (searchQuery.isNotBlank()) {
                            // Section 1: Matching Conversations
                            if (searchConversations.isNotEmpty()) {
                                item {
                                    Text(
                                        text = strings.conversationsSection,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                items(items = searchConversations, key = { "conv_${it.threadId}" }, contentType = { "conv_item" }) { conv ->
                                    ConversationSwipeRow(
                                        conv = conv,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = selectedThreads.contains(conv.threadId),
                                        usePersianDigits = usePersianDigits,
                                        currentLanguage = currentLanguage,
                                        viewModel = viewModel,
                                        onOpen = { onOpenConversation(conv.threadId, conv.address, conv.contactName) },
                                        onToggle = {
                                            viewModel.toggleThreadSelection(conv.threadId)
                                        },
                                        onCreateRule = { ruleDialogAddress = conv.address },
                                        onMoveToFolder = {
                                            moveDialogThreadId = conv.threadId
                                            moveTargetFolderId = conv.customFolderId ?: "all"
                                        },
                                        onDeleteWithUndo = {
                                            val tId = conv.threadId
                                            viewModel.deleteConversation(tId)
                                            scope.launch {
                                                val res = snackbarHostState.showSnackbar(
                                                    message = strings.conversationMovedToTrash,
                                                    actionLabel = strings.undo,
                                                    duration = SnackbarDuration.Long
                                                )
                                                if (res == SnackbarResult.ActionPerformed) {
                                                    viewModel.restoreThreads(listOf(tId))
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            // Section 2: Matching Messages
                            if (searchMessages.isNotEmpty()) {
                                item {
                                    Text(
                                        text = strings.messagesSection,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                items(items = searchMessages, key = { "msg_${it.threadId}" }) { conv ->
                                    ConversationSwipeRow(
                                        conv = conv,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = selectedThreads.contains(conv.threadId),
                                        usePersianDigits = usePersianDigits,
                                        currentLanguage = currentLanguage,
                                        viewModel = viewModel,
                                        onOpen = { onOpenConversation(conv.threadId, conv.address, conv.contactName) },
                                        onToggle = {
                                            viewModel.toggleThreadSelection(conv.threadId)
                                        },
                                        onCreateRule = { ruleDialogAddress = conv.address },
                                        onMoveToFolder = {
                                            moveDialogThreadId = conv.threadId
                                            moveTargetFolderId = conv.customFolderId ?: "all"
                                        },
                                        onDeleteWithUndo = {
                                            val tId = conv.threadId
                                            viewModel.deleteConversation(tId)
                                            scope.launch {
                                                val res = snackbarHostState.showSnackbar(
                                                    message = strings.conversationMovedToTrash,
                                                    actionLabel = strings.undo,
                                                    duration = SnackbarDuration.Long
                                                )
                                                if (res == SnackbarResult.ActionPerformed) {
                                                    viewModel.restoreThreads(listOf(tId))
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            items(
                                items = filteredConversations,
                                key = { it.threadId },
                                contentType = { "conversation_row" }
                            ) { conv ->
                                ConversationSwipeRow(
                                    conv = conv,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedThreads.contains(conv.threadId),
                                    usePersianDigits = usePersianDigits,
                                    currentLanguage = currentLanguage,
                                    viewModel = viewModel,
                                    onOpen = { onOpenConversation(conv.threadId, conv.address, conv.contactName) },
                                    onToggle = {
                                        viewModel.toggleThreadSelection(conv.threadId)
                                    },
                                    onCreateRule = { ruleDialogAddress = conv.address },
                                    onMoveToFolder = {
                                        moveDialogThreadId = conv.threadId
                                        moveTargetFolderId = conv.customFolderId ?: "all"
                                    },
                                    onDeleteWithUndo = {
                                        val tId = conv.threadId
                                        viewModel.deleteConversation(tId)
                                        scope.launch {
                                            val res = snackbarHostState.showSnackbar(
                                                message = strings.conversationMovedToTrash,
                                                actionLabel = strings.undo,
                                                duration = SnackbarDuration.Long
                                            )
                                            if (res == SnackbarResult.ActionPerformed) {
                                                viewModel.restoreThreads(listOf(tId))
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Create Rule
    ruleDialogAddress?.let { addr ->
        AlertDialog(
            onDismissRequest = { ruleDialogAddress = null },
            title = { Text(strings.createRule, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = String.format(strings.routeIncomingToFolder, addr),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    folders.filter { it.id != "all" }.forEach { f ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { ruleTargetFolderId = f.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (ruleTargetFolderId == f.id),
                                onClick = { ruleTargetFolderId = f.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val folderTitle = if (currentLanguage == AppLanguage.ENGLISH && f.nameEn.isNotBlank()) f.nameEn else f.nameFa
                            Text(text = folderTitle)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createSenderRule(
                            senderPattern = addr,
                            targetFolderId = ruleTargetFolderId,
                            explanation = String.format(strings.ruleForSender, addr)
                        )
                        ruleDialogAddress = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { ruleDialogAddress = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Dialog: Move Conversation to Folder
    moveDialogThreadId?.let { tId ->
        AlertDialog(
            onDismissRequest = { moveDialogThreadId = null },
            title = { Text(strings.moveToFolder, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = strings.selectFolderToMove,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    folders.forEach { f ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { moveTargetFolderId = f.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (moveTargetFolderId == f.id),
                                onClick = { moveTargetFolderId = f.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val folderTitle = if (currentLanguage == AppLanguage.ENGLISH && f.nameEn.isNotBlank()) f.nameEn else f.nameFa
                            Text(text = folderTitle)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val folderIdToSave = if (moveTargetFolderId == "all") null else moveTargetFolderId
                        viewModel.setConversationCustomFolder(tId, folderIdToSave)
                        moveDialogThreadId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { moveDialogThreadId = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Dialog: Batch Move to Folder
    if (isBatchMoveDialogOpen) {
        var batchTargetFolderId by remember { mutableStateOf("all") }
        val countDigits = if (usePersianDigits) JalaliCalendar.toPersianDigits(selectedThreads.size.toString()) else selectedThreads.size.toString()
        AlertDialog(
            onDismissRequest = { isBatchMoveDialogOpen = false },
            title = { Text(strings.moveSelectedConversations, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = String.format(strings.targetFolderForCount, countDigits),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    folders.forEach { f ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { batchTargetFolderId = f.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (batchTargetFolderId == f.id),
                                onClick = { batchTargetFolderId = f.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val folderTitle = if (currentLanguage == AppLanguage.ENGLISH && f.nameEn.isNotBlank()) f.nameEn else f.nameFa
                            Text(text = folderTitle)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val folderIdToSave = if (batchTargetFolderId == "all") null else batchTargetFolderId
                        viewModel.batchMoveToFolder(selectedThreads.toList(), folderIdToSave)
                        isBatchMoveDialogOpen = false
                        viewModel.clearSelection()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { isBatchMoveDialogOpen = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationSwipeRow(
    conv: ConversationItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    usePersianDigits: Boolean,
    currentLanguage: AppLanguage,
    viewModel: SmsViewModel,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onCreateRule: () -> Unit,
    onMoveToFolder: () -> Unit,
    onDeleteWithUndo: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val strings = appStrings()
    val calendarSystem by viewModel.calendarSystemFlow.collectAsState()

    if (isSelectionMode) {
        ConversationListItem(
            conversation = conv,
            usePersianNumerals = usePersianDigits,
            calendarSystem = calendarSystem,
            isSelectionMode = true,
            isSelected = isSelected,
            onClick = onOpen,
            onToggleSelect = onToggle,
            onPinToggle = { isPinned -> viewModel.setConversationPinned(conv.threadId, isPinned) },
            onDelete = onDeleteWithUndo,
            onCreateRule = onCreateRule,
            onMoveToFolder = onMoveToFolder
        )
    } else {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        val isUnread = conv.unreadCount > 0
                        viewModel.toggleThreadReadStatus(conv.threadId, markAsRead = isUnread)
                        false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDeleteWithUndo()
                        true
                    }
                    SwipeToDismissBoxValue.Settled -> false
                }
            }
        )

        val markReadActionLabel = if (conv.unreadCount > 0) strings.markAsRead else strings.markAsUnread
        val deleteActionLabel = strings.deleteConversationAction

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            modifier = Modifier.semantics {
                customActions = listOf(
                    CustomAccessibilityAction(label = markReadActionLabel) {
                        val isUnread = conv.unreadCount > 0
                        viewModel.toggleThreadReadStatus(conv.threadId, markAsRead = isUnread)
                        true
                    },
                    CustomAccessibilityAction(label = deleteActionLabel) {
                        onDeleteWithUndo()
                        true
                    }
                )
            },
            backgroundContent = {
                val isSwiping = dismissState.currentValue != SwipeToDismissBoxValue.Settled ||
                        dismissState.targetValue != SwipeToDismissBoxValue.Settled

                if (isSwiping) {
                    val direction = dismissState.dismissDirection
                    val isReadSwipe = (direction == SwipeToDismissBoxValue.StartToEnd)
                    val bgColor = if (isReadSwipe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    val icon = if (isReadSwipe) {
                        if (conv.unreadCount > 0) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread
                    } else {
                        Icons.Default.Delete
                    }
                    val alignment = if (isReadSwipe) Alignment.CenterStart else Alignment.CenterEnd

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgColor)
                            .padding(horizontal = 24.dp),
                        contentAlignment = alignment
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        ) {
            ConversationListItem(
                conversation = conv,
                usePersianNumerals = usePersianDigits,
                calendarSystem = calendarSystem,
                isSelectionMode = false,
                isSelected = false,
                onClick = onOpen,
                onToggleSelect = onToggle,
                onPinToggle = { isPinned -> viewModel.setConversationPinned(conv.threadId, isPinned) },
                onDelete = onDeleteWithUndo,
                onCreateRule = onCreateRule,
                onMoveToFolder = onMoveToFolder
            )
        }
    }
}

private fun requestDefaultSmsApp(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_SMS)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                if (intent != null) {
                    launcher.launch(intent)
                    return
                }
            }
        }
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
        launcher.launch(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
