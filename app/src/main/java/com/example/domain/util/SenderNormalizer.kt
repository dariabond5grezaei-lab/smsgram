package com.example.domain.util

/**
 * Normalizes sender identifiers (phone numbers, short codes, alphanumeric sender IDs)
 * for automatic rule routing and grouping.
 */
object SenderNormalizer {

    private val normalizationCache = java.util.concurrent.ConcurrentHashMap<String, String>(256)
    private val regexCache = java.util.concurrent.ConcurrentHashMap<String, Regex>(64)

    /**
     * Normalizes phone numbers:
     * - Strips spaces, dashes, parentheses
     * - Normalizes Persian digits
     * - Standardizes +98, 0098, 98 to local 09... format for Iranian mobiles
     * - Retains shortcodes (e.g. 1000..., 2000..., 9821..., 580855)
     */
    fun normalize(rawSender: String): String {
        val cached = normalizationCache[rawSender]
        if (cached != null) return cached

        val trimmed = rawSender.trim()
        if (trimmed.isEmpty()) return ""

        val digitsNormalized = SmartTextParser.normalizeDigits(trimmed)
        val cleaned = digitsNormalized.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")

        // If it starts with +98 or 0098 or 98 for mobile numbers
        val result = when {
            cleaned.startsWith("+989") && cleaned.length == 13 -> "09" + cleaned.substring(4)
            cleaned.startsWith("00989") && cleaned.length == 14 -> "09" + cleaned.substring(5)
            cleaned.startsWith("989") && cleaned.length == 12 -> "09" + cleaned.substring(3)
            cleaned.startsWith("+98") -> "0" + cleaned.substring(3)
            cleaned.startsWith("0098") -> "0" + cleaned.substring(4)
            else -> cleaned
        }

        if (normalizationCache.size < 2000) {
            normalizationCache[rawSender] = result
        }
        return result
    }

    /**
     * Checks if a sender matches a rule pattern (supports wildcard '*' and exact match or regex)
     */
    fun matchesRule(sender: String, pattern: String, isRegex: Boolean): Boolean {
        val trimmedPattern = pattern.trim()
        if (trimmedPattern.isEmpty()) return false

        val sendersToTest = listOf(
            sender.trim(),
            sender.trim().removePrefix("+"),
            normalize(sender)
        ).distinct()

        if (isRegex) {
            return try {
                val rx = regexCache.computeIfAbsent(trimmedPattern) {
                    Regex(it, RegexOption.IGNORE_CASE)
                }
                sendersToTest.any { rx.containsMatchIn(it) }
            } catch (e: Exception) {
                false
            }
        }

        // Wildcard match using precise character escaping
        if (trimmedPattern.contains("*") || trimmedPattern.contains("?")) {
            return try {
                val rx = regexCache.computeIfAbsent("wildcard_$trimmedPattern") {
                    wildcardToRegex(trimmedPattern)
                }
                sendersToTest.any { rx.matches(it) }
            } catch (e: Exception) {
                false
            }
        }

        // Direct equality match only (no contains)
        val normPatNorm = normalize(trimmedPattern)
        return sendersToTest.any {
            it.equals(trimmedPattern, ignoreCase = true) ||
            it.equals(normPatNorm, ignoreCase = true)
        }
    }

    /**
     * Matches sender against a blocked entry:
     * - If entry ends with "*" or ".", strip them and match as prefix:
     *   normalize(sender).startsWith(normalize(prefix)) OR sender.startsWith(prefix)
     * - Otherwise exact equality (case-insensitive) or normalized equality or last 10 digits match when both >= 10
     */
    fun matchesBlockEntry(sender: String, entry: String): Boolean {
        val trimmedEntry = entry.trim()
        if (trimmedEntry.isEmpty()) return false

        val rawSender = sender.trim()
        val normSender = normalize(rawSender)

        if (trimmedEntry.endsWith("*") || trimmedEntry.endsWith(".")) {
            val prefix = trimmedEntry.trimEnd('*', '.').trim()
            val normPrefix = normalize(prefix)
            return (normPrefix.isNotEmpty() && normSender.startsWith(normPrefix)) ||
                   (prefix.isNotEmpty() && rawSender.startsWith(prefix))
        }

        val normEntry = normalize(trimmedEntry)
        return rawSender.equals(trimmedEntry, ignoreCase = true) ||
               (normSender.isNotEmpty() && normSender == normEntry) ||
               (rawSender.length >= 10 && trimmedEntry.length >= 10 && rawSender.takeLast(10) == trimmedEntry.takeLast(10))
    }

    private fun wildcardToRegex(pattern: String): Regex {
        val sb = StringBuilder("^")
        for (char in pattern) {
            when (char) {
                '*' -> sb.append(".*")
                '?' -> sb.append(".")
                else -> {
                    if ("\\.[{()<>+=-!^$|".contains(char)) {
                        sb.append('\\').append(char)
                    } else {
                        sb.append(char)
                    }
                }
            }
        }
        sb.append("$")
        return Regex(sb.toString(), RegexOption.IGNORE_CASE)
    }
}
