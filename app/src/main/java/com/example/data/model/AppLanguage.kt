package com.example.data.model

enum class AppLanguage(val code: String, val titleFa: String, val titleEn: String) {
    PERSIAN("fa", "فارسی", "Persian"),
    ENGLISH("en", "English", "English");

    fun getDisplayName(currentLang: AppLanguage): String {
        return when (this) {
            PERSIAN -> if (currentLang == ENGLISH) "Persian (فارسی)" else "فارسی (Persian)"
            ENGLISH -> if (currentLang == ENGLISH) "English (انگلیسی)" else "انگلیسی (English)"
        }
    }
}
