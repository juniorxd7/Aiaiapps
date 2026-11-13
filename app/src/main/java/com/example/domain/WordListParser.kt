package com.example.domain

object WordListParser {
    private val delimiterRegex = Regex("[\\r\\n,;]+")
    private val multiWhitespaceRegex = Regex("\\s+")

    fun normalizeWord(raw: String): String {
        return raw.trim().replace(multiWhitespaceRegex, " ")
    }

    fun parseWords(rawText: String): List<String> {
        if (rawText.isBlank()) return emptyList()
        val seen = LinkedHashSet<String>()
        val result = mutableListOf<String>()
        rawText.split(delimiterRegex).forEach { token ->
            val cleaned = normalizeWord(token)
            if (cleaned.isNotEmpty()) {
                val key = cleaned.lowercase()
                if (seen.add(key)) {
                    result.add(cleaned)
                }
            }
        }
        return result
    }
}
