package com.myoshita.bookshelf.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.myoshita.bookshelf.model.Author
import com.myoshita.bookshelf.model.AuthorInfo

@Entity(
    tableName = "authors",
    indices = [Index(value = ["name"], unique = true)]
)
data class AuthorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val transcription: String,
) {
    companion object {
        fun from(author: Author) = AuthorEntity(
            id = author.id,
            name = author.name,
            transcription = author.transcription,
        )

        fun from(authorInfo: AuthorInfo) = AuthorEntity(
            name = authorInfo.name,
            transcription = authorInfo.transcription,
        )

        fun transform(authorEntity: AuthorEntity) = Author(
            id = authorEntity.id,
            name = authorEntity.name,
            transcription = authorEntity.transcription,
        )
    }
}
