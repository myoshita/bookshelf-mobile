package com.myoshita.bookshelf.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        BookEntity::class,
        AuthorEntity::class,
        TagEntity::class,
        BookAuthorRelations::class,
        BookTagRelations::class,
    ],
    version = 1
)
@ConstructedBy(BookshelfDataBaseConstructor::class)
abstract class BookshelfDataBase : RoomDatabase() {
    abstract fun getBookDao(): BookDao
}

@Suppress("KotlinNoActualForExpect")
expect object BookshelfDataBaseConstructor : RoomDatabaseConstructor<BookshelfDataBase> {
    override fun initialize(): BookshelfDataBase
}
