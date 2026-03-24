package com.myoshita.bookshelf.data.api

interface BookApiManager<T> {
    suspend fun getBook(isbn: String): T
}