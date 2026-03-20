package com.myoshita.bookshelf.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.myoshita.bookshelf.model.BookTag

@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
) {
    fun transform(): BookTag = BookTag.entries.first { it.name == name }

    companion object {
        fun from(tag: BookTag): TagEntity = TagEntity(
            name = tag.name,
        )
    }
}
