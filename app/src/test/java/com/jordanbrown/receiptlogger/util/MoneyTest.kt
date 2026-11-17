package com.jordanbrown.receiptlogger.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset

class MoneyTest {

    private fun pence(input: String) = (Money.parse(input) as Money.ParseResult.Ok).pence
    private fun isError(input: String) = Money.parse(input) is Money.ParseResult.Error

    @Test fun parsesWholePounds() = assertEquals(1200L, pence("12"))
    @Test fun parsesPoundsAndPence() = assertEquals(1250L, pence("12.50"))
    @Test fun parsesSingleDecimal() = assertEquals(1250L, pence("12.5"))
    @Test fun parsesTrailingPoint() = assertEquals(1200L, pence("12."))
    @Test fun parsesPenceOnly() = assertEquals(50L, pence(".50"))
    @Test fun ignoresPoundSignCommasAndSpaces() = assertEquals(123_456L, pence(" £1,234.56 "))
    @Test fun noFloatingPointError() = assertEquals(29L, pence("0.29"))

    @Test fun rejectsEmpty() = assertTrue(isError(""))
    @Test fun rejectsZero() = assertTrue(isError("0.00"))
    @Test fun rejectsNegative() = assertTrue(isError("-5"))
    @Test fun rejectsThreeDecimals() = assertTrue(isError("1.234"))
    @Test fun rejectsLetters() = assertTrue(isError("abc"))
    @Test fun rejectsTwoPoints() = assertTrue(isError("1.2.3"))
    @Test fun rejectsOverLimit() = assertTrue(isError("100000.01"))
    @Test fun rejectsHugeNumberWithoutCrashing() = assertTrue(isError("99999999999999999999999"))

    @Test fun formatsAsPounds() = assertEquals("£1,234.50", Money.format(123_450L))
    @Test fun formatsZero() = assertEquals("£0.00", Money.format(0L))
    @Test fun toInputRoundTrips() = assertEquals(1250L, pence(Money.toInput(1250L)))

    @Test fun parsesExifDate() = assertEquals(
        1_794_910_445_000L,
        DateTimeUtil.parseExif("2026:11:17 10:14:05", ZoneOffset.UTC)
    )
    @Test fun badExifDateIsNull() = assertNull(DateTimeUtil.parseExif("not a date", ZoneOffset.UTC))
    @Test fun formatsDisplayDate() = assertEquals(
        "17 Nov 2026, 10:14",
        DateTimeUtil.format(1_794_910_445_000L, ZoneOffset.UTC)
    )
}
