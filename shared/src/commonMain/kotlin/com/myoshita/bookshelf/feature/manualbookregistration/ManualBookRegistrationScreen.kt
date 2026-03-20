package com.myoshita.bookshelf.feature.manualbookregistration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Serializable
object ManualBookRegistration

@Composable
fun ManualBookRegistrationScreen(
    viewModel: ManualBookRegistrationViewModel = koinViewModel(),
    onNavigateUp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Single,
    ) { file ->
        file?.let {
            viewModel.onSelectThumbnailFile(it)
        }
    }

    LaunchedEffect(uiState.navigateUp) {
        if (uiState.navigateUp) {
            onNavigateUp()
        }
    }

    ManualBookRegistrationScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onClickRegister = viewModel::onClickRegister,
        onBookValueUpdated = viewModel::onBookValueUpdated,
        onClickUploadThumbnail = {
            launcher.launch()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualBookRegistrationScreen(
    uiState: ManualBookRegistrationUiState,
    onNavigateUp: () -> Unit,
    onClickRegister: (Book) -> Unit,
    onBookValueUpdated: (Book) -> Unit,
    onClickUploadThumbnail: () -> Unit,
) {
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
                        TextButton(
                            onClick = {
                                onClickRegister(bookStateValue)
                            },
                            enabled = bookStateValue != Book.Empty
                        ) {
                            Text("登録")
                        }

                    }
                )
                if (uiState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    ) { contentPadding ->
        BookEditableList(
            bookStateValue = bookStateValue,
            onBookValueUpdated = onBookValueUpdated,
            onClickUploadThumbnail = onClickUploadThumbnail,
            contentPadding = contentPadding + PaddingValues(horizontal = 8.dp),
            authors = uiState.authors,
        )
    }
}
