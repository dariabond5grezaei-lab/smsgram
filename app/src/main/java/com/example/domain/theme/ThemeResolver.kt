package com.example.domain.theme

import com.example.data.model.ThemeFamily
import com.example.data.model.ThemeMode

object ThemeResolver {
    fun resolvePalette(family: ThemeFamily, dark: Boolean): ThemeMode? {
        return if (family == ThemeFamily.DYNAMIC) {
            ThemeMode.DYNAMIC
        } else {
            if (dark) family.darkMode else family.lightMode
        }
    }
}
