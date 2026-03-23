package com.myoshita.bookshelf.feature.manualbooksearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoshita.bookshelf.exception.BookNotFoundException
import com.myoshita.bookshelf.feature.bulkbarcodescan.BookNotFoundError
import com.myoshita.bookshelf.model.BookInfo
import com.myoshita.bookshelf.data.repository.BookRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val manualBookSearchViewModelModule = module {
    viewModelOf(::ManualBookSearchViewModel)
}

data class ManualBookSearchUiState(
    val isLoading: Boolean = false,
    val bookInfos: List<BookInfo> = emptyList(),
    val checkedBookInfos: List<BookInfo> = emptyList(),
    val isbn: String = "",
    val navigateUp: Boolean = false,
    val bookNotFoundError: BookNotFoundError? = null,
)

class ManualBookSearchViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManualBookSearchUiState())
    val uiState = _uiState.asStateFlow()

    fun onIsbnValueChange(value: String) {
        _uiState.update { it.copy(isbn = value) }
    }

    fun onSearchFromIsbn() {
        fetchBookInfo(_uiState.value.isbn)
    }

    private var fetchBookInfoJob: Job? = null

    fun onClickBookInfo(bookInfo: BookInfo) {
        _uiState.update {
            val updated = if (it.checkedBookInfos.contains(bookInfo)) {
                it.checkedBookInfos - bookInfo
            } else {
                it.checkedBookInfos + bookInfo
            }
            it.copy(checkedBookInfos = updated)
        }
    }

    fun onClickRegister() {
        if (_uiState.value.checkedBookInfos.isEmpty()) return

        viewModelScope.launch {
            try {
                val books = _uiState.value.checkedBookInfos
                books.forEach {
                    bookRepository.registerBook(it)
                }
                _uiState.update { it.copy(navigateUp = true) }
            } catch (e: Exception) {
            }
        }
    }

    private fun fetchBookInfo(isbn: String) {
        if (fetchBookInfoJob?.isActive == true) {
            return
        }
        fetchBookInfoJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val book = bookRepository.searchFromIsbn(isbn)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        bookInfos = listOf(book) + it.bookInfos,
                        checkedBookInfos = listOf(book) + it.checkedBookInfos,
                        isbn = "",
                    )
                }
            } catch (e: Exception) {
                when (e) {
                    is BookNotFoundException -> {
                        val onDismiss = { _uiState.update { it.copy(bookNotFoundError = null) } }
                        _uiState.update { it.copy(bookNotFoundError = BookNotFoundError(onDismiss)) }
                    }
                    else -> {
                        Napier.e("Failed to fetch book info", e)
                    }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}