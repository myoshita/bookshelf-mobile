package com.myoshita.bookshelf.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module

val dataBaseModule = module {
    single { getRoomDatabase(get()) }
}

private fun getRoomDatabase(
    builder: RoomDatabase.Builder<BookshelfDataBase>,
): BookshelfDataBase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

expect fun platformDataBaseModule(): Module

expect class DatabaseBuilderFactory {
    fun create(): RoomDatabase.Builder<BookshelfDataBase>
}
