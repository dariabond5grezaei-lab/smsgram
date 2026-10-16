package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.JalaliCalendar
import com.example.ui.components.SmsGramEmptyState
import com.example.ui.components.SmsGramTopBar
import com.example.ui.theme.smsGramColors
import com.example.ui.viewmodel.SmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedContactsScreen(
    viewModel: SmsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = com.example.ui.locale.appStrings()
    val currentLanguage = com.example.ui.locale.LocalAppLanguage.current
    val blockedList by viewModel.blockedContactsFlow.collectAsState()
    val colors = smsGramColors

    var showAddDialog by remember { mutableStateOf(false) }
    var inputAddress by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    val defaultReason = if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "پیامک‌های تبلیغاتی و هرزنامه" else "Spam & Advertising SMS"
    var inputReason by remember { mutableStateOf(defaultReason) }

    val usePersianNumerals by viewModel.persianDigitsFlow.collectAsState()
    val formattedCount = if (usePersianNumerals) JalaliCalendar.toPersianDigits(blockedList.size.toString()) else blockedList.size.toString()
    val subtitle = if (blockedList.isNotEmpty()) "$formattedCount ${strings.blockedContactsCount}" else null

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmsGramTopBar(
                title = strings.blockedNumbers,
                subtitle = subtitle,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.blockSender,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
            if (blockedList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SmsGramEmptyState(
                        icon = Icons.Default.Block,
                        title = strings.noBlockedContactsTitle,
                        subtitle = strings.noBlockedContactsSubtitle
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    item {
                        Text(
                            text = strings.blockContactDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                        )
                    }

                    items(blockedList, key = { it.address }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.danger.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = colors.danger,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.contactName ?: item.address,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!item.contactName.isNullOrBlank()) {
                                        Text(
                                            text = item.address,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                    if (!item.reason.isNullOrBlank()) {
                                        Text(
                                            text = item.reason,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.danger.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.unblockContact(item.address)
                                        val toastMsg = if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "${item.address} از لیست مسدودها خارج شد" else "${item.address} unblocked"
                                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        text = if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "رفع مسدودی" else "Unblock",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "مسدودسازی شماره یا فرستنده" else "Block Number or Sender", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "شماره یا پیش‌شماره سرشماره تبلیغاتی را برای مسدودسازی وارد کنید:" else "Enter phone number or prefix to block:",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputAddress,
                        onValueChange = { inputAddress = it },
                        label = { Text(if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "شماره یا سرشماره (مانند 989000...)" else "Number or Prefix (e.g., 989000...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text(if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "نام / عنوان دلخواه (اختیاری)" else "Title / Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputReason,
                        onValueChange = { inputReason = it },
                        label = { Text(if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "علت مسدودی" else "Reason") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputAddress.isNotBlank()) {
                            viewModel.blockContact(
                                address = inputAddress.trim(),
                                contactName = inputName.takeIf { it.isNotBlank() },
                                reason = inputReason.takeIf { it.isNotBlank() }
                            )
                            val addedMsg = if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "$inputAddress به لیست مسدودها اضافه شد 🚫" else "$inputAddress blocked 🚫"
                            Toast.makeText(context, addedMsg, Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                            inputAddress = ""
                            inputName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                    enabled = inputAddress.isNotBlank()
                ) {
                    Text(if (currentLanguage == com.example.data.model.AppLanguage.PERSIAN) "مسدودسازی" else "Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
