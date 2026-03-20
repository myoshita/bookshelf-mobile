package com.myoshita.bookshelf.feature.bookedit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myoshita.bookshelf.feature.common.BookEditableList
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.util.plus
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class BookEdit(val bookId: Int)

@Composable
fun BookEditScreen(
    bookId: Int,
    onNavigateUp: () -> Unit,
) {
    val viewModel = koinViewModel<BookEditViewModel> { parametersOf(bookId) }
    val uiState by viewModel.uiState.collectAsState()
    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Single,
    ) { file ->
        file?.let {
            viewModel.onSelectThumbnailFile(it)
        }
    }
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(uiState.navigateUp) {
        if (uiState.navigateUp) {
            onNavigateUp()
        }
    }

    LaunchedEffect(uiState.thumbnailUploadError) {
        if (uiState.thumbnailUploadError != null) {
            try {
                snackbarHostState.showSnackbar(message = "サムネイルのアップロードに失敗しました")
            } finally {
                uiState.thumbnailUploadError?.onDismiss?.invoke()
            }
        }
    }

    BookEditScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onClickUpdate = viewModel::onClickUpdate,
        onClickUploadThumbnail = {
            launcher.launch()
        },
        onBookValueUpdated = viewModel::onBookValueUpdated,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BookEditScreen(
    uiState: BookEditUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onClickUpdate: (Book) -> Unit,
    onClickUploadThumbnail: () -> Unit,
    onBookValueUpdated: (Book) -> Unit,
) {
    val book = uiState.book
    val bookStateValue = uiState.editingBook

    Scaffold(
        topBar = {
            Box {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onNavigateUp()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                    actions = {
                        if (bookStateValue != null) {
                            TextButton(
                                onClick = {
                                    onClickUpdate(bookStateValue)
                                },
                                enabled = book != bookStateValue
                            ) {
                                Text("更新")
                            }
                        }

                    }
                )
                if (uiState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                            .align(alignment = Alignment.BottomCenter)
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { contentPadding ->
        if (bookStateValue != null) {
            BookEditableList(
                bookStateValue = bookStateValue,
                onBookValueUpdated = onBookValueUpdated,
                onClickUploadThumbnail = onClickUploadThumbnail,
                contentPadding = contentPadding + PaddingValues(horizontal = 8.dp),
                authors = uiState.authors,
            )
        }
    }
}
