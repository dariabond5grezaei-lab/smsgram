package com.example.domain.rules

import com.example.data.local.SenderRuleEntity
import com.example.domain.util.SenderNormalizer

object RuleEngine {

    const val MIN_KEYWORD_LENGTH = 2

    /**
     * Normalizes text for matching:
     * lowercase, removes spaces, zero-width non-joiner (U+200C), and tatweel (U+0640).
     */
    fun normalizeForMatch(text: String): String {
        return text.lowercase()
            .replace(" ", "")
            .replace("\u200C", "") // Zero-Width Non-Joiner (ZWNJ)
            .replace("\u0640", "") // Tatweel (Kashida)
            .replace("\t", "")
            .replace("\n", "")
            .replace("\r", "")
    }

    /**
     * Evaluates if a given rule matches an incoming message.
     */
    fun matches(rule: SenderRuleEntity, sender: String, body: String, isContact: Boolean): Boolean {
        val hasSender = rule.senderPattern.trim().isNotEmpty()
        val parsedKeywords = rule.keywords.lines()
            .map { it.trim() }
            .filter { normalizeForMatch(it).length >= MIN_KEYWORD_LENGTH }
        val hasKeywords = parsedKeywords.isNotEmpty()

        // A rule with neither returns false (or if all keywords were too short)
        if (!hasSender && !hasKeywords) {
            return false
        }

        // If the rule has only keywords, the rule never matches a contact
        if (hasKeywords && !hasSender && isContact) {
            return false
        }

        val senderMatches = if (hasSender) {
            SenderNormalizer.matchesRule(sender, rule.senderPattern, rule.isRegex)
        } else {
            true
        }

        val keywordsMatch = if (hasKeywords) {
            val normBody = normalizeForMatch(body)
            parsedKeywords.any { kw ->
                normBody.contains(normalizeForMatch(kw))
            }
        } else {
            true
        }

        return if (hasSender && hasKeywords) {
            senderMatches && keywordsMatch
        } else if (hasSender) {
            senderMatches
        } else {
            keywordsMatch
        }
    }

    /**
     * Returns targetFolderId of the first matching enabled rule.
     * The input list is already in priority order.
     */
    fun firstMatchingFolder(
        rules: List<SenderRuleEntity>,
        sender: String,
        body: String,
        isContact: Boolean
    ): String? {
        for (rule in rules) {
            if (!rule.isEnabled) continue
            // Never route a contact to spam through a rule (Phase D)
            if (rule.targetFolderId == "spam" && isContact) continue
            if (matches(rule, sender, body, isContact)) {
                return rule.targetFolderId
            }
        }
        return null
    }

    /**
     * Returns one plain sentence describing what the rule does.
     */
    fun describe(rule: SenderRuleEntity, folderName: String, isFa: Boolean): String {
        val sender = rule.senderPattern.trim()
        val parsedKeywords = rule.keywords.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val hasSender = sender.isNotEmpty()
        val hasKeywords = parsedKeywords.isNotEmpty()

        return if (isFa) {
            when {
                hasSender && hasKeywords -> {
                    val kws = parsedKeywords.joinToString("، ")
                    "پیام‌های «$sender» با کلمهٔ «$kws» به پوشهٔ «$folderName» می‌روند."
                }
                hasSender -> {
                    "پیام‌های «$sender» به پوشهٔ «$folderName» می‌روند."
                }
                hasKeywords -> {
                    val kws = parsedKeywords.joinToString("، ")
                    "پیام‌هایی که «$kws» دارند به پوشهٔ «$folderName» می‌روند."
                }
                else -> {
                    "پیام‌ها به پوشهٔ «$folderName» می‌روند."
                }
            }
        } else {
            when {
                hasSender && hasKeywords -> {
                    val kws = parsedKeywords.joinToString(", ")
                    "Messages from \"$sender\" with \"$kws\" go to \"$folderName\" folder."
                }
                hasSender -> {
                    "Messages from \"$sender\" go to \"$folderName\" folder."
                }
                hasKeywords -> {
                    val kws = parsedKeywords.joinToString(", ")
                    "Messages with \"$kws\" go to \"$folderName\" folder."
                }
                else -> {
                    "Messages go to \"$folderName\" folder."
                }
            }
        }
    }
}
