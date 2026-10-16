package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Folder(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val iconKey: String = "folder",
    val colorHex: String = "#5288C1",
    val isSystem: Boolean = false,
    val sortOrder: Int = 0
) {
    companion object {
        val ALL = Folder(
            id = "all",
            nameFa = "همه",
            nameEn = "All",
            iconKey = "chat",
            colorHex = "#5288C1",
            isSystem = true,
            sortOrder = 0
        )

        // By default, only "All" (همه) is shown. Users can add custom folders.
        val DEFAULT_SYSTEM_FOLDERS = listOf(ALL)
    }
}
