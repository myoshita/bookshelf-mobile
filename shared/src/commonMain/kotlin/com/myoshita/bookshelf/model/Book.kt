package com.myoshita.bookshelf.model

import com.myoshita.bookshelf.util.Fuzzy
import com.myoshita.bookshelf.util.Kana.hiraganaToKatakana
import com.myoshita.bookshelf.util.Kana.katakanaToHiragana
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Book(
    val id: Int,
    val title: String,
    val titleTranscription: String,
    val seriesTitle: String,
    val authors: List<Author>,
    val publisher: String,
    val extent: Int,
    val edition: String,
    val volume: String,
    val issued: String,
    val isbn: String,
    val description: String,
    val thumbnailUrl: String,
    val ndlThumbnailUrl: String,
    val googleBookThumbnailUrl: String,
    val obi: String,
    val memo: String,
    val tags: List<BookTag>,
    val createdAt: Long,
) {
    companion object {
        val Empty = Book(
            id = 0,
            title = "",
            titleTranscription = "",
            seriesTitle = "",
            authors = emptyList(),
            publisher = "",
            extent = 0,
            edition = "",
            volume = "",
            issued = "",
            isbn = "",
            description = "",
            thumbnailUrl = "",
            ndlThumbnailUrl = "",
            googleBookThumbnailUrl = "",
            obi = "",
            memo = "",
            tags = emptyList(),
            createdAt = Clock.System.now().toEpochMilliseconds(),
        )
    }
}

val Book.isSigned: Boolean
    get() = tags.contains(BookTag.Signed)

fun Book.authorText(): String = authors.joinToString(", ") { it.name }

fun Book.buildTitleText(
    hasVolume: Boolean = true,
    hasEdition: Boolean = true,
    hasSeriesTitle: Boolean = true,
): String = buildString {
    append(title)
    if (hasVolume && volume.isNotEmpty()) {
        append(" ($volume)")
    }
    if (hasEdition && edition.isNotEmpty()) {
        append(" <$edition>")
    }
    if (hasSeriesTitle && seriesTitle.isNotEmpty()) {
        append(" ($seriesTitle)")
    }
}

@OptIn(FormatStringsInDatetimeFormats::class)
fun Book.createdDate(): String = Instant.fromEpochMilliseconds(createdAt).format(
    DateTimeComponents.Format {
        byUnicodePattern("yyyy年MM月dd日")
    },
)

fun List<Book>.sortedBy(sort: Sort): List<Book> = when (sort.order) {
    SortOrder.Asc -> {
        when (sort.key) {
            SortKey.CreatedAt -> sortedBy { it.createdAt }
            SortKey.Title -> sortedBy { it.title }
        }
    }

    SortOrder.Desc -> {
        when (sort.key) {
            SortKey.CreatedAt -> sortedByDescending { it.createdAt }
            SortKey.Title -> sortedByDescending { it.title }
        }
    }
}

fun Book.similarity(searchQuery: String): Double = maxOf(
    Fuzzy.jaroWinklerSimilarity(searchQuery, title),
    Fuzzy.jaroWinklerSimilarity(searchQuery, titleTranscription),
    Fuzzy.jaroWinklerSimilarity(
        searchQuery.katakanaToHiragana(),
        titleTranscription.katakanaToHiragana(),
    ),
    Fuzzy.jaroWinklerSimilarity(
        searchQuery.hiraganaToKatakana(),
        titleTranscription.hiraganaToKatakana(),
    ),
    authors.maxOfOrNull { Fuzzy.jaroWinklerSimilarity(searchQuery, it.name) }
        ?: Double.MIN_VALUE,
    authors.maxOfOrNull { Fuzzy.jaroWinklerSimilarity(searchQuery, it.transcription) }
        ?: Double.MIN_VALUE,
    authors.maxOfOrNull {
        Fuzzy.jaroWinklerSimilarity(
            searchQuery.katakanaToHiragana(),
            it.transcription.katakanaToHiragana(),
        )
    } ?: Double.MIN_VALUE,
    authors.maxOfOrNull {
        Fuzzy.jaroWinklerSimilarity(
            searchQuery.hiraganaToKatakana(),
            it.transcription.hiraganaToKatakana(),
        )
    } ?: Double.MIN_VALUE,
    Fuzzy.jaroWinklerSimilarity(searchQuery, description),
)
