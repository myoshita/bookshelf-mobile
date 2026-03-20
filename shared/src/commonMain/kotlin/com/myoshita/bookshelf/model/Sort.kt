package com.myoshita.bookshelf.model

data class Sort(
    val key: SortKey,
    val order: SortOrder,
) {
    companion object {
        val Default = Sort(SortKey.CreatedAt, SortOrder.Desc)
    }
}

enum class SortOrder(val title: String) {
    Asc("昇順"),
    Desc("降順"),
}

enum class SortKey(val title: String) {
    CreatedAt("登録日"),
    Title("タイトル"),
}
