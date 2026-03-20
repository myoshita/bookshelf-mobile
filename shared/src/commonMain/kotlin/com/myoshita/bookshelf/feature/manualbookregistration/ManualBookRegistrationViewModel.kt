package com.myoshita.bookshelf.feature.manualbookregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoshita.bookshelf.model.Author
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.repository.BookRepository
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val manualBookRegistrationViewModelModule = module {
    viewModelOf(::ManualBookRegistrationViewModel)
}

data class ManualBookRegistrationUiState(
    val isLoading: Boolean = false,
    val editingBook: Book = Book.Empty,
    val navigateUp: Boolean = false,
    private val _authors: List<Author> = emptyList(),
) {
    val authors: List<Author>
        get() = _authors.filter { editingBook.authors.contains(it).not() }
}

class ManualBookRegistrationViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManualBookRegistrationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(_authors = bookRepository.getAuthors()) }
        }
    }

    fun onBookValueUpdated(book: Book) {
        _uiState.update { it.copy(editingBook = book) }
    }

    fun onClickRegister(book: Book) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            bookRepository.registerBook(book)
            _uiState.update { it.copy(navigateUp = true, isLoading = false) }
        }
    }

    fun onSelectThumbnailFile(file: PlatformFile) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            val url = thumbnailRepository.uploadThumbnail(file)
//            _uiState.update {
//                it.copy(
//                    editingBook = it.editingBook.copy(thumbnailUrl = url),
//                    isLoading = false,
//                )
//            }
//        }
    }
}