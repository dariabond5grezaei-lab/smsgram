package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.example.domain.util.ClipboardHelper
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.findMatches
import com.example.domain.util.shouldShowUnknownCard
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.SmsMessageItem
import com.example.domain.util.isSameShamsiDayAs
import com.example.domain.util.toShamsiHeader
import com.example.ui.components.ChatComposer
import com.example.ui.components.MessageBubble
import com.example.ui.components.ScheduleSheet
import com.example.data.local.ScheduleStatus
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.appStrings
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.AvatarPalette
import com.example.ui.theme.smsGramColors
import com.example.ui.viewmodel.SmsViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    threadId: Long,
    address: String,
    contactName: String?,
    initialBody: String? = null,
    viewModel: SmsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val strings = appStrings()
    val currentLanguage = LocalAppLanguage.current
    val colors = smsGramColors

    val messages by viewModel.activeMessages.collectAsState()
    val pinnedMetadata by viewModel.activePinnedMessage.collectAsState()
    val activeSims by viewModel.activeSims.collectAsState()
    val templates by viewModel.templatesFlow.collectAsState()
    val folders by viewModel.foldersFlow.collectAsState()
    val fontSize by viewModel.fontSizeFlow.collectAsState()
    val usePersianDigits by viewModel.persianDigitsFlow.collectAsState()
    val calendarSystem by viewModel.calendarSystemFlow.collectAsState()
    val pendingScheduledMessages by viewModel.pendingForCurrentConversation.collectAsState()

    LaunchedEffect(address) {
        viewModel.setCurrentConversationAddress(address)
    }

    var textInput by remember(address, initialBody) {
        mutableStateOf(initialBody.orEmpty())
    }
    var selectedSimIndex by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showSimManagerModal by remember { mutableStateOf(false) }

    // Dialog state for Scheduling SMS
    var showScheduleDialog by remember { mutableStateOf(false) }
    var scheduleDelayMinutes by remember { mutableIntStateOf(30) }

    // Dialog state for Creating Sender Rule
    var showRuleDialog by remember { mutableStateOf(false) }
    var ruleFolderId by remember { mutableStateOf("advertising") }

    // Phase C: Unknown Sender state
    val blockedContacts by viewModel.blockedContactsFlow.collectAsState()
    val isBlocked = remember(blockedContacts, address) { blockedContacts.any { it.address == address } }
    val isContact = !contactName.isNullOrBlank()
    val conversationMetadata by viewModel.database.conversationMetadataDao().getMetadataFlow(threadId).collectAsState(initial = null)
    val isMuted = conversationMetadata?.isMuted == true
    var isDismissed by remember(threadId) {
        mutableStateOf(viewModel.preferences.getUnknownDismissedThreads().contains(threadId.toString()))
    }
    val hasOutgoingMessage = remember(messages) { messages.any { it.isSentByUser } }
    val showUnknownCard = shouldShowUnknownCard(isContact, isBlocked, isMuted, isDismissed, hasOutgoingMessage)

    // Phase D: Search in Conversation state
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentSearchMatchIndex by remember { mutableIntStateOf(0) }
    val searchMatches = remember(messages, searchQuery) {
        findMatches(messages.map { it.body }, searchQuery)
    }

    LaunchedEffect(threadId) {
        viewModel.loadConversation(threadId)
    }

    var lastProcessedReceivedMessageId by remember(threadId) { mutableStateOf<Long?>(null) }

    LaunchedEffect(threadId, messages, activeSims) {
        if (activeSims.isEmpty()) return@LaunchedEffect
        
        val lastReceived = messages.lastOrNull { !it.isSentByUser }
        
        // If a new received message is found that we haven't processed yet
        if (lastReceived != null && lastReceived.id != lastProcessedReceivedMessageId) {
            val targetSubId = lastReceived.subId
            if (targetSubId != -1) {
                val idx = activeSims.indexOfFirst { it.subscriptionId == targetSubId }
                if (idx != -1) {
                    selectedSimIndex = idx
                    lastProcessedReceivedMessageId = lastReceived.id
                }
            }
        } else if (lastProcessedReceivedMessageId == null) {
            // No received messages processed yet (either list is empty or they are all sent by user)
            val preferredSubId = viewModel.getThreadPreferredSim(threadId)
            val targetSubId = if (preferredSubId != -1) {
                preferredSubId
            } else {
                viewModel.defaultSimIdFlow.value
            }
            
            if (targetSubId != -1) {
                val idx = activeSims.indexOfFirst { it.subscriptionId == targetSubId }
                if (idx != -1) {
                    selectedSimIndex = idx
                }
            }
            // Mark as processed (even if null) so we don't keep resetting if user changes SIM manually
            if (messages.isNotEmpty()) {
                lastProcessedReceivedMessageId = -1L // value representing "checked, no received messages"
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val gradientColors = AvatarPalette.getGradientFor(address, isDarkTheme)
    val snackbarHostState = remember { SnackbarHostState() }

    val displayName = contactName ?: address
    val initials = if (displayName.isNotBlank()) {
        displayName.trim().take(2).uppercase()
    } else "#"

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(gradientColors)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (contactName != null) address else strings.cellularSms,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = strings.back,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$address"))
                                    context.startActivity(intent)
                                }
                            ) {
                                 Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = strings.callContact,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    isSearchActive = !isSearchActive
                                    if (!isSearchActive) {
                                        searchQuery = ""
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = strings.searchInChat,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = strings.moreOptions,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(strings.createRule) },
                                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    onClick = {
                                        showRuleDialog = true
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.blockSender) },
                                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF3B30)) },
                                    onClick = {
                                        viewModel.blockContact(address, contactName)
                                        val blockMsg = String.format(strings.blockSenderAdded, address)
                                        Toast.makeText(context, blockMsg, Toast.LENGTH_SHORT).show()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.deleteAllMessages) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF3B30)) },
                                    onClick = {
                                        viewModel.deleteConversation(threadId)
                                        showMenu = false
                                        onBack()
                                    }
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    if (isSearchActive) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = {
                                            searchQuery = it
                                            currentSearchMatchIndex = 0
                                        },
                                        placeholder = { Text(strings.searchInChat, fontSize = 14.sp) },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        shape = RoundedCornerShape(20.dp)
                                    )

                                    val matchCount = searchMatches.size
                                    val currentMatchHuman = if (matchCount > 0) (currentSearchMatchIndex + 1) else 0
                                    val counterText = if (usePersianDigits) {
                                        JalaliCalendar.toPersianDigits("$currentMatchHuman از $matchCount")
                                    } else {
                                        "$currentMatchHuman of $matchCount"
                                    }

                                    Text(
                                        text = counterText,
                                        fontSize = 14.sp,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (searchMatches.isNotEmpty()) {
                                                currentSearchMatchIndex = if (currentSearchMatchIndex > 0) currentSearchMatchIndex - 1 else searchMatches.size - 1
                                                val targetItemIndex = searchMatches[currentSearchMatchIndex]
                                                scope.launch {
                                                    listState.animateScrollToItem(targetItemIndex)
                                                }
                                            }
                                        },
                                        enabled = searchMatches.isNotEmpty(),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Previous match",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (searchMatches.isNotEmpty()) {
                                                currentSearchMatchIndex = if (currentSearchMatchIndex < searchMatches.size - 1) currentSearchMatchIndex + 1 else 0
                                                val targetItemIndex = searchMatches[currentSearchMatchIndex]
                                                scope.launch {
                                                    listState.animateScrollToItem(targetItemIndex)
                                                }
                                            }
                                        },
                                        enabled = searchMatches.isNotEmpty(),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Next match",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            isSearchActive = false
                                            searchQuery = ""
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = strings.close,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = strings.searchLoadedOnly,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp, start = 4.dp)
                                )
                            }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                ChatComposer(
                    text = textInput,
                    onTextChanged = { textInput = it },
                    activeSims = activeSims,
                    selectedSimIndex = selectedSimIndex,
                    onSelectSimIndex = { selectedSimIndex = it },
                    templates = templates,
                    onSend = { content ->
                        val simSubId = activeSims.getOrNull(selectedSimIndex)?.subscriptionId ?: -1
                        viewModel.sendMessage(address, content, simSubId)
                        textInput = ""
                    },
                    onScheduleClick = {
                        showScheduleDialog = true
                    },
                    threadId = threadId,
                    onSetThreadDefaultSim = { subId ->
                        viewModel.setThreadPreferredSim(threadId, subId)
                    },
                    onOpenSimManager = {
                        showSimManagerModal = true
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phase C: Unknown Sender Card
            if (showUnknownCard) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.unknownNumber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = {
                                    viewModel.preferences.dismissUnknownSenderThread(threadId.toString())
                                    isDismissed = true
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.close,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // a. Add contact
                            OutlinedButton(
                                onClick = {
                                    val insertIntent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                                        type = ContactsContract.RawContacts.CONTENT_TYPE
                                        putExtra(ContactsContract.Intents.Insert.PHONE, address)
                                    }
                                    try {
                                        context.startActivity(insertIntent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("ConversationScreen", "Failed to start insert contact: ${e.message}")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(text = strings.addContact, fontSize = 14.sp)
                            }

                            // b. Mute
                            OutlinedButton(
                                onClick = {
                                    viewModel.setConversationMuted(threadId, true, null)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(text = strings.mute, fontSize = 14.sp)
                            }

                            // c. Block
                            Button(
                                onClick = {
                                    viewModel.blockContact(address, contactName)
                                    viewModel.preferences.dismissUnknownSenderThread(threadId.toString())
                                    isDismissed = true
                                    val blockMsg = String.format(strings.blockSenderAdded, address)
                                    Toast.makeText(context, blockMsg, Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Text(text = strings.block, fontSize = 14.sp, color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
            }

            // Pinned Message Banner
            pinnedMetadata?.let { pinned ->
                val pinnedMsg = messages.find { it.id == pinned.smsId }
                if (pinnedMsg != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 840.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            .clickable {
                                val index = messages.indexOfFirst { it.id == pinned.smsId }
                                if (index >= 0) {
                                    scope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(30.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.pinnedMessage,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = pinnedMsg.body,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.pinMessage(pinnedMsg, false)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.unpin,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = colors.divider
                    )
                }
            }

            // Message Bubble List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                itemsIndexed(
                    items = messages,
                    key = { _, item -> item.id },
                    contentType = { _, item -> if (item.isSentByUser) "bubble_sent" else "bubble_received" }
                ) { index, msg ->
                    val showDateHeader = if (index == 0) {
                        true
                    } else {
                        val prev = messages[index - 1]
                        !prev.dateEpoch.isSameShamsiDayAs(msg.dateEpoch)
                    }

                    if (showDateHeader) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.textSecondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = com.example.domain.util.AppDateFormatter.formatDate(
                                        epochMs = msg.dateEpoch,
                                        system = calendarSystem,
                                        persianDigits = usePersianDigits,
                                        isPersianLanguage = (currentLanguage == AppLanguage.PERSIAN)
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    val isPinned = (pinnedMetadata?.smsId == msg.id && pinnedMetadata?.isPinned == true)

                    val prevMsg = messages.getOrNull(index - 1)
                    val nextMsg = messages.getOrNull(index + 1)

                    val isGroupedTop = prevMsg != null &&
                            prevMsg.isSentByUser == msg.isSentByUser &&
                            abs(msg.dateEpoch - prevMsg.dateEpoch) <= 120_000L &&
                            prevMsg.dateEpoch.isSameShamsiDayAs(msg.dateEpoch)

                    val isGroupedBottom = nextMsg != null &&
                            nextMsg.isSentByUser == msg.isSentByUser &&
                            abs(nextMsg.dateEpoch - msg.dateEpoch) <= 120_000L &&
                            nextMsg.dateEpoch.isSameShamsiDayAs(msg.dateEpoch)

                    MessageBubble(
                        message = msg.copy(isPinned = isPinned),
                        fontSize = fontSize,
                        usePersianNumerals = usePersianDigits,
                        activeSims = activeSims,
                        isGroupedTop = isGroupedTop,
                        isGroupedBottom = isGroupedBottom,
                        onPinMessage = { item, pinState ->
                            viewModel.pinMessage(item, pinState)
                        },
                        onResendMessage = { item ->
                            viewModel.resendMessage(item, onError = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(strings.messageNotSent)
                                }
                            })
                        },
                        onDeleteMessage = { item ->
                            viewModel.deleteMessageWithUndo(item) { undoAction ->
                                scope.launch {
                                    val res = snackbarHostState.showSnackbar(
                                        message = strings.messageDeletedWithUndo,
                                        actionLabel = strings.undo,
                                        duration = SnackbarDuration.Long
                                    )
                                    if (res == SnackbarResult.ActionPerformed) {
                                        undoAction()
                                    }
                                }
                            }
                        },
                        onCopyOtp = { code ->
                            ClipboardHelper.copyOtp(context, code)
                            scope.launch {
                                snackbarHostState.showSnackbar(strings.code_copied)
                            }
                        },
                        searchQuery = searchQuery,
                        isCurrentSearchMatch = (searchMatches.getOrNull(currentSearchMatchIndex) == index)
                    )
                }

                // S8: Scheduled bubbles for PENDING and SENDING rows
                items(
                    items = pendingScheduledMessages,
                    key = { "scheduled_${it.id}" }
                ) { scheduledItem ->
                    val scheduledTime = com.example.domain.util.AppDateFormatter.formatDateTime(
                        epochMs = scheduledItem.scheduledEpoch,
                        system = calendarSystem,
                        persianDigits = usePersianDigits,
                        isPersianLanguage = (currentLanguage == AppLanguage.PERSIAN)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            tonalElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = scheduledItem.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = String.format(strings.scheduledForBubblePrefix, scheduledTime),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (activeSims.size >= 2 && scheduledItem.subId != -1) {
                                        val sim = activeSims.firstOrNull { it.subscriptionId == scheduledItem.subId }
                                        val simLabel = sim?.displayName ?: "SIM ${scheduledItem.subId}"
                                        Text(
                                            text = "• $simLabel",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.cancel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.danger,
                                        modifier = Modifier
                                            .clickable { viewModel.cancelScheduledSms(scheduledItem.id) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Jump to bottom FAB when scrolled up
            val showScrollToBottom = remember {
                androidx.compose.runtime.derivedStateOf {
                    val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    messages.isNotEmpty() && lastVisible < messages.size - 3
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showScrollToBottom.value,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, bottom = 8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = {
                            scope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(42.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = strings.scrollToBottom,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // S4: Schedule SMS Sheet
    if (showScheduleDialog) {
        ScheduleSheet(
            recipient = address,
            messageBody = textInput.trim(),
            calendarSystem = calendarSystem,
            usePersianDigits = usePersianDigits,
            onDismissRequest = { showScheduleDialog = false },
            onScheduleConfirmed = { chosenEpoch ->
                val simSubId = activeSims.getOrNull(selectedSimIndex)?.subscriptionId ?: -1
                viewModel.scheduleSms(
                    recipient = address,
                    body = textInput.trim(),
                    epochTime = chosenEpoch,
                    subId = simSubId,
                    onSuccess = {
                        textInput = ""
                        showScheduleDialog = false
                        scope.launch {
                            val res = snackbarHostState.showSnackbar(
                                message = strings.scheduledSmsScheduledSuccess,
                                actionLabel = strings.viewAction,
                                duration = SnackbarDuration.Short
                            )
                            if (res == SnackbarResult.ActionPerformed) {
                                viewModel.navigateTo(com.example.ui.viewmodel.ScreenDestination.ScheduledSms)
                            }
                        }
                    }
                )
            }
        )
    }

    // Sender Rule Dialog
    if (showRuleDialog) {
        AlertDialog(
            onDismissRequest = { showRuleDialog = false },
            title = { Text(strings.createRule, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(String.format(strings.autoRouteChatToFolder, address))
                    Spacer(modifier = Modifier.height(10.dp))
                    folders.filter { it.id != "all" }.forEach { f ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { ruleFolderId = f.id }
                                .padding(vertical = 3.dp)
                        ) {
                            RadioButton(
                                selected = (ruleFolderId == f.id),
                                onClick = { ruleFolderId = f.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val title = if (currentLanguage == AppLanguage.ENGLISH && f.nameEn.isNotBlank()) f.nameEn else f.nameFa
                            Text(title)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createSenderRule(address, ruleFolderId, strings.ruleCreatedFromChat)
                        showRuleDialog = false
                        Toast.makeText(context, strings.routingRuleApplied, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRuleDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showSimManagerModal) {
        com.example.ui.components.DualSimManagerModal(
            viewModel = viewModel,
            onDismiss = { showSimManagerModal = false }
        )
    }
}
