package com.myoshita.bookshelf.feature.bulkbarcodescan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoshita.bookshelf.data.repository.BookRepository
import com.myoshita.bookshelf.exception.BookNotFoundException
import com.myoshita.bookshelf.model.BookInfo
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

val bulkViewModelModule = module {
    viewModelOf(::BulkBarcodeScanViewModel)
}

data class BulkBarcodeScanUiState(
    val isLoading: Boolean = false,
    val bookInfos: List<BookInfo> = emptyList(),
    val checkedBookInfos: List<BookInfo> = emptyList(),
    val navigateUp: Boolean = false,
    val bookNotFoundError: BookNotFoundError? = null,
    val isRegistering: Boolean = false,
)

data class BookNotFoundError(val onDismiss: () -> Unit)

class BulkBarcodeScanViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BulkBarcodeScanUiState())
    val uiState = _uiState.asStateFlow()

    fun onScanBarcode(isbn: String) {
        fetchBookInfo(isbn)
    }

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

    private var bookRegisterJob: Job? = null

    fun onClickRegister() {
        if (_uiState.value.checkedBookInfos.isEmpty() || bookRegisterJob?.isActive == true) return

        bookRegisterJob = viewModelScope.launch {
            try {
                val books = _uiState.value.checkedBookInfos
                _uiState.update { it.copy(isRegistering = true) }
                // 登録順に送るためのreverse
                books.reversed().forEach {
                    bookRepository.registerBook(it)
                }
                _uiState.update { it.copy(navigateUp = true, isRegistering = false) }
            } catch (e: Exception) {
            }
        }
    }

    private var fetchBookInfoJob: Job? = null

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
            delay(2.seconds) // 連続読み込みしないようにする
        }
    }
}
