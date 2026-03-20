package com.myoshita.bookshelf.api

import org.koin.dsl.module

val apiModule = module {
    single { GoogleBooksApiManager() }
    single { NdlApiManager() }
}