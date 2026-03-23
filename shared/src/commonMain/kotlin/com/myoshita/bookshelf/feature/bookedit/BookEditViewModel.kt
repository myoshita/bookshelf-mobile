package com.myoshita.bookshelf.feature.bookedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoshita.bookshelf.model.Author
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.data.repository.BookRepository
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val bookEditViewModelModule = module {
    viewModelOf(::BookEditViewModel)
}

data class BookEditUiState(
    val isLoading: Boolean = false,
    val book: Book? = null,
    val bookNotFoundError: Boolean = false,
    val navigateUp: Boolean = false,
    val editingBook: Book? = book,
    private val _authors: List<Author> = emptyList(),
    val thumbnailUploadError: ThumbnailUploadError? = null,
    val editUpdateError: EditUpdateError? = null,
) {
    val authors: List<Author>
        get() = _authors.filter { book?.authors?.contains(it)?.not() ?: true }
}

data class ThumbnailUploadError(val onDismiss: () -> Unit)
data class EditUpdateError(val onDismiss: () -> Unit)

class BookEditViewModel(
    private val bookId: Int,
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = BookEditUiState(isLoading = true)

            val book = bookRepository.getBook(bookId)

            if (book == null) {
                _uiState.value = BookEditUiState(isLoading = false, bookNotFoundError = true)
            } else {
                _uiState.value = BookEditUiState(isLoading = false, book = book)
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(_authors = bookRepository.getAuthors()) }
        }
    }

    fun onClickUpdate(book: Book) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                bookRepository.updateBook(book)
                _uiState.update { it.copy(navigateUp = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNavigateUp() {
        _uiState.update { it.copy(navigateUp = false) }
    }

    fun onBookValueUpdated(book: Book) {
        _uiState.update { it.copy(editingBook = book) }
    }

    fun onSelectThumbnailFile(file: PlatformFile) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            try {
//                val url = thumbnailRepository.uploadThumbnail(file)
//                _uiState.update {
//                    it.copy(
//                        editingBook = it.editingBook?.copy(thumbnailUrl = url),
//                    )
//                }
//            } catch (e: Exception) {
//                when (e) {
//                    is FirebaseFirestoreException -> {
//                        val onDismiss = { _uiState.update { it.copy(thumbnailUploadError = null) } }
//                        _uiState.update {
//                            it.copy(
//                                thumbnailUploadError = ThumbnailUploadError(onDismiss)
//                            )
//                        }
//                    }
//                    else -> {
//                        // nop
//                    }
//                }
//            } finally {
//                _uiState.update { it.copy(isLoading = false) }
//            }
//
//        }
    }
}