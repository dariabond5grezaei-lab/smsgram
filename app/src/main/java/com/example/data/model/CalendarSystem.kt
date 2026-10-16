package com.example.data.model

enum class CalendarSystem {
    SHAMSI,
    GREGORIAN,
    BOTH;

    companion object {
        fun fromName(name: String?): CalendarSystem {
            if (name == null) return SHAMSI
            return try {
                valueOf(name)
            } catch (_: Exception) {
                SHAMSI
            }
        }
    }
}
