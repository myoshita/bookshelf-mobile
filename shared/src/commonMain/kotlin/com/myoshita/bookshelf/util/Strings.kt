package com.myoshita.bookshelf.util

import kotlin.math.min

fun String.isIsbn(): Boolean {
    val isbn13 = """^978\d{10}$""".toRegex()
    return matches(isbn13)
}

fun String.removeSpaces(): String = this.replace("[\\s\u3000]+".toRegex(), "")
