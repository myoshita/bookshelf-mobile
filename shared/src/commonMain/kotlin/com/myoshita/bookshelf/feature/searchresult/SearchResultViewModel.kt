package com.myoshita.bookshelf.feature.searchresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.model.similarity
import com.myoshita.bookshelf.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val searchResultViewModelModule = module {
    viewModelOf(::SearchResultViewModel)
}

data class SearchResultUiState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val appBarTitle: String = "",
)

class SearchResultViewModel(
    private val route: SearchResult,
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchResultUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            bookRepository.books.collect { books ->
                val filteredBooks = when {
                    route.author != null -> {
                        books.filter { book ->
                            book.authors.any { it.name == route.author }
                        }
                    }
                    route.tag != null -> {
                        books.filter { book ->
                            book.tags.any { it == route.tag }
                        }
                    }
                    route.word != null -> {
                        books.filter { book ->
                            book.similarity(route.word) >= 0.7
                        }
                    }
                    route.title != null -> {
                        books.filter { book ->
                            book.title == route.title
                        }
                    }
                    else -> throw IllegalArgumentException("Invalid route: $route")
                }
                _uiState.update {
                    it.copy(
                        books = filteredBooks,
                        appBarTitle = with(route) {
                            word ?: author ?: tag?.title ?: title ?: ""
                        }
                    )
                }
            }
        }
    }
}