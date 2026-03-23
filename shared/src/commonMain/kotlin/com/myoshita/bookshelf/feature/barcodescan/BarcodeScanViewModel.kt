package com.myoshita.bookshelf.feature.barcodescan

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoshita.bookshelf.model.BookInfo
import com.myoshita.bookshelf.data.repository.BookRepository
import com.myoshita.bookshelf.util.isIsbn
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val barcodeScanViewModelModule = module {
    viewModelOf(::BarcodeScanViewModel)
}

@Immutable
data class BarcodeScanUiState(
    val isLoading: Boolean = false,
    val book: BookInfo? = null,
    val inputIsbn: String = "",
) {
    val showIsbnError: Boolean
        get() {
            val isbn13Pattern = inputIsbn == "9" || inputIsbn == "97" || inputIsbn.startsWith("978")
            return inputIsbn.isNotEmpty() && isbn13Pattern.not()
        }
}

class BarcodeScanViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BarcodeScanUiState())
    val uiState = _uiState.asStateFlow()

    fun onScanBarcode(isbn: String) {
        fetchBookInfo(isbn)
    }

    fun onClickReset() {
        _uiState.update { it.copy(book = null, inputIsbn = "") }
    }

    fun onIsbnFieldChange(value: String) {
        if (value.isIsbn()) {
            fetchBookInfo(value)
        }
        _uiState.update { it.copy(inputIsbn = value) }
    }

    private fun fetchBookInfo(isbn: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val book = bookRepository.searchFromIsbn(isbn)
                _uiState.update { it.copy(isLoading = false, book = book, inputIsbn = "") }
            } catch (e: Exception) {
                Napier.e("Failed to fetch book info", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}