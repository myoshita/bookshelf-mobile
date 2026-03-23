package com.myoshita.bookshelf.feature.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val bookDetailViewModeModule = module {
    viewModelOf(::BookDetailViewModel)
}

data class BookDetailUiState(
    val isLoading: Boolean = false,
    val book: Book? = null,
    val bookNotFoundError: Boolean = false,
    val bookDeletionConfirmUiState: BookDeletionConfirmUiState? = null,
    val navigateToTop: Boolean = false,
)

data class BookDeletionConfirmUiState(
    val confirmAction: () -> Unit,
    val cancelAction: () -> Unit,
)

class BookDetailViewModel(
    private val bookId: Int,
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            bookRepository.getBookFlow(bookId)
                .collect { book ->
                    _uiState.update { it.copy(book = book) }
                }
        }
    }

    fun onClickDelete() {
        _uiState.update { state ->
            state.copy(
                bookDeletionConfirmUiState = BookDeletionConfirmUiState(
                    confirmAction = {
                        viewModelScope.launch {
                            bookRepository.deleteBook(bookId)
                            _uiState.update { it.copy(navigateToTop = true) }
                        }
                    },
                    cancelAction = {
                        _uiState.update { it.copy(bookDeletionConfirmUiState = null) }
                    },
                )
            )
        }
    }
}