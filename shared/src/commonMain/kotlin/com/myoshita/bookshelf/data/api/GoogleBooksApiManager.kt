package com.myoshita.bookshelf.data.api

import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

class GoogleBooksApiManager {
    private val client = httpClient()

    suspend fun getGoogleBook(isbn: String): GoogleBook {
        val url = "https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn&langRestrict=ja"
        try {
            val response = client.get(url)
            if (response.status == HttpStatusCode.OK) {
                Napier.d(response.bodyAsText())
                val book = response.body<GoogleBook>()
                return book
            } else {
                throw Exception("${response.status}")
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

@Serializable
data class GoogleBook(
    val items: List<Item>?,
) {
    @Serializable
    data class Item(
        val volumeInfo: VolumeInfo,
    )

    @Serializable
    data class VolumeInfo(
        val title: String,
        val authors: List<String>? = null,
        val subtitle: String? = null,
        val publishedDate: String,
        val description: String? = null,
        val industryIdentifiers: List<IndustryIdentifier>? = null,
        val pageCount: Int?,
        val imageLinks: ImageLinks? = null,
        val infoLink: String? = null,
        val language: String,
    ) {
        val isbn: String? = industryIdentifiers?.firstOrNull { it.type == "ISBN_13" }?.identifier

        val imageLink: String? = (imageLinks?.thumbnail ?: imageLinks?.smallThumbnail)
            ?.replaceFirst("http://", "https://")
    }

    @Serializable
    data class IndustryIdentifier(
        val type: String,
        val identifier: String
    )

    @Serializable
    data class ImageLinks(
        val smallThumbnail: String?,
        val thumbnail: String?,
    )
}
