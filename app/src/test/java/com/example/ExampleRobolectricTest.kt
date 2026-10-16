package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.util.JalaliCalendar
import com.example.domain.util.SenderNormalizer
import com.example.domain.util.SmsEncodingUtils
import com.example.domain.util.SmartTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SMSGRAM", appName)
    }

    @Test
    fun `test jalali calendar conversion`() {
        val (jy, jm, jd) = JalaliCalendar.gregorianToJalali(2024, 3, 20)
        assertEquals(1403, jy)
        assertEquals(1, jm)
        assertEquals(1, jd)
    }

    @Test
    fun `test shamsi date utils conversions and helpers`() {
        val shamsi = com.example.domain.util.ShamsiDateUtils.gregorianToShamsi(2024, 3, 20)
        assertEquals(1403, shamsi.year)
        assertEquals(1, shamsi.month)
        assertEquals(1, shamsi.day)
        assertEquals("فروردین", shamsi.monthName)

        val cal = java.util.Calendar.getInstance().apply {
            set(2024, java.util.Calendar.MARCH, 20, 15, 30)
        }
        val formattedTime = com.example.domain.util.ShamsiDateUtils.formatTime(cal.timeInMillis, persianDigits = true)
        assertEquals("۱۵:۳۰", formattedTime)

        val formattedDate = com.example.domain.util.ShamsiDateUtils.formatFullDateTime(cal.timeInMillis, persianDigits = true)
        assertTrue(formattedDate.contains("فروردین"))
        assertTrue(formattedDate.contains("۱۴۰۳"))
    }

    @Test
    fun `test persian numerals conversion`() {
        val converted = JalaliCalendar.toPersianDigits("0123456789")
        assertEquals("۰۱۲۳۴۵۶۷۸۹", converted)
    }

    @Test
    fun `test sms encoding calculations`() {
        val asciiText = "Hello SMSGRAM"
        val countInfo = SmsEncodingUtils.calculateSmsCount(asciiText)
        assertEquals(SmsEncodingUtils.EncodingType.GSM_7BIT, countInfo.encoding)
        assertEquals(1, countInfo.segmentCount)

        val persianText = "سلام به پیام‌رسان اس‌ام‌اس‌گرام"
        val persianInfo = SmsEncodingUtils.calculateSmsCount(persianText)
        assertEquals(SmsEncodingUtils.EncodingType.UNICODE, persianInfo.encoding)
    }

    @Test
    fun `test smart text parsing for OTP and Bank Card`() {
        val otpSample = "کد تایید شما برای ورود به همراه بانک 584920 است. از افشای آن خودداری فرمایید."
        val parsedOtp = SmartTextParser.parse(otpSample)
        assertEquals("584920", parsedOtp.otpCode)

        val bankSample = "واریز به حساب: مبلغ ۵۰۰,۰۰۰ ریال به کارت ۶۰۳۷۹۹۱۸۱۲۳۴۵۶۷۸ واریز شد."
        val parsedBank = SmartTextParser.parse(bankSample)
        assertNotNull(parsedBank.bankCard)
        assertEquals("6037991812345678", parsedBank.bankCard)
    }

    @Test
    fun `test sender normalizer and wildcard rules`() {
        val matched = SenderNormalizer.matchesRule("+982188889999", "9821*", false)
        assertTrue(matched)

        val shortcodeMatched = SenderNormalizer.matchesRule("580855", "580855", false)
        assertTrue(shortcodeMatched)
    }

    @Test
    fun `test main activity creation`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
        controller.create().start().resume()
        val activity = controller.get()
        assertNotNull(activity)
    }

    @Test
    fun `test main activity with onboarding completed`() {
        val app = ApplicationProvider.getApplicationContext<SmsGramApp>()
        app.preferences.setOnboardingCompleted(true)
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
        controller.create().start().resume()
        val activity = controller.get()
        assertNotNull(activity)
    }

    @Test
    fun `test main activity with permissions and onboarding completed`() {
        val app = ApplicationProvider.getApplicationContext<SmsGramApp>()
        app.preferences.setOnboardingCompleted(true)
        org.robolectric.Shadows.shadowOf(app).grantPermissions(
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_PHONE_STATE
        )
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
        controller.create().start().resume()
        val activity = controller.get()
        assertNotNull(activity)
    }

    @Test
    fun `test launch from contacts with smsto intent`() {
        val app = ApplicationProvider.getApplicationContext<SmsGramApp>()
        app.preferences.setOnboardingCompleted(true)
        org.robolectric.Shadows.shadowOf(app).grantPermissions(
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_PHONE_STATE
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("smsto:09123456789")
        }
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java, intent)
        controller.create().start().resume()
        val activity = controller.get()
        assertNotNull(activity)
    }
}
