package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FolderEntity
import com.example.data.local.RuleTemplateEntity
import com.example.domain.templates.TemplateValidator
import com.example.ui.locale.PersianStrings
import com.example.ui.locale.appStrings
import com.example.ui.theme.smsGramColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorScreen(
    folders: List<FolderEntity> = emptyList(),
    templateToEdit: RuleTemplateEntity? = null,
    onSave: (name: String, desc: String, sender: String, folderId: String, keywords: String, isRegex: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val strings = appStrings()
    val colors = smsGramColors

    val eligibleFolders = remember(folders) {
        folders.filter { !it.isHidden && it.id != "all" && it.id != "spam" }
    }

    var name by remember { mutableStateOf(templateToEdit?.name ?: "") }
    var description by remember { mutableStateOf(templateToEdit?.description ?: "") }
    var senderPattern by remember { mutableStateOf(templateToEdit?.senderPattern ?: "") }
    var targetFolderId by remember {
        mutableStateOf(
            templateToEdit?.targetFolderId
                ?: eligibleFolders.find { it.id == "shopping" }?.id
                ?: eligibleFolders.firstOrNull()?.id
                ?: "shopping"
        )
    }
    var keywords by remember { mutableStateOf(templateToEdit?.keywords ?: "") }
    var isRegex by remember { mutableStateOf(templateToEdit?.isRegex ?: false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (templateToEdit == null) strings.newTemplate else strings.rulesEditRule) },
                navigationIcon = {
                    IconButton(onClick = onCancel, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surfaceCard)
            )
        },
        containerColor = colors.surfaceCard
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 40) name = it },
                label = { Text(strings.templateTitleLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("${name.length}/40") }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 120) description = it },
                label = { Text(strings.ruleExplanationLabel) },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("${description.length}/120") }
            )

            OutlinedTextField(
                value = senderPattern,
                onValueChange = { senderPattern = it },
                label = { Text(strings.rulePatternLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(strings.targetFolderLabel, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)

            val isFa = strings is PersianStrings
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                eligibleFolders.forEach { folder ->
                    val label = if (isFa && folder.nameFa.isNotBlank()) folder.nameFa else folder.nameEn.ifBlank { folder.nameFa }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = targetFolderId == folder.id,
                            onClick = { targetFolderId = folder.id },
                            modifier = Modifier.size(56.dp)
                        )
                        Text(label, color = colors.textPrimary, fontSize = 16.sp)
                    }
                }
            }

            OutlinedTextField(
                value = keywords,
                onValueChange = { keywords = it },
                label = { Text(strings.rulesKeywordHint) },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("کلمات کلیدی با خط جدید (حداکثر ۳ عدد، حداقل ۲ حرف)") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isRegex,
                    onCheckedChange = { isRegex = it },
                    modifier = Modifier.size(56.dp)
                )
                Text("استفاده از عبارت منظم (Regex)", color = colors.textPrimary, fontSize = 16.sp)
            }

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        errorMessage = "نام قالب نمی‌تواند خالی باشد"
                        return@Button
                    }
                    if (targetFolderId == "spam") {
                        errorMessage = "قالب نمی‌تواند به پوشه هرزنامه هدایت شود"
                        return@Button
                    }
                    if (isRegex) {
                        if (senderPattern.length > 200) {
                            errorMessage = "طول عبارت منظم نمی‌تواند بیش از ۲۰۰ نویسه باشد"
                            return@Button
                        }
                        if (TemplateValidator.hasNestedQuantifiers(senderPattern)) {
                            errorMessage = "عبارت منظم نمی‌تواند شامل کوانتیفایرهای تودرتو باشد"
                            return@Button
                        }
                        try {
                            senderPattern.toRegex()
                        } catch (e: Exception) {
                            errorMessage = "عبارت منظم نامعتبر است"
                            return@Button
                        }
                    }
                    onSave(trimmedName, description.trim(), senderPattern.trim(), targetFolderId, keywords.trim(), isRegex)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.save, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(strings.cancel, fontSize = 18.sp)
            }
        }
    }
}
