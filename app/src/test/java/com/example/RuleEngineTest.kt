package com.example

import com.example.data.local.SenderRuleEntity
import com.example.domain.rules.RuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {

    @Test
    fun keywordOnlyRuleMatchesNonContactBodyWithNormalization() {
        val rule = SenderRuleEntity(
            id = 1,
            senderPattern = "",
            targetFolderId = "shopping",
            keywords = "دیجی کالا",
            isEnabled = true
        )

        // Non-contact with normalized matching
        val matches1 = RuleEngine.matches(rule, sender = "2000100", body = "سفارش شما در دیجیکالا ثبت شد.", isContact = false)
        assertTrue(matches1)

        val matches2 = RuleEngine.matches(rule, sender = "2000100", body = "سفارش شما در دیجی‌کالا ثبت شد.", isContact = false)
        assertTrue(matches2)

        val matches3 = RuleEngine.matches(rule, sender = "2000100", body = "سفارش شما در دیـــجی کالا ثبت شد.", isContact = false)
        assertTrue(matches3)
    }

    @Test
    fun keywordOnlyRuleDoesNotMatchContact() {
        val rule = SenderRuleEntity(
            id = 1,
            senderPattern = "",
            targetFolderId = "shopping",
            keywords = "دیجی کالا",
            isEnabled = true
        )

        val matches = RuleEngine.matches(rule, sender = "09121234567", body = "سلام دیجی کالا تخفیف داره", isContact = true)
        assertFalse("Keyword-only rule must never match contacts", matches)
    }

    @Test
    fun ruleWithSenderAndKeywordNeedsBoth() {
        val rule = SenderRuleEntity(
            id = 2,
            senderPattern = "Snapp*",
            targetFolderId = "travel",
            keywords = "فاکتور",
            isEnabled = true
        )

        // Matching sender but not keyword
        assertFalse(RuleEngine.matches(rule, sender = "SnappFood", body = "سفارش شما ارسال شد", isContact = false))

        // Matching keyword but not sender
        assertFalse(RuleEngine.matches(rule, sender = "Tap30", body = "فاکتور سفر شما", isContact = false))

        // Matching both sender and keyword
        assertTrue(RuleEngine.matches(rule, sender = "Snapp", body = "فاکتور سفر شما آماده است", isContact = false))
    }

    @Test
    fun disabledRuleNeverMatchesInFirstMatchingFolder() {
        val disabledRule = SenderRuleEntity(
            id = 3,
            senderPattern = "BankMelli",
            targetFolderId = "bank",
            keywords = "",
            isEnabled = false
        )

        val folder = RuleEngine.firstMatchingFolder(
            rules = listOf(disabledRule),
            sender = "BankMelli",
            body = "واریز شد",
            isContact = false
        )
        assertNull(folder)
    }

    @Test
    fun firstRuleInListWins() {
        val rule1 = SenderRuleEntity(
            id = 10,
            senderPattern = "Bank*",
            targetFolderId = "priority_folder",
            isEnabled = true
        )
        val rule2 = SenderRuleEntity(
            id = 11,
            senderPattern = "Bank*",
            targetFolderId = "secondary_folder",
            isEnabled = true
        )

        val folder = RuleEngine.firstMatchingFolder(
            rules = listOf(rule1, rule2),
            sender = "BankMellat",
            body = "پیامک",
            isContact = false
        )
        assertEquals("priority_folder", folder)
    }

    @Test
    fun describeReturnsSentenceContainingFolderName() {
        val senderOnlyRule = SenderRuleEntity(
            id = 20,
            senderPattern = "Snapp",
            targetFolderId = "travel",
            keywords = "",
            isEnabled = true
        )
        val describedFaSender = RuleEngine.describe(senderOnlyRule, "سفر", isFa = true)
        assertTrue(describedFaSender.contains("سفر"))
        assertTrue(describedFaSender.contains("Snapp"))

        val describedEnSender = RuleEngine.describe(senderOnlyRule, "Travel", isFa = false)
        assertTrue(describedEnSender.contains("Travel"))
        assertTrue(describedEnSender.contains("Snapp"))

        val keywordOnlyRule = SenderRuleEntity(
            id = 21,
            senderPattern = "",
            targetFolderId = "work",
            keywords = "فاکتور",
            isEnabled = true
        )
        val describedFaKeyword = RuleEngine.describe(keywordOnlyRule, "کار", isFa = true)
        assertTrue(describedFaKeyword.contains("کار"))
        assertTrue(describedFaKeyword.contains("فاکتور"))

        val describedEnKeyword = RuleEngine.describe(keywordOnlyRule, "Work", isFa = false)
        assertTrue(describedEnKeyword.contains("Work"))
        assertTrue(describedEnKeyword.contains("فاکتور"))
    }

    @Test
    fun singleLetterKeywordMatchesNothingAndTwoLetterMatchesNormally() {
        val singleLetterRule = SenderRuleEntity(
            id = 30,
            senderPattern = "",
            targetFolderId = "promo",
            keywords = "a",
            isEnabled = true
        )
        // Single letter keyword must be ignored, rule with only short keywords matches nothing
        val matchSingle = RuleEngine.matches(singleLetterRule, sender = "2000", body = "a big discount", isContact = false)
        assertFalse("Single-letter keyword must match nothing", matchSingle)

        val twoLetterRule = SenderRuleEntity(
            id = 31,
            senderPattern = "",
            targetFolderId = "promo",
            keywords = "ab",
            isEnabled = true
        )
        val matchTwo = RuleEngine.matches(twoLetterRule, sender = "2000", body = "about a big discount", isContact = false)
        assertTrue("Two-letter keyword must match normally", matchTwo)
    }

    @Test
    fun spamRuleDoesNotMatchContactAndNextRuleIsUsed() {
        val spamRule = SenderRuleEntity(
            id = 40,
            senderPattern = "0912*",
            targetFolderId = "spam",
            keywords = "",
            isEnabled = true
        )
        val personalRule = SenderRuleEntity(
            id = 41,
            senderPattern = "0912*",
            targetFolderId = "personal",
            keywords = "",
            isEnabled = true
        )

        // Contact sender: spam rule skipped, personal rule chosen
        val folderForContact = RuleEngine.firstMatchingFolder(
            rules = listOf(spamRule, personalRule),
            sender = "09121234567",
            body = "سلام دوست من",
            isContact = true
        )
        assertEquals("personal", folderForContact)

        // Non-contact sender: spam rule matches
        val folderForNonContact = RuleEngine.firstMatchingFolder(
            rules = listOf(spamRule, personalRule),
            sender = "09121234567",
            body = "تبلیغ ویژه",
            isContact = false
        )
        assertEquals("spam", folderForNonContact)
    }
}
