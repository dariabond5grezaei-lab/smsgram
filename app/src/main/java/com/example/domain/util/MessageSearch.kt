package com.example.domain.util

/**
 * Normalizes text for message search by trimming, lowercasing,
 * and removing Zero-Width Non-Joiner (ZWNJ \u200C) and Tatweel (\u0640).
 */
fun normalizeForSearch(text: String): String {
    return text.trim()
        .lowercase()
        .replace("\u200C", "")
        .replace("\u0640", "")
}

/**
 * Pure function to find indexes of messages matching the query.
 * Ignores case and spaces at the start and end.
 * Normalizes ZWNJ and tatweel.
 * An empty query returns an empty list.
 */
fun findMatches(messages: List<String>, query: String): List<Int> {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) return emptyList()
    val normalizedQuery = normalizeForSearch(trimmedQuery)
    if (normalizedQuery.isEmpty()) return emptyList()

    return messages.mapIndexedNotNull { index, message ->
        if (normalizeForSearch(message).contains(normalizedQuery)) index else null
    }
}
