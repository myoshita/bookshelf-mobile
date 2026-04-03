package com.myoshita.bookshelf.feature.top

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoshita.bookshelf.data.repository.BookRepository
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.model.Sort
import com.myoshita.bookshelf.model.Suggestion
import com.myoshita.bookshelf.model.calculateSimilarity
import com.myoshita.bookshelf.model.sortedBy
import com.myoshita.bookshelf.model.toSuggestions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val topViewModelModule = module {
    viewModelOf(::TopViewModel)
}

data class TopUiState(
    val isLoading: Boolean = false,
    private val _books: List<Book> = emptyList(),
    val searchQuery: String = "",
    val sort: Sort = Sort.Default,
) {
    val books: List<Book>
        get() = _books.sortedBy(sort)

    val suggestions: List<Suggestion>
        get() = books
            .asSequence()
            .flatMap { it.toSuggestions() }
            .distinct()
            .mapNotNull { suggestion ->
                val similarity = suggestion.calculateSimilarity(searchQuery)
                if (similarity >= 0.6) {
                    suggestion to similarity
                } else {
                    null
                }
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(10)
            .toList()
}

class TopViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TopUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            bookRepository.books.collect { books ->
                _uiState.update { it.copy(_books = books) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onChangeSort(sort: Sort) {
        _uiState.update { it.copy(sort = sort) }
    }
}
