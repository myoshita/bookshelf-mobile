package com.myoshita.bookshelf.data.repository

import com.myoshita.bookshelf.data.api.GoogleBooksApiManager
import com.myoshita.bookshelf.data.api.NdlApiManager
import com.myoshita.bookshelf.data.db.AuthorEntity
import com.myoshita.bookshelf.data.db.BookEntity
import com.myoshita.bookshelf.data.db.BookWithAuthorsAndTags
import com.myoshita.bookshelf.data.db.BookshelfDataBase
import com.myoshita.bookshelf.data.db.TagEntity
import com.myoshita.bookshelf.exception.BookNotFoundException
import com.myoshita.bookshelf.model.Author
import com.myoshita.bookshelf.model.AuthorInfo
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.model.BookInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import org.koin.dsl.module

val bookRepositoryModule = module {
    single<BookRepository> { BookRepositoryImpl(get(), get(), get()) }
}

interface BookRepository {
    val books: Flow<List<Book>>
    suspend fun searchFromIsbn(isbn: String): BookInfo
    suspend fun registerBook(book: BookInfo)
    suspend fun registerBook(book: Book)
    suspend fun getBook(bookId: Int): Book?
    suspend fun getBookFlow(bookId: Int): Flow<Book?>
    suspend fun deleteBook(bookId: Int)
    suspend fun updateBook(book: Book)
    suspend fun getAuthors(): List<Author>
}

internal class BookRepositoryImpl(
    private val googleBooksApiManager: GoogleBooksApiManager,
    private val ndlApiManager: NdlApiManager,
    private val db: BookshelfDataBase,
) : BookRepository {
    override val books: Flow<List<Book>>
        get() = db.getBookDao().getAll().map { bookWithAuthorsList ->
            bookWithAuthorsList.map {
                BookWithAuthorsAndTags.transform(it)
            }
        }

    override suspend fun searchFromIsbn(isbn: String): BookInfo = supervisorScope {
        val googleBookDeferred = async { googleBooksApiManager.getBook(isbn) }
        val ndlBookDeferred = async { ndlApiManager.getBook(isbn) }

        val googleBook = try {
            googleBookDeferred.await().items?.firstOrNull()
        } catch (e: Exception) {
            null
        }
        val ndlBibResource = try {
            ndlBookDeferred.await().records?.record?.recordData?.rdf?.bibResource?.firstOrNull()
        } catch (e: Exception) {
            null
        }

        if (googleBook == null && ndlBibResource == null) {
            throw BookNotFoundException("Book not found")
        }

        val isbn13 = ndlBibResource?.isbn ?: googleBook?.volumeInfo?.isbn ?: isbn
        BookInfo(
            title = ndlBibResource?.title?.description?.value
                ?: googleBook?.volumeInfo?.title.orEmpty(),
            titleTranscription = ndlBibResource?.title?.description?.transcription,
            seriesTitle = ndlBibResource?.seriesTitle?.description?.value.orEmpty(),
            authors = ndlBibResource?.creators?.map {
                AuthorInfo(
                    it.description.name,
                    it.description.transcription.orEmpty(),
                )
            }
                ?: googleBook?.volumeInfo?.authors?.map { AuthorInfo(it, "") } ?: emptyList(),
            publisher = ndlBibResource?.publisher?.agent?.name.orEmpty(),
            extent = ndlBibResource?.extent ?: googleBook?.volumeInfo?.pageCount ?: 0,
            edition = ndlBibResource?.edition ?: googleBook?.volumeInfo?.subtitle,
            volume = ndlBibResource?.volume?.description?.value,
            issued = ndlBibResource?.date ?: googleBook?.volumeInfo?.publishedDate,
            isbn = isbn13,
            description = googleBook?.volumeInfo?.description,
            thumbnailUrl = createNdlThumbnailUrl(isbn13),
            ndlThumbnailUrl = createNdlThumbnailUrl(isbn13),
            googleBookThumbnailUrl = googleBook?.volumeInfo?.imageLink,
            aboutUrl = ndlBibResource?.about ?: googleBook?.volumeInfo?.infoLink.orEmpty(),
        )
    }

    override suspend fun registerBook(book: BookInfo) {
        db.getBookDao()
            .insert(
                BookWithAuthorsAndTags(
                    book = BookEntity.from(book),
                    authors = book.authors.map { AuthorEntity.from(it) },
                    tags = emptyList(),
                ),
            )
    }

    override suspend fun registerBook(book: Book) {
        db.getBookDao()
            .insert(
                BookWithAuthorsAndTags(
                    book = BookEntity.from(book),
                    authors = book.authors.map { AuthorEntity.from(it) },
                    tags = emptyList(),
                ),
            )
    }

    override suspend fun getBook(bookId: Int): Book? = db.getBookDao()
        .getBookById(bookId)
        .map { it?.let { BookWithAuthorsAndTags.transform(it) } }
        .firstOrNull()

    override suspend fun getBookFlow(bookId: Int): Flow<Book?> = db.getBookDao()
        .getBookById(bookId)
        .map { it?.let { BookWithAuthorsAndTags.transform(it) } }

    override suspend fun deleteBook(bookId: Int) {
        db.getBookDao().delete(bookId)
    }

    override suspend fun updateBook(book: Book) {
        db.getBookDao().update(
            BookWithAuthorsAndTags(
                book = BookEntity.from(book),
                authors = book.authors.map { AuthorEntity.from(it) },
                tags = book.tags.map { TagEntity.from(it) },
            ),
        )
    }

    override suspend fun getAuthors(): List<Author> = db.getBookDao()
        .getAuthors()
        .map { authors ->
            authors.map { AuthorEntity.transform(it) }
        }.firstOrNull() ?: emptyList()

    private fun createNdlThumbnailUrl(isbn: String): String = "https://ndlsearch.ndl.go.jp/thumbnail/$isbn.jpg"
}
