package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SmsGramApp
import com.example.data.local.FolderEntity
import com.example.data.local.SenderRuleEntity
import com.example.data.local.RuleTemplateEntity
import com.example.data.model.AppLanguage
import com.example.domain.rules.RuleEngine
import com.example.domain.util.JalaliCalendar
import com.example.ui.components.SmsGramEmptyState
import com.example.ui.components.SmsGramTopBar
import com.example.ui.locale.appStrings
import com.example.ui.theme.smsGramColors
import com.example.ui.viewmodel.SmsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: SmsViewModel,
    onBack: () -> Unit
) {
    val rules by viewModel.rulesFlow.collectAsState()
    val folders by viewModel.foldersFlow.collectAsState()
    val currentLang by viewModel.languageFlow.collectAsState()
    val usePersianDigits by viewModel.persianDigitsFlow.collectAsState()
    val strings = appStrings()
    val colors = smsGramColors
    val isFa = (currentLang == AppLanguage.PERSIAN)

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isHelpSeen by remember { mutableStateOf(viewModel.isRulesHelpSeen()) }

    // Wizard state
    var showWizard by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<SenderRuleEntity?>(null) }

    // Delete confirmation state
    var ruleToDelete by remember { mutableStateOf<SenderRuleEntity?>(null) }

    // Apply rules to older messages state
    var showApplyOlderConfirm by remember { mutableStateOf(false) }
    var isApplyingOlder by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SmsGramTopBar(
                title = strings.rulesEngine,
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
                    .widthIn(max = 760.dp)
            ) {
                // Two tabs with large labels
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        modifier = Modifier.defaultMinSize(minHeight = 52.dp),
                        text = {
                            Text(
                                text = strings.rulesMyRules,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        modifier = Modifier.defaultMinSize(minHeight = 52.dp),
                        text = {
                            Text(
                                text = strings.templatesTabName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

                if (selectedTabIndex == 1) {
                    val scope = rememberCoroutineScope()
                    val templates by viewModel.ruleTemplates.collectAsState()
                    val senderRules by viewModel.senderRules.collectAsState()
                    var showEditor by remember { mutableStateOf(false) }
                    var templateToEdit by remember { mutableStateOf<RuleTemplateEntity?>(null) }
                    var templateToDelete by remember { mutableStateOf<RuleTemplateEntity?>(null) }
                    var showShareSheet by remember { mutableStateOf(false) }
                    var importState by remember { mutableStateOf<Triple<List<RuleTemplateEntity>, List<String>, String?>?>(null) }

                    val context = androidx.compose.ui.platform.LocalContext.current
                    val importLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        if (uri != null) {
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val maxAllowedBytes = 512 * 1024
                                    val buffer = java.io.ByteArrayOutputStream()
                                    var totalBytesRead = 0
                                    val chunk = ByteArray(8192)
                                    var exceeded = false

                                    inputStream?.use { stream ->
                                        while (true) {
                                            val toRead = minOf(chunk.size, (maxAllowedBytes + 1) - totalBytesRead)
                                            if (toRead <= 0) {
                                                exceeded = true
                                                break
                                            }
                                            val count = stream.read(chunk, 0, toRead)
                                            if (count == -1) break
                                            totalBytesRead += count
                                            if (totalBytesRead > maxAllowedBytes) {
                                                exceeded = true
                                                break
                                            }
                                            buffer.write(chunk, 0, count)
                                        }
                                    }

                                    if (exceeded) {
                                        importState = Triple(emptyList(), emptyList(), "file_too_large")
                                    } else {
                                        val text = buffer.toString("UTF-8")
                                        val existingChecksums = viewModel.database.ruleTemplateDao().getAllList().map { it.checksum }.toSet()
                                        val existingFolderIds = viewModel.database.folderDao().getAllFoldersList().map { it.id }.toSet()
                                        val result = com.example.domain.templates.TemplateCodec.decode(text, existingChecksums, existingFolderIds)
                                        importState = Triple(result.templates, result.warnings, result.errorCode)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("RulesScreen", "Import error", e)
                                    importState = Triple(emptyList(), emptyList(), "unsupported_format")
                                }
                            }
                        }
                    }

                    if (importState != null) {
                        TemplateImportScreen(
                            templates = importState!!.first,
                            warnings = importState!!.second,
                            errorCode = importState!!.third,
                            onConfirmAdd = {
                                scope.launch {
                                    viewModel.importTemplatesList(importState!!.first)
                                    importState = null
                                    snackbarHostState.showSnackbar(strings.success)
                                }
                            },
                            onCancel = { importState = null }
                        )
                    } else if (showEditor) {
                        TemplateEditorScreen(
                            folders = folders,
                            templateToEdit = templateToEdit,
                            onSave = { name, desc, sender, folderId, keywords, isRegex ->
                                scope.launch {
                                    viewModel.upsertRuleTemplate(
                                        id = templateToEdit?.id,
                                        name = name,
                                        description = desc,
                                        senderPattern = sender,
                                        targetFolderId = folderId,
                                        keywords = keywords,
                                        isRegex = isRegex
                                    )
                                    showEditor = false
                                    templateToEdit = null
                                }
                            },
                            onCancel = {
                                showEditor = false
                                templateToEdit = null
                            }
                        )
                    } else if (templateToDelete != null) {
                        AlertDialog(
                            onDismissRequest = { templateToDelete = null },
                            title = { Text(strings.deleteTemplateConfirm) },
                            text = { Text("«${templateToDelete?.name}» حذف شود؟") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val t = templateToDelete
                                        if (t != null) {
                                            scope.launch {
                                                viewModel.deleteRuleTemplate(t.id)
                                                templateToDelete = null
                                            }
                                        }
                                    },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text(strings.confirm)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = { templateToDelete = null },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text(strings.cancel)
                                }
                            }
                        )
                    } else if (showShareSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showShareSheet = false }
                        ) {
                            TemplateShareSheet(
                                templates = templates,
                                onShare = { selectedIds, removePersonalNumbers ->
                                    scope.launch {
                                        val uri = viewModel.createTemplateShareFile(context, selectedIds, removePersonalNumbers)
                                        showShareSheet = false
                                        if (uri != null) {
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/json"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            val chooser = Intent.createChooser(sendIntent, strings.shareTemplatesButton)
                                            try {
                                                context.startActivity(chooser)
                                            } catch (e: Exception) {
                                                android.util.Log.e("RulesScreen", "Error sharing file", e)
                                            }
                                        }
                                    }
                                },
                                onCancel = { showShareSheet = false }
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        templateToEdit = null
                                        showEditor = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.createTemplateButton, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(strings.importFromFile, fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = { showShareSheet = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    enabled = templates.isNotEmpty()
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.shareTemplatesButton, fontSize = 13.sp)
                                }
                            }

                            if (templates.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = strings.noTemplatesMessage,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textSecondary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Button(
                                            onClick = {
                                                templateToEdit = null
                                                showEditor = true
                                            },
                                            modifier = Modifier
                                                .height(56.dp)
                                                .padding(horizontal = 24.dp),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(strings.createTemplateButton, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(templates, key = { it.id }) { template ->
                                        val linkedRule = senderRules.find { it.templateId == template.id }
                                        val isEnabled = linkedRule?.isEnabled == true

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = template.name,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary
                                                )
                                                if (template.description.isNotBlank()) {
                                                    Text(
                                                        text = template.description,
                                                        fontSize = 14.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                                Text(
                                                    text = "فرستنده: ${template.senderPattern.ifBlank { "هرکسی" }} | پوشه: ${template.targetFolderId}",
                                                    fontSize = 13.sp,
                                                    color = colors.textSecondary
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .defaultMinSize(minHeight = 48.dp)
                                                            .clickable {
                                                                scope.launch {
                                                                    viewModel.toggleTemplateEnabled(template, !isEnabled)
                                                                }
                                                            }
                                                    ) {
                                                        Switch(
                                                            checked = isEnabled,
                                                            onCheckedChange = { checked ->
                                                                scope.launch {
                                                                    viewModel.toggleTemplateEnabled(template, checked)
                                                                }
                                                            },
                                                            modifier = Modifier.size(52.dp, 36.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = if (isEnabled) strings.rulesStateOn else strings.rulesStateOff,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (isEnabled) MaterialTheme.colorScheme.primary else colors.textSecondary
                                                        )
                                                    }

                                                    Row {
                                                        IconButton(
                                                            onClick = {
                                                                templateToEdit = template
                                                                showEditor = true
                                                            },
                                                            modifier = Modifier.size(48.dp)
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = colors.textPrimary)
                                                        }
                                                        IconButton(
                                                            onClick = { templateToDelete = template },
                                                            modifier = Modifier.size(48.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = colors.danger)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // My Rules tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // First-time help card
                        if (!isHelpSeen) {
                            item(key = "first_time_help") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = strings.rulesFirstTimeHelp,
                                            fontSize = 16.sp,
                                            lineHeight = 22.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                viewModel.markRulesHelpSeen()
                                                isHelpSeen = true
                                            },
                                            modifier = Modifier
                                                .align(Alignment.End)
                                                .defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text(strings.rulesGotIt, fontSize = 15.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Large New Rule button at the top (at least 56dp high)
                        item(key = "new_rule_button") {
                            Button(
                                onClick = {
                                    ruleToEdit = null
                                    showWizard = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = strings.rulesNewRule,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = strings.rulesNewRule,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Empty State
                        if (rules.isEmpty()) {
                            item(key = "empty_state") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SmsGramEmptyState(
                                        icon = Icons.Default.Shield,
                                        title = strings.rulesNoRulesTitle,
                                        subtitle = ""
                                    )
                                }
                            }
                        } else {
                            items(rules, key = { it.id }) { rule ->
                                val targetFolder = folders.find { it.id == rule.targetFolderId }
                                val folderName = if (targetFolder != null) {
                                    if (isFa && targetFolder.nameFa.isNotBlank()) targetFolder.nameFa else targetFolder.nameEn
                                } else {
                                    rule.targetFolderId
                                }

                                val sentence = RuleEngine.describe(rule, folderName, isFa)

                                val stateLabel = if (rule.isEnabled) strings.rulesStateOn else strings.rulesStateOff
                                val talkBackDesc = "$sentence, $stateLabel"

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = talkBackDesc
                                        }
                                        .clickable(enabled = !rule.isRegex) {
                                            ruleToEdit = rule
                                            showWizard = true
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // Advanced rule badge
                                        if (rule.isRegex) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            ) {
                                                Text(
                                                    text = strings.rulesAdvancedRule,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        // Rule Description Sentence in at least 18sp
                                        Text(
                                            text = sentence,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 24.sp
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Action Row: Switch with On/Off label & Delete button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Switch + Text state indicator
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .clickable {
                                                        viewModel.toggleRule(rule.id, !rule.isEnabled)
                                                    }
                                                    .padding(end = 8.dp)
                                            ) {
                                                Switch(
                                                    checked = rule.isEnabled,
                                                    onCheckedChange = { isChecked ->
                                                        viewModel.toggleRule(rule.id, isChecked)
                                                    },
                                                    modifier = Modifier.size(52.dp, 36.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = stateLabel,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (rule.isEnabled) MaterialTheme.colorScheme.primary else colors.textSecondary
                                                )
                                            }

                                            // Delete button with confirmation
                                            IconButton(
                                                onClick = { ruleToDelete = rule },
                                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = strings.delete,
                                                    tint = colors.danger,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Bottom of list: Apply rules to older messages button
                            item(key = "apply_rules_older_button") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showApplyOlderConfirm = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    enabled = !isApplyingOlder
                                ) {
                                    if (isApplyingOlder) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = strings.rulesApplyOlderButton,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Apply Rules to Older Messages Confirmation Dialog
    if (showApplyOlderConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isApplyingOlder) showApplyOlderConfirm = false },
            title = {
                Text(
                    text = strings.rulesApplyOlderButton,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = strings.rulesApplyOlderConfirmDialog,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApplyOlderConfirm = false
                        isApplyingOlder = true
                        viewModel.applyRulesToOlderMessages { result ->
                            isApplyingOlder = false
                            coroutineScope.launch {
                                if (result.failed) {
                                    snackbarHostState.showSnackbar(
                                        message = strings.rulesApplyFailed,
                                        duration = SnackbarDuration.Short
                                    )
                                } else {
                                    val countStr = if (usePersianDigits) JalaliCalendar.toPersianDigits(result.changed.toString()) else result.changed.toString()
                                    snackbarHostState.showSnackbar(
                                        message = strings.rulesConversationsMoved.format(countStr),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(text = strings.rulesGotIt, fontSize = 15.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showApplyOlderConfirm = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(text = strings.cancel, fontSize = 15.sp)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (ruleToDelete != null) {
        val rule = ruleToDelete!!
        AlertDialog(
            onDismissRequest = { ruleToDelete = null },
            title = {
                Text(
                    text = strings.rulesConfirmDelete,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val targetFolder = folders.find { it.id == rule.targetFolderId }
                val folderName = if (targetFolder != null) {
                    if (isFa && targetFolder.nameFa.isNotBlank()) targetFolder.nameFa else targetFolder.nameEn
                } else rule.targetFolderId
                val desc = RuleEngine.describe(rule, folderName, isFa)
                Text(text = desc, fontSize = 16.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = rule
                        viewModel.deleteRule(deleted.id)
                        ruleToDelete = null
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = strings.rulesDeletedMessage,
                                actionLabel = strings.rulesUndoAction,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreRule(deleted)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(strings.delete, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { ruleToDelete = null },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(strings.cancel, fontSize = 16.sp)
                }
            }
        )
    }

    // Rule Creation & Editing Wizard
    if (showWizard) {
        RuleWizardDialog(
            viewModel = viewModel,
            existingRule = ruleToEdit,
            folders = folders,
            isFa = isFa,
            usePersianDigits = usePersianDigits,
            snackbarHostState = snackbarHostState,
            onDismiss = { showWizard = false },
            onSaved = { appliedCount ->
                showWizard = false
                if (appliedCount != null && appliedCount > 0) {
                    val formatted = if (usePersianDigits) {
                        JalaliCalendar.toPersianDigits(appliedCount.toString())
                    } else {
                        appliedCount.toString()
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = strings.rulesConversationsMoved.format(formatted),
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        )
    }
}

/**
 * 3-step wizard with review screen:
 * Step 1: "از چه کسی؟" (Contact / Number or name / Anyone with word)
 * Step 2: "اگر پیام این کلمه‌ها را داشت" (Up to 3 word chips)
 * Step 3: "به کدام پوشه برود؟" (Folder cards + Create new folder)
 * Step 4: Summary screen (describe sentence + affects N conversations + Save / Save and apply)
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RuleWizardDialog(
    viewModel: SmsViewModel,
    existingRule: SenderRuleEntity?,
    folders: List<FolderEntity>,
    isFa: Boolean,
    usePersianDigits: Boolean,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onSaved: (appliedCount: Int?) -> Unit
) {
    val context = LocalContext.current
    val strings = appStrings()
    val colors = smsGramColors
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(1) }

    // Step 1 state
    // 0 = Contact, 1 = Number or name, 2 = Anyone with word
    var senderOption by remember {
        mutableIntStateOf(
            when {
                existingRule == null -> 1
                existingRule.senderPattern.isBlank() -> 2
                else -> 1
            }
        )
    }
    var senderInput by remember { mutableStateOf(existingRule?.senderPattern ?: "") }
    var selectedContactName by remember { mutableStateOf("") }
    var showContactPicker by remember { mutableStateOf(false) }

    // Step 2 state
    var keywordsList by remember {
        mutableStateOf(
            existingRule?.keywords?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        )
    }
    var keywordInput by remember { mutableStateOf("") }

    // Step 3 state: never show 'spam' folder as target for rules
    val visibleFolders = remember(folders) { folders.filter { !it.isHidden && it.id != "all" && it.id != "spam" } }
    var targetFolderId by remember {
        mutableStateOf(
            existingRule?.targetFolderId ?: visibleFolders.firstOrNull()?.id ?: "personal"
        )
    }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderNameFa by remember { mutableStateOf("") }
    var newFolderNameEn by remember { mutableStateOf("") }

    // Summary calculation
    var affectedCount by remember { mutableIntStateOf(0) }
    var isCalculatingCount by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // When reaching summary (Step 4), calculate how many conversations match
    LaunchedEffect(currentStep) {
        if (currentStep == 4) {
            isCalculatingCount = true
            withContext(Dispatchers.IO) {
                try {
                    val app = context.applicationContext as SmsGramApp
                    val convs = app.smsRepository.getConversations()
                    val metaList = app.database.conversationMetadataDao().getAllMetadataList()
                    val metaMap = metaList.associateBy { it.threadId }

                    val tentativeRule = SenderRuleEntity(
                        id = existingRule?.id ?: 0L,
                        senderPattern = if (senderOption == 2) "" else senderInput.trim(),
                        targetFolderId = targetFolderId,
                        isRegex = false,
                        isEnabled = true,
                        keywords = keywordsList.joinToString("\n"),
                        source = "manual"
                    )

                    val count = convs.count { conv ->
                        val meta = metaMap[conv.threadId]
                        if (meta?.customFolderId != null) return@count false
                        val isContact = app.smsRepository.getContactInfo(conv.address).first != null
                        RuleEngine.matches(tentativeRule, conv.address, conv.snippet, isContact)
                    }
                    affectedCount = count
                } catch (e: Exception) {
                    android.util.Log.e("RuleWizard", "Error calculating affected conversations", e)
                } finally {
                    isCalculatingCount = false
                }
            }
        }
    }

    val stepProgressString = when (currentStep) {
        1 -> strings.rulesStepOfThree.format(if (usePersianDigits) "۱" else "1")
        2 -> strings.rulesStepOfThree.format(if (usePersianDigits) "۲" else "2")
        3 -> strings.rulesStepOfThree.format(if (usePersianDigits) "۳" else "3")
        else -> strings.rulesStepSummaryTitle
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingRule != null) strings.rulesEditRule else strings.rulesNewRule,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stepProgressString,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                when (currentStep) {
                    1 -> {
                        // STEP 1: "از چه کسی؟"
                        Text(
                            text = strings.rulesStep1Title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Choice A: From a contact
                        OptionChoiceCard(
                            title = strings.rulesChoiceContact,
                            subtitle = if (senderOption == 0 && senderInput.isNotBlank()) {
                                "$selectedContactName ($senderInput)"
                            } else null,
                            selected = (senderOption == 0),
                            onClick = {
                                senderOption = 0
                                viewModel.loadDeviceContacts()
                                showContactPicker = true
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Choice B: From a number or name
                        OptionChoiceCard(
                            title = strings.rulesChoiceNumberOrName,
                            subtitle = null,
                            selected = (senderOption == 1),
                            onClick = { senderOption = 1 }
                        )

                        if (senderOption == 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = senderInput,
                                onValueChange = { senderInput = it },
                                placeholder = { Text(strings.rulesNumberOrNameHint, fontSize = 15.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 56.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Choice C: Anyone, with a word
                        OptionChoiceCard(
                            title = strings.rulesChoiceAnyoneWithWord,
                            subtitle = null,
                            selected = (senderOption == 2),
                            onClick = {
                                senderOption = 2
                                senderInput = ""
                            }
                        )
                    }

                    2 -> {
                        // STEP 2: "اگر پیام این کلمه‌ها را داشت"
                        Text(
                            text = strings.rulesStep2Title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = keywordInput,
                                onValueChange = { keywordInput = it },
                                placeholder = { Text(strings.rulesKeywordHint, fontSize = 15.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 56.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = keywordsList.size < 3
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val trimmed = keywordInput.trim()
                                    if (trimmed.length >= 2 && keywordsList.size < 3 && !keywordsList.contains(trimmed)) {
                                        keywordsList = keywordsList + trimmed
                                        keywordInput = ""
                                    }
                                },
                                enabled = keywordInput.trim().length >= 2 && keywordsList.size < 3,
                                modifier = Modifier.defaultMinSize(minHeight = 56.dp, minWidth = 72.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(strings.rulesAddWord, fontSize = 15.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.rulesMinKeywordLengthHint,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Word chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            keywordsList.forEach { kw ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clickable {
                                            keywordsList = keywordsList.filter { it != kw }
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = kw,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = strings.delete,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // STEP 3: "به کدام پوشه برود؟"
                        Text(
                            text = strings.rulesStep3Title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        visibleFolders.forEach { folder ->
                            val title = if (isFa && folder.nameFa.isNotBlank()) folder.nameFa else folder.nameEn
                            val isSelected = (targetFolderId == folder.id)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .defaultMinSize(minHeight = 56.dp)
                                    .clickable { targetFolderId = folder.id },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else colors.textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = title,
                                        fontSize = 17.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Create New Folder Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .defaultMinSize(minHeight = 56.dp)
                                .clickable { showNewFolderDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = strings.rulesCreateNewFolder,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    4 -> {
                        // LAST SCREEN: Review & Save
                        val targetFolder = folders.find { it.id == targetFolderId }
                        val folderName = if (targetFolder != null) {
                            if (isFa && targetFolder.nameFa.isNotBlank()) targetFolder.nameFa else targetFolder.nameEn
                        } else targetFolderId

                        val tentativeRule = SenderRuleEntity(
                            id = existingRule?.id ?: 0L,
                            senderPattern = if (senderOption == 2) "" else senderInput.trim(),
                            targetFolderId = targetFolderId,
                            isRegex = false,
                            isEnabled = true,
                            keywords = keywordsList.joinToString("\n"),
                            source = "manual"
                        )
                        val sentence = RuleEngine.describe(tentativeRule, folderName, isFa)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = sentence,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 28.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Affects N conversations
                        val formattedCount = if (usePersianDigits) {
                            JalaliCalendar.toPersianDigits(affectedCount.toString())
                        } else {
                            affectedCount.toString()
                        }

                        if (isCalculatingCount) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "...", fontSize = 16.sp)
                            }
                        } else {
                            Text(
                                text = strings.rulesAffectsConversations.format(formattedCount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (currentStep < 4) {
                // Next / Skip buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Skip button on Step 2 if option was not C
                    if (currentStep == 2 && senderOption != 2) {
                        OutlinedButton(
                            onClick = { currentStep = 3 },
                            modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                        ) {
                            Text(strings.rulesSkip, fontSize = 16.sp)
                        }
                    }

                    val isNextEnabled = when (currentStep) {
                        1 -> (senderOption == 2) || (senderOption == 0 && senderInput.isNotBlank()) || (senderOption == 1 && senderInput.isNotBlank())
                        2 -> (keywordsList.isNotEmpty())
                        3 -> targetFolderId.isNotBlank()
                        else -> true
                    }

                    Button(
                        onClick = {
                            if (currentStep < 4) {
                                currentStep += 1
                            }
                        },
                        enabled = isNextEnabled,
                        modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                    ) {
                        Text(strings.rulesNext, fontSize = 16.sp)
                    }
                }
            } else {
                // Last screen: Save & Save and Apply buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val targetFolder = folders.find { it.id == targetFolderId }
                    val folderName = if (targetFolder != null) {
                        if (isFa && targetFolder.nameFa.isNotBlank()) targetFolder.nameFa else targetFolder.nameEn
                    } else targetFolderId

                    val finalRule = SenderRuleEntity(
                        id = existingRule?.id ?: 0L,
                        senderPattern = if (senderOption == 2) "" else senderInput.trim(),
                        targetFolderId = targetFolderId,
                        isRegex = false,
                        isEnabled = true,
                        keywords = keywordsList.joinToString("\n"),
                        explanation = RuleEngine.describe(
                            SenderRuleEntity(
                                id = existingRule?.id ?: 0L,
                                senderPattern = if (senderOption == 2) "" else senderInput.trim(),
                                targetFolderId = targetFolderId,
                                isRegex = false,
                                isEnabled = true,
                                keywords = keywordsList.joinToString("\n")
                            ),
                            folderName,
                            isFa
                        ),
                        source = "manual"
                    )

                    val onSaveError: (String) -> Unit = { errorKey ->
                        isSaving = false
                        coroutineScope.launch {
                            val msg = if (errorKey == "rule_save_failed") strings.ruleSaveFailed else errorKey
                            snackbarHostState.showSnackbar(msg)
                        }
                    }

                    // Button: Save and apply to older messages
                    Button(
                        onClick = {
                            isSaving = true
                            viewModel.saveRule(
                                rule = finalRule,
                                onComplete = {
                                    viewModel.applyRulesToOlderMessages { result ->
                                        isSaving = false
                                        if (result.failed) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(strings.rulesApplyFailed)
                                            }
                                        } else {
                                            onSaved(result.changed)
                                        }
                                    }
                                },
                                onError = onSaveError
                            )
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = strings.rulesSaveAndApply,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Button: Save
                    OutlinedButton(
                        onClick = {
                            isSaving = true
                            viewModel.saveRule(
                                rule = finalRule,
                                onComplete = {
                                    isSaving = false
                                    onSaved(0)
                                },
                                onError = onSaveError
                            )
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = strings.rulesSave,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        dismissButton = {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = {
                        if (currentStep > 1 && !isSaving) {
                            currentStep -= 1
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                ) {
                    Text(strings.rulesBack, fontSize = 16.sp)
                }
            } else {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                ) {
                    Text(strings.cancel, fontSize = 16.sp)
                }
            }
        }
    )

    // Contact Picker Dialog
    if (showContactPicker) {
        val contacts by viewModel.deviceContacts.collectAsState()
        var contactQuery by remember { mutableStateOf("") }
        val filtered = remember(contacts, contactQuery) {
            if (contactQuery.isBlank()) contacts
            else contacts.filter {
                it.name.contains(contactQuery, ignoreCase = true) || it.number.contains(contactQuery)
            }
        }

        AlertDialog(
            onDismissRequest = { showContactPicker = false },
            title = { Text(strings.rulesChoiceContact, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = contactQuery,
                        onValueChange = { contactQuery = it },
                        placeholder = { Text(strings.searchContactHint, fontSize = 15.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(filtered) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedContactName = contact.name
                                        senderInput = contact.number
                                        showContactPicker = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    Text(contact.number, fontSize = 14.sp, color = colors.textSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showContactPicker = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(strings.cancel, fontSize = 15.sp)
                }
            }
        )
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text(strings.addFolder, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFolderNameFa,
                        onValueChange = { newFolderNameFa = it },
                        label = { Text(strings.folderNameFa) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newFolderNameEn,
                        onValueChange = { newFolderNameEn = it },
                        label = { Text(strings.folderNameEn) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fa = newFolderNameFa.trim()
                        val en = newFolderNameEn.trim()
                        if (fa.isNotBlank() || en.isNotBlank()) {
                            viewModel.addFolder(fa.ifBlank { en }, en.ifBlank { fa })
                            showNewFolderDialog = false
                            newFolderNameFa = ""
                            newFolderNameEn = ""
                        }
                    },
                    enabled = newFolderNameFa.isNotBlank() || newFolderNameEn.isNotBlank(),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(strings.save, fontSize = 15.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNewFolderDialog = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(strings.cancel, fontSize = 15.sp)
                }
            }
        )
    }
}

@Composable
fun OptionChoiceCard(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
