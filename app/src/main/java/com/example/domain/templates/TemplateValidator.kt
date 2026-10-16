package com.example.domain.templates

import com.example.data.local.RuleTemplateEntity
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

data class DecodeResult(
    val templates: List<RuleTemplateEntity> = emptyList(),
    val validCount: Int = 0,
    val duplicateCount: Int = 0,
    val warnings: List<String> = emptyList(),
    val errorCode: String? = null
)

object TemplateValidator {

    fun normalizeDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            when (ch) {
                in '۰'..'۹' -> sb.append((ch - '۰' + '0'.code).toChar())
                in '٠'..'٩' -> sb.append((ch - '٠' + '0'.code).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun isPersonalMobileNumber(sender: String): Boolean {
        if (sender.isBlank()) return false
        val normalized = normalizeDigits(sender).trim()
        val digitsOnly = normalized.filter { it.isDigit() }
        val length = digitsOnly.length
        if (length !in 10..14) return false

        val startsWithMobile = normalized.startsWith("09") ||
                normalized.startsWith("+989") ||
                normalized.startsWith("989") ||
                normalized.startsWith("00989") ||
                digitsOnly.startsWith("00989") ||
                digitsOnly.startsWith("989") ||
                digitsOnly.startsWith("09")
        return startsWithMobile
    }

    fun hasNestedQuantifiers(pattern: String): Boolean {
        val nestedQuantifierRegex = Regex("""(?<!\\)\([^()]*[+*?{][^()]*\)\s*[+*?{]""")
        return nestedQuantifierRegex.containsMatchIn(pattern)
    }

    fun sanitizeString(input: String, maxLength: Int): String {
        val cleaned = input.filter { it >= ' ' || it == '\n' || it == '\r' || it == '\t' }.trim()
        return if (cleaned.length > maxLength) cleaned.substring(0, maxLength) else cleaned
    }

    fun sanitizeKeywords(keywords: String): String {
        return keywords.split("\n", ",")
            .map { it.trim() }
            .filter { it.length >= 2 }
            .take(3)
            .joinToString("\n")
    }

    fun computeChecksum(name: String, senderPattern: String, targetFolderId: String, keywords: String): String {
        val cleanName = sanitizeString(name, 40)
        val cleanSender = sanitizeString(senderPattern, 100)
        val cleanFolder = sanitizeString(targetFolderId, 50).ifBlank { "shopping" }
        val cleanKw = sanitizeKeywords(keywords)
        val raw = "$cleanName|$cleanSender|$cleanFolder|$cleanKw"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun decode(
        jsonString: String,
        existingChecksums: Set<String> = emptySet(),
        existingFolderIds: Set<String> = emptySet()
    ): DecodeResult {
        if (jsonString.toByteArray(Charsets.UTF_8).size > 512 * 1024) {
            return DecodeResult(errorCode = "file_too_large")
        }

        val root = try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            return DecodeResult(errorCode = "unsupported_format")
        }

        val format = root.optString("format", "")
        val version = root.optInt("version", 0)
        if (format != "smsgram-templates" || version != 1) {
            return DecodeResult(errorCode = "unsupported_format")
        }

        val templatesArray = root.optJSONArray("templates") ?: return DecodeResult(errorCode = "unsupported_format")
        if (templatesArray.length() > 300) {
            return DecodeResult(errorCode = "too_many_templates")
        }

        val validTemplates = mutableListOf<RuleTemplateEntity>()
        val warnings = mutableListOf<String>()
        var duplicateCount = 0
        val currentChecksums = mutableSetOf<String>().apply { addAll(existingChecksums) }

        for (i in 0 until templatesArray.length()) {
            val obj = templatesArray.optJSONObject(i) ?: continue

            val rawName = obj.optString("name", "")
            val name = sanitizeString(rawName, 40)
            if (name.isEmpty() || name.length > 40) {
                warnings.appendWarning("قالب شماره ${i + 1} به دلیل نامعتبر بودن نام حذف شد.")
                continue
            }

            val rawDesc = obj.optString("description", "")
            val description = sanitizeString(rawDesc, 120)

            var senderPattern = sanitizeString(obj.optString("senderPattern", ""), 100)
            if (isPersonalMobileNumber(senderPattern)) {
                warnings.appendWarning("شماره موبایل شخصی در قالب «$name» حذف شد.")
                senderPattern = ""
            }

            var targetFolderId = sanitizeString(obj.optString("targetFolderId", "shopping"), 50)
            if (targetFolderId.lowercase() == "spam") {
                warnings.appendWarning("پوشه هرزنامه در قالب «$name» مجاز نیست و به خرید تغییر یافت.")
                targetFolderId = "shopping"
            } else if (existingFolderIds.isNotEmpty() && targetFolderId !in existingFolderIds) {
                warnings.appendWarning("پوشه نامعتبر در قالب «$name» به خرید تغییر یافت.")
                targetFolderId = "shopping"
            }
            if (targetFolderId.isEmpty()) {
                targetFolderId = "shopping"
            }

            val isRegex = obj.optBoolean("isRegex", false)
            if (isRegex) {
                if (senderPattern.length > 200) {
                    warnings.appendWarning("عبارت منظم (Regex) در قالب «$name» بیش از حد طولانی است و حذف شد.")
                    continue
                }
                if (hasNestedQuantifiers(senderPattern)) {
                    warnings.appendWarning("عبارت منظم (Regex) در قالب «$name» دارای تکرار تودرتو نامعتبر است و حذف شد.")
                    continue
                }
                try {
                    senderPattern.toRegex()
                } catch (e: Exception) {
                    warnings.appendWarning("عبارت منظم (Regex) نامعتبر در قالب «$name» حذف شد.")
                    continue
                }
            }

            val keywordsArray = obj.optJSONArray("keywords")
            val validKeywords = mutableListOf<String>()
            if (keywordsArray != null) {
                for (k in 0 until keywordsArray.length()) {
                    if (validKeywords.size >= 3) break
                    val kw = sanitizeString(keywordsArray.optString(k, ""), 30)
                    if (kw.length >= 2) {
                        validKeywords.add(kw)
                    } else if (kw.isNotEmpty()) {
                        warnings.appendWarning("کلمه کلیدی کوتاه در قالب «$name» حذف شد.")
                    }
                }
            }
            val keywordsStr = validKeywords.joinToString("\n")

            if (senderPattern.isBlank() && validKeywords.isEmpty()) {
                warnings.appendWarning("قالب «$name» به دلیل خالی بودن الگو و کلمات کلیدی حذف شد.")
                continue
            }

            val checksum = computeChecksum(name, senderPattern, targetFolderId, keywordsStr)
            if (currentChecksums.contains(checksum)) {
                duplicateCount++
                warnings.appendWarning("قالب «$name» تکراری است و قبلاً اضافه شده است.")
                continue
            }
            currentChecksums.add(checksum)

            val now = System.currentTimeMillis()
            val entity = RuleTemplateEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                senderPattern = senderPattern,
                targetFolderId = targetFolderId,
                keywords = keywordsStr,
                isRegex = isRegex,
                origin = "imported",
                isEnabledByDefault = false,
                checksum = checksum,
                createdAt = now,
                updatedAt = now
            )
            validTemplates.add(entity)
        }

        return DecodeResult(
            templates = validTemplates,
            validCount = validTemplates.size,
            duplicateCount = duplicateCount,
            warnings = warnings
        )
    }

    private fun MutableList<String>.appendWarning(msg: String) {
        if (!contains(msg)) {
            add(msg)
        }
    }
}
