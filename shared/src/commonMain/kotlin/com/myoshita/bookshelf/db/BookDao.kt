package com.myoshita.bookshelf.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Transaction
    @Query("SELECT * FROM books ORDER BY createdAt")
    fun getAll(): Flow<List<BookWithAuthorsAndTags>>

    @Query("SELECT * FROM authors")
    fun getAuthors(): Flow<List<AuthorEntity>>

    @Transaction
    suspend fun insert(bookWithAuthorsAndTags: BookWithAuthorsAndTags) {
        val bookId = insertBook(bookWithAuthorsAndTags.book)
        bookWithAuthorsAndTags.authors.forEach {
            val authorId = getOrCreateAuthor(it)
            upsertAuthorRelation(
                BookAuthorRelations(
                    bookId = bookId.toInt(),
                    authorId = authorId,
                )
            )
        }
        bookWithAuthorsAndTags.tags.forEach {
            val tagId = getOrCreateTag(it)
            upsertTagRelations(
                BookTagRelations(
                    bookId = bookId.toInt(),
                    tagId = tagId,
                )
            )
        }
    }

    private suspend fun getOrCreateAuthor(author: AuthorEntity): Int {
        val id = insertAuthor(author)
        if (id != -1L) return id.toInt()
        return getAuthorByName(author.name)?.id ?: throw IllegalStateException("Author should exist")
    }

    private suspend fun getOrCreateTag(tag: TagEntity): Int {
        val id = insertTag(tag)
        if (id != -1L) return id.toInt()
        return getTagByName(tag.name)?.id ?: throw IllegalStateException("Tag should exist")
    }

    @Insert
    suspend fun insertBook(bookEntity: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthor(authorEntity: AuthorEntity): Long

    @Query("SELECT * FROM authors WHERE name = :name LIMIT 1")
    suspend fun getAuthorByName(name: String): AuthorEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tagEntity: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Upsert
    suspend fun upsertAuthorRelation(relation: BookAuthorRelations)

    @Upsert
    suspend fun upsertTagRelations(relation: BookTagRelations)

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    fun getBookById(bookId: Int): Flow<BookWithAuthorsAndTags?>

    @Transaction
    suspend fun update(bookWithAuthorsAndTags: BookWithAuthorsAndTags) {
        updateBook(bookWithAuthorsAndTags.book)
        val bookId = bookWithAuthorsAndTags.book.id

        deleteAuthorRelation(bookId)
        bookWithAuthorsAndTags.authors.forEach {
            val authorId = getOrCreateAuthor(it)
            upsertAuthorRelation(
                BookAuthorRelations(
                    bookId = bookId,
                    authorId = authorId,
                )
            )
        }

        deleteTagRelation(bookId)
        bookWithAuthorsAndTags.tags.forEach {
            val tagId = getOrCreateTag(it)
            upsertTagRelations(
                BookTagRelations(
                    bookId = bookId,
                    tagId = tagId,
                )
            )
        }
    }

    @Update
    suspend fun updateBook(bookEntity: BookEntity): Int

    @Transaction
    suspend fun delete(bookId: Int) {
        deleteBook(bookId)
        deleteAuthorRelation(bookId)
        deleteTagRelation(bookId)
    }

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: Int)

    @Query("DELETE FROM book_author_relations where bookId = :bookId")
    suspend fun deleteAuthorRelation(bookId: Int)

    @Query("DELETE FROM book_tag_relations where bookId = :bookId")
    suspend fun deleteTagRelation(bookId: Int)
}
