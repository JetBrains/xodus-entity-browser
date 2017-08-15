package com.lehvolk.xodus.web.search

import java.util.regex.Pattern

enum class SearchTermType {
    VALUE,
    LIKE,
    RANGE;
}

enum class SearchType {
    PROPERTY,
    LINK;
}

class SearchTerm(val property: String, val value: Any?, val termType: SearchTermType, val searchType: SearchType) {
    data class Range(val start: Long, val end: Long) {
        override fun toString() = "[$start, $end]"
    }

    companion object {
        private val RANGE_PATTERN = Pattern.compile("\\[\\s*(\\d*)\\s*,\\s*(\\d*)\\s*\\]")

        @JvmStatic
        fun from(rawProperty: String, rawOperand: String, rawValue: String): SearchTerm {
            val property = prepare(rawProperty)
            val operand = prepare(rawOperand)
            val value = prepare(rawValue)

            val searchType = if (property.startsWith("@")) SearchType.LINK else SearchType.PROPERTY
            val isNullValue = rawValue == "null"
            val matcher = RANGE_PATTERN.matcher(value)
            return if (matcher.matches()) {
                SearchTerm(
                        property,
                        Range(matcher.group(1).toLong(), matcher.group(2).toLong()),
                        SearchTermType.RANGE,
                        searchType
                )
            } else {
                SearchTerm(
                        property,
                        if (isNullValue) null else value,
                        if (operand == "~") SearchTermType.LIKE else SearchTermType.VALUE,
                        searchType
                )
            }
        }

        private fun prepare(value: String): String {
            if (value.length <= 1) {
                return value
            }

            val first = value[0]
            val last = value[value.length - 1]
            return if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                removeDoubleQuotes(value.substring(1, value.length - 1))
            } else {
                removeDoubleQuotes(value)
            }
        }

        private fun removeDoubleQuotes(value: String): String {
            if (value.length <= 1) {
                return value
            }

            return value.replace("''", "'")
        }
    }
}