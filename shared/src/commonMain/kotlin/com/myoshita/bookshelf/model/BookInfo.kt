package com.myoshita.bookshelf.model

import kotlin.time.Clock

data class BookInfo(
    val title: String,
    val titleTranscription: String?,
    val seriesTitle: String,
    val authors: List<AuthorInfo>,
    val publisher: String,
    val extent: Int,
    val edition: String?,
    val volume: String?,
    val issued: String?,
    val isbn: String,
    val description: String?,
    val thumbnailUrl: String? = null,
    val ndlThumbnailUrl: String? = null,
    val googleBookThumbnailUrl: String? = null,
    val aboutUrl: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)
