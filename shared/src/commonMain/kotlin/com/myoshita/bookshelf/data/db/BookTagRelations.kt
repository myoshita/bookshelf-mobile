package com.myoshita.bookshelf.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "book_tag_relations",
    primaryKeys = ["bookId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("tagId")],
)
data class BookTagRelations(
    val bookId: Int,
    val tagId: Int,
)
