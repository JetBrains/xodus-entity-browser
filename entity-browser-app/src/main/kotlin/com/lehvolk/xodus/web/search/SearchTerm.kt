package com.lehvolk.xodus.web.search

import java.util.regex.Pattern

enum class SearchTermType {
    VALUE,
    LIKE,
    RANGE;
}

class SearchTerm(val property: String, val value: Any, val type: SearchTermType) {
    data class Range(val start: Long, val end: Long) {
        override fun toString() = "[$start, $end]"
    }

    companion object {
        private val RANGE_PATTERN = Pattern.compile("\\[\\s*(\\d*)\\s*,\\s*(\\d*)\\s*\\]")

        @JvmStatic
        fun from(property: String, operand: String, value: String): SearchTerm {
            val matcher = RANGE_PATTERN.matcher(value)
            return if (matcher.matches()) {
                SearchTerm(
                        property,
                        Range(matcher.group(1).toLong(), matcher.group(2).toLong()),
                        SearchTermType.RANGE
                )
            } else {
                SearchTerm(
                        property,
                        value,
                        if (operand == "~") SearchTermType.LIKE else SearchTermType.VALUE
                )
            }
        }
    }
}