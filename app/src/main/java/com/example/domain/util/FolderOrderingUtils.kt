package com.example.domain.util

import com.example.data.local.FolderEntity

enum class FolderAction {
    DELETE,
    HIDE
}

/**
  Returns FolderAction.HIDE for system folders (isSystem = true)
  and FolderAction.DELETE for custom folders (isSystem = false).
 */
fun deleteActionFor(isSystem: Boolean): FolderAction {
    return if (isSystem) FolderAction.HIDE else FolderAction.DELETE
}

/**
  Orders folder entities such that:
  1. "all" is always placed first.
  2. Hidden folders (isHidden = true) are excluded from the result.
  3. Other folders follow savedOrder if provided; unknown/new folders are appended at the end (sorted by sortOrder).
 */
fun orderFolders(
    folders: List<FolderEntity>,
    savedOrder: List<String> = emptyList()
): List<FolderEntity> {
    // Filter out hidden folders
    val visibleFolders = folders.filter { !it.isHidden }

    val allFolder = visibleFolders.find { it.id == "all" }
    val otherFolders = visibleFolders.filter { it.id != "all" }

    val sortedOthers = if (savedOrder.isNotEmpty()) {
        val orderMap = savedOrder.withIndex().associate { it.value to it.index }
        otherFolders.sortedWith { a, b ->
            val indexA = orderMap[a.id] ?: Int.MAX_VALUE
            val indexB = orderMap[b.id] ?: Int.MAX_VALUE
            if (indexA != indexB) {
                indexA.compareTo(indexB)
            } else {
                a.sortOrder.compareTo(b.sortOrder)
            }
        }
    } else {
        otherFolders.sortedBy { it.sortOrder }
    }

    return if (allFolder != null) {
        listOf(allFolder) + sortedOthers
    } else {
        sortedOthers
    }
}
