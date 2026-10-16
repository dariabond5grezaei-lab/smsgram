package com.example

import com.example.data.local.FolderEntity
import com.example.domain.util.FolderAction
import com.example.domain.util.deleteActionFor
import com.example.domain.util.orderFolders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderOrderingTest {

    @Test
    fun testDeleteActionForSystemAndCustom() {
        assertEquals(FolderAction.HIDE, deleteActionFor(isSystem = true))
        assertEquals(FolderAction.DELETE, deleteActionFor(isSystem = false))
    }

    @Test
    fun testOrderFoldersAllFirstAndHiddenExcluded() {
        val folders = listOf(
            FolderEntity("personal", "شخصی", "Personal", isSystem = true, sortOrder = 3),
            FolderEntity("all", "همه", "All", isSystem = true, sortOrder = 0),
            FolderEntity("bank", "بانکی", "Bank", isSystem = true, sortOrder = 1),
            FolderEntity("hidden_folder", "پنهان", "Hidden", isSystem = false, sortOrder = 2, isHidden = true)
        )

        val result = orderFolders(folders)

        // "all" must be first
        assertEquals("all", result.first().id)

        // hidden_folder must be excluded
        assertFalse(result.any { it.id == "hidden_folder" })

        // 3 visible folders remaining
        assertEquals(3, result.size)
    }

    @Test
    fun testOrderFoldersSavedOrderWithUnknownFoldersAppended() {
        val folders = listOf(
            FolderEntity("all", "همه", "All", isSystem = true, sortOrder = 0),
            FolderEntity("f_unknown", "ناشناخته", "Unknown", isSystem = false, sortOrder = 10),
            FolderEntity("bank", "بانکی", "Bank", isSystem = true, sortOrder = 1),
            FolderEntity("personal", "شخصی", "Personal", isSystem = true, sortOrder = 2)
        )

        // Custom saved order for bank and personal
        val savedOrder = listOf("personal", "bank")

        val result = orderFolders(folders, savedOrder)

        // Order should be: "all", then "personal", then "bank", then unknown "f_unknown" at the end
        val ids = result.map { it.id }
        assertEquals(listOf("all", "personal", "bank", "f_unknown"), ids)
    }
}
