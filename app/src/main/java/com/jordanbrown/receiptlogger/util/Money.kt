package com.jordanbrown.receiptlogger.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/** Converts between what the user types (e.g. "12.50") and pence stored in the database. */
object Money {

    /** Upper limit for a single receipt: £100,000.00. Catches accidental extra zeros. */
    const val MAX_PENCE = 10_000_000L

    sealed interface ParseResult {
        data class Ok(val pence: Long) : ParseResult
        data class Error(val message: String) : ParseResult
    }

    private val validNumber = Regex("""^(\d+(\.\d{0,2})?|\.\d{1,2})$""")

    fun parse(input: String): ParseResult {
        val cleaned = input.trim().removePrefix("£").replace(",", "").trim()

        if (cleaned.isEmpty()) return ParseResult.Error("Enter the amount to claim")
        if (cleaned.startsWith("-")) return ParseResult.Error("Amount can't be negative")
        if (!validNumber.matches(cleaned)) {
            val decimals = cleaned.substringAfter('.', "")
            return if (cleaned.count { it == '.' } == 1 && decimals.length > 2 && decimals.all(Char::isDigit)) {
                ParseResult.Error("Use no more than 2 decimal places, e.g. 12.50")
            } else {
                ParseResult.Error("Enter a number, e.g. 12.50")
            }
        }

        val pounds = BigDecimal(cleaned)
        if (pounds.signum() == 0) return ParseResult.Error("Amount must be more than £0.00")
        if (pounds > BigDecimal.valueOf(MAX_PENCE, 2)) {
            return ParseResult.Error("Amount can't be more than ${format(MAX_PENCE)}")
        }
        return ParseResult.Ok(pounds.movePointRight(2).longValueExact())
    }

    /** 1250 -> "£12.50" */
    fun format(pence: Long): String =
        NumberFormat.getCurrencyInstance(Locale.UK).format(BigDecimal.valueOf(pence, 2))

    /** 1250 -> "12.50", used to pre-fill the amount field when editing. */
    fun toInput(pence: Long): String = BigDecimal.valueOf(pence, 2).toPlainString()
}
