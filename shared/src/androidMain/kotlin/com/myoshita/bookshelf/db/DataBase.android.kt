package com.myoshita.bookshelf.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDataBaseModule(): Module  = module {
    single { DatabaseBuilderFactory(get()).create() }
}

actual class DatabaseBuilderFactory(private val context: Context) {
    actual fun create(): RoomDatabase.Builder<BookshelfDataBase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath("bookshelf.db")
        return Room.databaseBuilder<BookshelfDataBase>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}
