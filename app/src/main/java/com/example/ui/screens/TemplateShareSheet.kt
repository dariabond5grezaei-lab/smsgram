package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
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

@Composable
fun TemplateShareSheet(
    templates: List<RuleTemplateEntity>,
    onShare: (selectedIds: Set<String>, removePersonalNumbers: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val strings = appStrings()
    val colors = smsGramColors

    var selectedIds by remember { mutableStateOf(templates.map { it.id }.toSet()) }
    var removePersonalNumbers by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surfaceCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = strings.shareTemplatesButton,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            if (templates.isEmpty()) {
                Text(
                    text = strings.noTemplatesToShare,
                    color = colors.textSecondary,
                    fontSize = 16.sp
                )
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(strings.cancel, fontSize = 18.sp)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = removePersonalNumbers,
                        onCheckedChange = { removePersonalNumbers = it },
                        modifier = Modifier.size(56.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.removePersonalNumbersCheckbox, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(strings.removePersonalNumbersDesc, color = colors.textSecondary, fontSize = 12.sp)
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier.heightIn(max = 250.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates) { t ->
                        val isSelected = selectedIds.contains(t.id)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        selectedIds + t.id
                                    } else {
                                        selectedIds - t.id
                                    }
                                },
                                modifier = Modifier.size(56.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.name, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text("فرستنده: ${t.senderPattern.ifBlank { "هرکسی" }}", color = colors.textSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Button(
                    onClick = { onShare(selectedIds, removePersonalNumbers) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selectedIds.isNotEmpty()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.createFileAndSend, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
