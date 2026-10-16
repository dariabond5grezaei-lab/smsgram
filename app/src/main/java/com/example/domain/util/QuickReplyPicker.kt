package com.example.domain.util

/**
 * Pure function that picks quick reply templates.
 * Removes blanks, removes duplicates, and returns at most max items in the saved order.
 */
fun pickQuickReplies(all: List<String>, max: Int = 6): List<String> {
    if (max <= 0) return emptyList()
    return all.filter { it.isNotBlank() }
        .distinct()
        .take(max)
}
