package com.myoshita.bookshelf.feature.manualbooksearch

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.myoshita.bookshelf.model.BookInfo
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object ManualBookSearch

@Composable
fun ManualBookSearchScreen(
    viewModel: ManualBookSearchViewModel = koinViewModel(),
    onNavigateUp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(uiState.navigateUp) {
        if (uiState.navigateUp) {
            onNavigateUp()
        }
    }

    LaunchedEffect(uiState.bookNotFoundError) {
        if (uiState.bookNotFoundError != null) {
            try {
                snackbarHostState.showSnackbar(
                    message = "書籍が見つかりませんでした",
                )
            } finally {
                uiState.bookNotFoundError?.onDismiss?.invoke()
            }
        }
    }

    ManualBookSearchScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onClickSearch = viewModel::onSearchFromIsbn,
        onClickRegister = viewModel::onClickRegister,
        onClickBookInfo = viewModel::onClickBookInfo,
        onIsbnValueChange = viewModel::onIsbnValueChange,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ManualBookSearchScreen(
    uiState: ManualBookSearchUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onClickSearch: () -> Unit,
    onClickRegister: () -> Unit,
    onClickBookInfo: (BookInfo) -> Unit,
    onIsbnValueChange: (String) -> Unit,
) {
    Scaffold(
        topBar = {
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
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.imePadding()
            )
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    stickyHeader {
                        OutlinedTextField(
                            value = uiState.isbn,
                            onValueChange = onIsbnValueChange,
                            maxLines = 1,
                            label = { Text("ISBN (978から始まる)") },
                            trailingIcon = {
                                IconButton(
                                    onClick = onClickSearch,
                                    enabled = uiState.isbn.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                    )
                                }
                            },
                            supportingText = { Text(text = "${uiState.isbn.length} (13 or 10桁)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search,
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = { onClickSearch() }
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                        )
                    }
                    items(uiState.bookInfos) { bookInfo ->
                        BookItem(
                            checked = uiState.checkedBookInfos.contains(bookInfo),
                            onCheckedChange = {
                                onClickBookInfo(bookInfo)
                            },
                            bookInfo = bookInfo,
                            modifier = Modifier.clickable {
                                onClickBookInfo(bookInfo)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
                    }
                }

                Button(
                    onClick = onClickRegister,
                    enabled = uiState.checkedBookInfos.isNotEmpty(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .imePadding()
                        .fillMaxWidth()
                ) {
                    Text(text = "登録")
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            )
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun BookItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    bookInfo: BookInfo,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            val error = rememberAsyncImagePainter(model = bookInfo.googleBookThumbnailUrl)
            AsyncImage(
                model = bookInfo.thumbnailUrl,
                contentDescription = null,
                error = error,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.width(60.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = buildTitleText(bookInfo),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = bookInfo.authors.joinToString(", ") { it.name },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun buildTitleText(bookInfo: BookInfo): String {
    return buildString {
        append(bookInfo.title)
        if (bookInfo.volume != null) {
            append(" ${bookInfo.volume}")
        }
        if (bookInfo.edition != null) {
            append("<${bookInfo.edition}>")
        }
        if (bookInfo.seriesTitle.isNotEmpty()) {
            append("(${bookInfo.seriesTitle})")
        }
    }
}
