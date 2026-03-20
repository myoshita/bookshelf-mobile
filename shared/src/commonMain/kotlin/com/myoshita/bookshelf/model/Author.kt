package com.myoshita.bookshelf.model

import kotlinx.serialization.Serializable

@Serializable
data class Author(
    val id: Int,
    val name: String,
    val transcription: String,
)
