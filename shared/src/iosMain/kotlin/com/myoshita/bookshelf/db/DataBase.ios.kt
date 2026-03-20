package com.myoshita.bookshelf.db

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun platformDataBaseModule(): Module  = module {
    single { DatabaseBuilderFactory().create() }
}

actual class DatabaseBuilderFactory {
    actual fun create(): RoomDatabase.Builder<BookshelfDataBase> {
        val dbFilePath = documentDirectory() + "/my_room.db"
        return Room.databaseBuilder<BookshelfDataBase>(
            name = dbFilePath,
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path)
    }
}
