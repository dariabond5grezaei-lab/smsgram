package com.example

import com.example.data.local.RuleTemplateEntity
import com.example.domain.templates.TemplateCodec
import com.example.domain.templates.TemplateValidator
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TemplateCodecTest {

    @Test
    fun testRoundTrip() {
        val original = RuleTemplateEntity(
            id = "test-id-1",
            name = "بانک سامان",
            description = "تراکنش‌های سامان",
            senderPattern = "SamanBank",
            targetFolderId = "bank",
            keywords = "واریز\nبرداشت",
            isRegex = false,
            origin = "user",
            isEnabledByDefault = false,
            checksum = TemplateValidator.computeChecksum("بانک سامان", "SamanBank", "bank", "واریز\nبرداشت")
        )

        val encoded = TemplateCodec.encode(listOf(original), removePersonalNumbers = false)
        val decoded = TemplateCodec.decode(encoded, existingFolderIds = setOf("bank", "shopping"))

        assertEquals(1, decoded.validCount)
        assertEquals(0, decoded.duplicateCount)
        assertEquals(null, decoded.errorCode)
        val item = decoded.templates[0]
        assertEquals("بانک سامان", item.name)
        assertEquals("تراکنش‌های سامان", item.description)
        assertEquals("SamanBank", item.senderPattern)
        assertEquals("bank", item.targetFolderId)
        assertEquals("واریز\nبرداشت", item.keywords)
        assertEquals(false, item.isRegex)
    }

    @Test
    fun testPersonalNumberRemoval() {
        val patterns = listOf(
            "09123456789",
            "+989123456789",
            "00989123456789",
            "۰۹۱۲۳۴۵۶۷۸۹"
        )

        val templates = patterns.mapIndexed { idx, pattern ->
            RuleTemplateEntity(
                id = "id-$idx",
                name = "قالب $idx",
                senderPattern = pattern,
                targetFolderId = "shopping",
                keywords = "تست",
                checksum = "cs-$idx"
            )
        }

        val encoded = TemplateCodec.encode(templates, removePersonalNumbers = true)
        val json = JSONObject(encoded)
        val array = json.getJSONArray("templates")

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            assertEquals("", obj.getString("senderPattern"))
        }
    }

    @Test
    fun testRemovePersonalNumbersFalseKeepsValues() {
        val template = RuleTemplateEntity(
            id = "id-1",
            name = "دوست",
            senderPattern = "09123456789",
            targetFolderId = "shopping",
            keywords = "سلام",
            checksum = "cs-1"
        )

        val encoded = TemplateCodec.encode(listOf(template), removePersonalNumbers = false)
        val json = JSONObject(encoded)
        val array = json.getJSONArray("templates")
        val obj = array.getJSONObject(0)

        assertEquals("09123456789", obj.getString("senderPattern"))
    }

    @Test
    fun testFormatOther() {
        val json = """
            {
                "format": "other-format",
                "version": 1,
                "templates": []
            }
        """.trimIndent()

        val decoded = TemplateCodec.decode(json)
        assertEquals("unsupported_format", decoded.errorCode)
        assertEquals(0, decoded.validCount)
    }

    @Test
    fun testVersion2() {
        val json = """
            {
                "format": "smsgram-templates",
                "version": 2,
                "templates": []
            }
        """.trimIndent()

        val decoded = TemplateCodec.decode(json)
        assertEquals("unsupported_format", decoded.errorCode)
        assertEquals(0, decoded.validCount)
    }

    @Test
    fun test301Templates() {
        val root = JSONObject()
        root.put("format", "smsgram-templates")
        root.put("version", 1)
        val array = JSONArray()

        for (i in 0 until 301) {
            val t = JSONObject()
            t.put("name", "قالب $i")
            t.put("senderPattern", "sender$i")
            t.put("targetFolderId", "shopping")
            array.put(t)
        }
        root.put("templates", array)

        val decoded = TemplateCodec.decode(root.toString())
        assertEquals("too_many_templates", decoded.errorCode)
        assertEquals(0, decoded.validCount)
    }

    @Test
    fun testFileOver512KB() {
        val base = """{"format":"smsgram-templates","version":1,"templates":[]}"""
        val padding = " ".repeat(512 * 1024 + 10)
        val largeJson = base + padding

        val decoded = TemplateCodec.decode(largeJson)
        assertEquals("file_too_large", decoded.errorCode)
        assertEquals(0, decoded.validCount)
    }

    @Test
    fun testOneCharacterKeywordDropped() {
        val json = """
            {
                "format": "smsgram-templates",
                "version": 1,
                "templates": [
                    {
                        "name": "تست کلیدواژه",
                        "senderPattern": "test_sender",
                        "targetFolderId": "shopping",
                        "keywords": ["a", "معتبر", "x"]
                    }
                ]
            }
        """.trimIndent()

        val decoded = TemplateCodec.decode(json, existingFolderIds = setOf("shopping"))
        assertEquals(1, decoded.validCount)
        assertEquals("معتبر", decoded.templates[0].keywords)
        assertTrue(decoded.warnings.any { it.contains("کوتاه") })
    }

    @Test
    fun testBadRegexDropsOnlyThatTemplate() {
        val json = """
            {
                "format": "smsgram-templates",
                "version": 1,
                "templates": [
                    {
                        "name": "قالب خراب با تکرار تودرتو",
                        "senderPattern": "(a+)+",
                        "isRegex": true,
                        "targetFolderId": "shopping"
                    },
                    {
                        "name": "قالب سالم",
                        "senderPattern": "safe_sender",
                        "isRegex": false,
                        "targetFolderId": "shopping"
                    }
                ]
            }
        """.trimIndent()

        val decoded = TemplateCodec.decode(json, existingFolderIds = setOf("shopping"))
        assertEquals(1, decoded.validCount)
        assertEquals("قالب سالم", decoded.templates[0].name)
        assertTrue(decoded.warnings.any { it.contains("تودرتو") || it.contains("Regex") })
    }

    @Test
    fun testUnknownFolderBecomesShopping() {
        val json = """
            {
                "format": "smsgram-templates",
                "version": 1,
                "templates": [
                    {
                        "name": "پوشه نامشخص",
                        "senderPattern": "sender1",
                        "targetFolderId": "completely_unknown_folder_xyz"
                    }
                ]
            }
        """.trimIndent()

        val decoded = TemplateCodec.decode(json, existingFolderIds = setOf("bank", "shopping"))
        assertEquals(1, decoded.validCount)
        assertEquals("shopping", decoded.templates[0].targetFolderId)
        assertTrue(decoded.warnings.any { it.contains("خرید") })
    }

    @Test
    fun testImportedSpamBecomesShopping() {
        val json = """
            {
                "format": "smsgram-templates",
                "version": 1,
                "templates": [
                    {
                        "name": "هرزنامه",
                        "senderPattern": "spam_sender",
                        "targetFolderId": "spam"
                    }
                ]
            }
        """.trimIndent()

        val decoded = TemplateCodec.decode(json, existingFolderIds = setOf("bank", "shopping"))
        assertEquals(1, decoded.validCount)
        assertEquals("shopping", decoded.templates[0].targetFolderId)
        assertTrue(decoded.warnings.any { it.contains("هرزنامه") && it.contains("خرید") })
    }

    @Test
    fun testPersonalNumberInImportedSenderPatternDroppedWithWarning() {
        val json = """
            {
                "format": "smsgram-templates",
                "version": 1,
                "templates": [
                    {
                        "name": "قالب شماره شخصی",
                        "senderPattern": "09123456789",
                        "targetFolderId": "shopping",
                        "keywords": ["واریز"]
                    }
                ]
            }
        """.trimIndent()

        val decoded = TemplateCodec.decode(json, existingFolderIds = setOf("shopping"))
        assertEquals(1, decoded.validCount)
        assertEquals("", decoded.templates[0].senderPattern)
        assertTrue(decoded.warnings.any { it.contains("شماره موبایل شخصی") })
    }

    @Test
    fun testDuplicateChecksumCountedAsDuplicate() {
        val json = """
            {
                "format": "smsgram-templates",
                "version": 1,
                "templates": [
                    {
                        "name": "قالب تکرار",
                        "senderPattern": "duplicate_sender",
                        "targetFolderId": "shopping",
                        "keywords": ["تکرار"]
                    },
                    {
                        "name": "قالب تکرار",
                        "senderPattern": "duplicate_sender",
                        "targetFolderId": "shopping",
                        "keywords": ["تکرار"]
                    }
                ]
            }
        """.trimIndent()

        val decoded = TemplateCodec.decode(json, existingFolderIds = setOf("shopping"))
        assertEquals(1, decoded.validCount)
        assertEquals(1, decoded.duplicateCount)
        assertTrue(decoded.warnings.any { it.contains("تکراری") })
    }

    @Test
    fun testEmptySenderPlusNoKeywordsDropped() {
        val json = """
            {
                "format": "smsgram-templates",
                "version": 1,
                "templates": [
                    {
                        "name": "قالب خالی",
                        "senderPattern": "",
                        "targetFolderId": "shopping",
                        "keywords": []
                    }
                ]
            }
        """.trimIndent()

        val decoded = TemplateCodec.decode(json, existingFolderIds = setOf("shopping"))
        assertEquals(0, decoded.validCount)
        assertEquals(0, decoded.templates.size)
        assertTrue(decoded.warnings.any { it.contains("خالی بودن") })
    }
}
