package com.myoshita.bookshelf

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun httpClient(): HttpClient {
    return HttpClient(Darwin) {
        install(ContentNegotiation) {
            val json = Json {
                ignoreUnknownKeys = true
            }
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
        }
    }
}