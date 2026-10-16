package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FolderEntity
import com.example.data.model.AppLanguage
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.orderFolders
import com.example.ui.locale.LocalAppLanguage
import com.example.ui.locale.appStrings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderTabsRow(
    folders: List<FolderEntity>,
    selectedFolderId: String,
    folderCounts: Map<String, Int>,
    usePersianNumerals: Boolean = true,
    onSelectFolder: (String) -> Unit,
    onReorderFolders: () -> Unit,
    onEditFolder: (FolderEntity) -> Unit,
    onMuteAll: (String) -> Unit,
    onMarkFolderAsRead: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onHideFolder: (String) -> Unit
) {
    val currentLang = LocalAppLanguage.current
    val strings = appStrings()
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    // Exclude hidden folders and sort according to orderFolders helper
    val visibleFolders = remember(folders) { orderFolders(folders) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleFolders.forEach { folder ->
            val isSelected = (selectedFolderId == folder.id)
            val unreadCount = folderCounts[folder.id] ?: 0
            var showTabMenu by remember(folder.id) { mutableStateOf(false) }

            val folderTitle = when (folder.id) {
                "all" -> strings.allChats
                "bank" -> strings.bank
                "otp" -> if (currentLang == AppLanguage.PERSIAN) "رمز و تایید" else "OTP Code"
                "personal" -> strings.personal
                "services" -> strings.services
                "advertising" -> strings.advertising
                else -> folder.getLocalizedName(currentLang)
            }

            val bgAnim by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                },
                label = "folderBg"
            )

            val textAnim by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "folderText"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgAnim)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(20.dp)
                            )
                        } else Modifier
                    )
                    .combinedClickable(
                        onClick = { onSelectFolder(folder.id) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showTabMenu = true
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = folderTitle,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = textAnim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (unreadCount in 1..99 && (folder.id == "bank" || folder.id == "otp")) {
                        Spacer(modifier = Modifier.width(6.dp))
                        val unreadStr = unreadCount.toString()
                        val displayUnread = if (usePersianNumerals) JalaliCalendar.toPersianDigits(unreadStr) else unreadStr

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        Color.White.copy(alpha = 0.25f)
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayUnread,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showTabMenu,
                    onDismissRequest = { showTabMenu = false }
                ) {
                    // 1. Reorder
                    DropdownMenuItem(
                        text = { Text(strings.reorderFolders) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showTabMenu = false
                            onReorderFolders()
                        }
                    )

                    // 2. Edit folder
                    DropdownMenuItem(
                        text = { Text(strings.editFolder) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showTabMenu = false
                            onEditFolder(folder)
                        }
                    )

                    // 3. Mute All
                    DropdownMenuItem(
                        text = { Text(strings.muteAll) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showTabMenu = false
                            onMuteAll(folder.id)
                        }
                    )

                    // 4. Mark all as read
                    DropdownMenuItem(
                        text = { Text(strings.markAllAsReadAction) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showTabMenu = false
                            onMarkFolderAsRead(folder.id)
                        }
                    )

                    // 5. Delete folder (custom) OR Hide folder (system)
                    if (folder.isSystem) {
                        DropdownMenuItem(
                            text = { Text(strings.hideFolder) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showTabMenu = false
                                onHideFolder(folder.id)
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = strings.deleteFolderAction,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showTabMenu = false
                                onDeleteFolder(folder.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
