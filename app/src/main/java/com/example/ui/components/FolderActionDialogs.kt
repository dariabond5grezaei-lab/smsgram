package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FolderEntity
import com.example.data.model.AppLanguage
import com.example.domain.util.orderFolders
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.appStrings

@Composable
fun EditFolderDialog(
    folder: FolderEntity,
    onDismiss: () -> Unit,
    onConfirm: (newNameFa: String, newNameEn: String) -> Unit
) {
    val currentLang = LocalAppLanguage.current
    val strings = appStrings()
    
    val initialTitle = when (folder.id) {
        "all" -> strings.allChats
        "bank" -> strings.bank
        "otp" -> if (currentLang == AppLanguage.PERSIAN) "رمز و تایید" else "OTP Code"
        "personal" -> strings.personal
        "services" -> strings.services
        "advertising" -> strings.advertising
        else -> folder.getLocalizedName(currentLang)
    }

    var nameFa by remember { mutableStateOf(folder.nameFa.ifBlank { initialTitle }) }
    var nameEn by remember { mutableStateOf(folder.nameEn.ifBlank { initialTitle }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.editFolder, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameFa,
                    onValueChange = { nameFa = it },
                    label = { Text(strings.folderNameFa) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(strings.folderNameEn) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameFa.isNotBlank() || nameEn.isNotBlank()) {
                        val finalFa = nameFa.ifBlank { nameEn }
                        val finalEn = nameEn.ifBlank { nameFa }
                        onConfirm(finalFa.trim(), finalEn.trim())
                    }
                },
                enabled = nameFa.isNotBlank() || nameEn.isNotBlank()
            ) {
                Text(strings.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun MuteFolderDialog(
    folderId: String,
    onDismiss: () -> Unit,
    onConfirm: (durationMs: Long?) -> Unit
) {
    val strings = appStrings()
    // Options: 1 hour (3600000), 8 hours (28800000), 1 week (604800000), Always (-1)
    var selectedOption by remember { mutableStateOf<Long?>(3600000L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.muteAll, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (folderId == "otp") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.otpMuteWarning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = strings.muteDurationTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val options = listOf(
                    3600000L to strings.mute1Hour,
                    28800000L to strings.mute8Hours,
                    604800000L to strings.mute1Week,
                    -1L to strings.muteAlways
                )

                options.forEach { (duration, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = duration }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == duration),
                            onClick = { selectedOption = duration }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedOption) }) {
                Text(strings.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun ConfirmActionDialog(
    title: String,
    body: String,
    confirmText: String = appStrings().confirm,
    isDanger: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val strings = appStrings()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (isDanger) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else ButtonDefaults.buttonColors()
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun FolderReorderDialog(
    folders: List<FolderEntity>,
    onDismiss: () -> Unit,
    onSaveOrder: (orders: List<Pair<String, Int>>) -> Unit
) {
    val currentLang = LocalAppLanguage.current
    val strings = appStrings()
    
    // Sort visible folders using orderFolders pure helper
    val visibleFolders = remember(folders) { orderFolders(folders) }
    
    val folderList = remember { mutableStateListOf<FolderEntity>().apply { addAll(visibleFolders) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.reorderFolders, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    itemsIndexed(folderList) { index, folder ->
                        val folderTitle = when (folder.id) {
                            "all" -> strings.allChats
                            "bank" -> strings.bank
                            "otp" -> if (currentLang == AppLanguage.PERSIAN) "رمز و تایید" else "OTP Code"
                            "personal" -> strings.personal
                            "services" -> strings.services
                            "advertising" -> strings.advertising
                            else -> folder.getLocalizedName(currentLang)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (folder.id == "all") {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = folderTitle,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (folder.id != "all") {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                if (index > 1) { // Index 0 is "all", so move up to min index 1
                                                    val item = folderList.removeAt(index)
                                                    folderList.add(index - 1, item)
                                                }
                                            },
                                            enabled = index > 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (index < folderList.size - 1) {
                                                    val item = folderList.removeAt(index)
                                                    folderList.add(index + 1, item)
                                                }
                                            },
                                            enabled = index < folderList.size - 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val orders = folderList.mapIndexed { idx, folder ->
                        folder.id to idx
                    }
                    onSaveOrder(orders)
                }
            ) {
                Text(strings.doneAction)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
