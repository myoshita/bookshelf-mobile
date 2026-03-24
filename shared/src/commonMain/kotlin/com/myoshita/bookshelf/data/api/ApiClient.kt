package com.myoshita.bookshelf.data.api

import io.ktor.client.HttpClient
import org.koin.dsl.module

val httpClientModule = module {
    single { httpClient() }
}

expect fun httpClient(): HttpClient
