package com.myoshita.bookshelf.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.model.BookInfo
import com.myoshita.bookshelf.model.BookTag
import kotlin.time.Clock

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val titleTranscription: String,
    val seriesTitle: String,
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
    val createdAt: Long = Clock.System.now().epochSeconds,
) {
    companion object {
        fun from(book: Book): BookEntity = BookEntity(
            id = book.id,
            title = book.title,
            titleTranscription = book.titleTranscription,
            seriesTitle = book.seriesTitle,
            publisher = book.publisher,
            extent = book.extent,
            edition = book.edition,
            volume = book.volume,
            issued = book.issued,
            isbn = book.isbn,
            description = book.description,
            thumbnailUrl = book.thumbnailUrl,
            ndlThumbnailUrl = book.ndlThumbnailUrl,
            googleBookThumbnailUrl = book.googleBookThumbnailUrl,
            obi = book.obi,
            memo = book.memo,
            createdAt = book.createdAt,
        )

        fun from(bookInfo: BookInfo): BookEntity = BookEntity(
            title = bookInfo.title,
            titleTranscription = bookInfo.titleTranscription.orEmpty(),
            seriesTitle = bookInfo.seriesTitle,
            publisher = bookInfo.publisher,
            extent = bookInfo.extent,
            edition = bookInfo.edition.orEmpty(),
            volume = bookInfo.volume.orEmpty(),
            issued = bookInfo.issued.orEmpty(),
            isbn = bookInfo.isbn,
            description = bookInfo.description.orEmpty(),
            thumbnailUrl = bookInfo.thumbnailUrl.orEmpty(),
            ndlThumbnailUrl = bookInfo.ndlThumbnailUrl.orEmpty(),
            googleBookThumbnailUrl = bookInfo.googleBookThumbnailUrl.orEmpty(),
            obi = "",
            memo = "",
            createdAt = bookInfo.createdAt,
        )
    }
}
