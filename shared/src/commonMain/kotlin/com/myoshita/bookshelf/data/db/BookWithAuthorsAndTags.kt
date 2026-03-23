package com.myoshita.bookshelf.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.myoshita.bookshelf.model.Book

data class BookWithAuthorsAndTags(
    @Embedded
    val book: BookEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookAuthorRelations::class,
            parentColumn = "bookId",
            entityColumn = "authorId",
        ),
    )
    val authors: List<AuthorEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookTagRelations::class,
            parentColumn = "bookId",
            entityColumn = "tagId",
        ),
    )
    val tags: List<TagEntity>,
) {
    companion object {
        fun transform(bookWithAuthorsAndTags: BookWithAuthorsAndTags): Book = Book(
            id = bookWithAuthorsAndTags.book.id,
            title = bookWithAuthorsAndTags.book.title,
            titleTranscription = bookWithAuthorsAndTags.book.titleTranscription,
            seriesTitle = bookWithAuthorsAndTags.book.seriesTitle,
            authors = bookWithAuthorsAndTags.authors.map { AuthorEntity.transform(it) },
            publisher = bookWithAuthorsAndTags.book.publisher,
            extent = bookWithAuthorsAndTags.book.extent,
            edition = bookWithAuthorsAndTags.book.edition,
            volume = bookWithAuthorsAndTags.book.volume,
            issued = bookWithAuthorsAndTags.book.issued,
            isbn = bookWithAuthorsAndTags.book.isbn,
            description = bookWithAuthorsAndTags.book.description,
            thumbnailUrl = bookWithAuthorsAndTags.book.thumbnailUrl,
            ndlThumbnailUrl = bookWithAuthorsAndTags.book.ndlThumbnailUrl,
            googleBookThumbnailUrl = bookWithAuthorsAndTags.book.googleBookThumbnailUrl,
            obi = bookWithAuthorsAndTags.book.obi,
            memo = bookWithAuthorsAndTags.book.memo,
            tags = bookWithAuthorsAndTags.tags.map { it.transform() },
            createdAt = bookWithAuthorsAndTags.book.createdAt,
        )
    }
}
