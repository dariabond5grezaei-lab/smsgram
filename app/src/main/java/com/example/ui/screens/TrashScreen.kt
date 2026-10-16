package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.domain.util.JalaliCalendar
import com.example.ui.components.SmsGramEmptyState
import com.example.ui.components.SmsGramTopBar
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.appStrings
import com.example.ui.theme.smsGramColors
import com.example.ui.viewmodel.SmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: SmsViewModel,
    onBack: () -> Unit
) {
    val trashList by viewModel.trashFlow.collectAsState()
    val usePersianNumerals by viewModel.persianDigitsFlow.collectAsState()
    val calendarSystem by viewModel.calendarSystemFlow.collectAsState()
    val currentLanguage = LocalAppLanguage.current
    val strings = appStrings()
    val colors = smsGramColors
    var showConfirmClearAll by remember { mutableStateOf(false) }

    val formattedCount = if (usePersianNumerals) JalaliCalendar.toPersianDigits(trashList.size.toString()) else trashList.size.toString()
    val subtitle = if (trashList.isNotEmpty()) {
        "$formattedCount ${strings.trashMessagesCount}"
    } else null

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SmsGramTopBar(
                title = strings.trash,
                subtitle = subtitle,
                onBack = onBack,
                actions = {
                    if (trashList.isNotEmpty()) {
                        IconButton(onClick = { showConfirmClearAll = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = strings.clearTrash,
                                tint = colors.danger
                            )
                        }
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
            if (trashList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SmsGramEmptyState(
                        icon = Icons.Default.Delete,
                        title = strings.emptyTrashTitle,
                        subtitle = strings.emptyTrashSubtitle
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trashList, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                        .background(colors.danger.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = colors.danger,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.address,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.body,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${strings.dateDeletedPrefix}${com.example.domain.util.AppDateFormatter.formatRelativeTime(item.deletedAtEpoch, calendarSystem, usePersianNumerals, (currentLanguage == AppLanguage.PERSIAN))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Restore button
                                IconButton(
                                    onClick = { viewModel.restoreFromTrash(item.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = strings.restoreMessage,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Delete Permanently
                                IconButton(
                                    onClick = { viewModel.deletePermanently(item.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = strings.permanentDelete,
                                        tint = colors.danger
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmClearAll) {
        AlertDialog(
            onDismissRequest = { showConfirmClearAll = false },
            title = { Text(strings.clearTrash, fontWeight = FontWeight.Bold) },
            text = { Text(strings.confirmClearTrash) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllTrash()
                        showConfirmClearAll = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.danger)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearAll = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
