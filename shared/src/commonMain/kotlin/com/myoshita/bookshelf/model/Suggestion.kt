package com.myoshita.bookshelf.model

import com.myoshita.bookshelf.util.Fuzzy
import com.myoshita.bookshelf.util.Kana.katakanaToHiragana

sealed interface Suggestion {
    val name: String
    val transcription: String

    data class Author(override val name: String, override val transcription: String) : Suggestion
    data class Title(override val name: String, override val transcription: String) : Suggestion
}

fun Book.toSuggestions(): List<Suggestion> {
    val authors = authors.map { Suggestion.Author(it.name, it.transcription) }
    val title = Suggestion.Title(title, titleTranscription)
    return authors + title
}

fun Suggestion.calculateSimilarity(searchQuery: String): Double {
    if (searchQuery.isBlank()) return 0.0

    val normalizedSearchQuery = searchQuery.trim().lowercase().katakanaToHiragana()
    val normalizedName = name.trim().lowercase().katakanaToHiragana()
    val normalizedTranscription = transcription.trim().katakanaToHiragana()

    return when {
        normalizedSearchQuery == normalizedName -> 1.0
        normalizedSearchQuery == normalizedTranscription -> 1.0
        normalizedName.startsWith(normalizedSearchQuery) -> 0.9
        normalizedTranscription.startsWith(normalizedSearchQuery) -> 0.9
        else -> {
            if (normalizedSearchQuery.length >= 3) {
                when {
                    normalizedName.contains(normalizedSearchQuery) -> 0.7
                    normalizedTranscription.contains(normalizedSearchQuery) -> 0.7
                    else -> maxOf(
                        Fuzzy.jaroWinklerSimilarity(normalizedName, normalizedSearchQuery),
                        Fuzzy.jaroWinklerSimilarity(normalizedTranscription, normalizedSearchQuery),
                    )
                }
            } else {
                maxOf(
                    Fuzzy.jaroWinklerSimilarity(normalizedName, normalizedSearchQuery),
                    Fuzzy.jaroWinklerSimilarity(normalizedTranscription, normalizedSearchQuery),
                )
            }
        }
    }
}
