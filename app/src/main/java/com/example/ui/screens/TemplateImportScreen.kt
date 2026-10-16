package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RuleTemplateEntity
import com.example.ui.locale.appStrings
import com.example.ui.theme.smsGramColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateImportScreen(
    templates: List<RuleTemplateEntity>,
    warnings: List<String>,
    errorCode: String?,
    onConfirmAdd: () -> Unit,
    onCancel: () -> Unit
) {
    val strings = appStrings()
    val colors = smsGramColors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.importFromFile) },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorCode != null) {
                val errorMsg = when (errorCode) {
                    "file_too_large" -> "فایل بیش از حد بزرگ است (حداکثر ۵۱۲ کیلوبایت)"
                    "unsupported_format" -> "فرمت فایل پشتیبانی نمی‌شود"
                    "too_many_templates" -> "تعداد قالب‌ها بیش از حد مجاز است (حداکثر ۳۰۰)"
                    else -> "خطا در خواندن فایل"
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("خطا", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Text(errorMsg, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(strings.cancel, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = strings.templatesFoundFormat.format(templates.size),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                if (warnings.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("هشدارها:", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            warnings.forEach { w ->
                                Text("• $w", color = colors.textSecondary, fontSize = 14.sp)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates) { t ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceCard)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(t.name, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 16.sp)
                                if (t.description.isNotBlank()) {
                                    Text(t.description, color = colors.textSecondary, fontSize = 14.sp)
                                }
                                Text("فرستنده: ${t.senderPattern.ifBlank { "هرکسی" }} | پوشه: ${t.targetFolderId}", color = colors.textSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Text(
                    text = strings.importInfoText,
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )

                Button(
                    onClick = onConfirmAdd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = templates.isNotEmpty()
                ) {
                    Text(strings.addTemplatesDisabled, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
}
