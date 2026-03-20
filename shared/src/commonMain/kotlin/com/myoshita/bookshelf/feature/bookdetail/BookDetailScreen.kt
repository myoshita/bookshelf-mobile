package com.myoshita.bookshelf.feature.bookdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bookshelf.shared.generated.resources.Res
import bookshelf.shared.generated.resources.placeholder
import coil3.compose.AsyncImage
import com.myoshita.bookshelf.model.Author
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.model.BookTag
import com.myoshita.bookshelf.model.authorText
import com.myoshita.bookshelf.model.buildTitleText
import com.myoshita.bookshelf.model.createdDate
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class BookDetail(val bookId: Int)

@Composable
fun BookDetailScreen(
    bookId: Int,
    onNavigateUp: () -> Unit,
    onClickEdit: (Book) -> Unit,
    navigateToAuthorSearch: (Author) -> Unit,
    navigateToTagSearch: (BookTag) -> Unit,
) {
    val viewModel = koinViewModel<BookDetailViewModel> { parametersOf(bookId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateToTop) {
        if (uiState.navigateToTop) {
            onNavigateUp()
        }
    }

    BookDetailScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onClickDelete = viewModel::onClickDelete,
        onClickEdit = onClickEdit,
        onClickAuthor = navigateToAuthorSearch,
        onClickTag = navigateToTagSearch,
    )

    val bookDeletionConfirmUiState = uiState.bookDeletionConfirmUiState
    if (bookDeletionConfirmUiState != null) {
        BookDeletionConfirmDialog(
            onDismissRequest = bookDeletionConfirmUiState.cancelAction,
            onConfirmRequest = bookDeletionConfirmUiState.confirmAction,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailScreen(
    uiState: BookDetailUiState,
    onNavigateUp: () -> Unit,
    onClickDelete: () -> Unit,
    onClickEdit: (Book) -> Unit,
    onClickAuthor: (Author) -> Unit,
    onClickTag: (BookTag) -> Unit,
) {
    val book = uiState.book
    val clipboardManager = LocalClipboardManager.current
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
                },
                actions = {
                    IconButton(
                        onClick = onClickDelete,
                        enabled = book != null,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                        )
                    }
                    IconButton(
                        onClick = { book?.let { onClickEdit(book) } },
                        enabled = book != null,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        if (book != null) {
            val contents = listOf(
                "タイトル" to book.title,
                "タイトル (読み)" to book.titleTranscription,
                "エディション" to book.edition,
                "巻名" to book.volume,
                "著者" to book.authorText(),
                "シリーズタイトル" to book.seriesTitle,
                "出版社" to book.publisher,
                "説明" to book.description,
                "ページ数" to book.extent.toString(),
                "出版日" to book.issued,
                "帯情報" to book.obi,
                "備考" to book.memo,
                "ISBN" to book.isbn,
                "登録日" to book.createdDate(),
            ).filter { it.second.isNotEmpty() }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = contentPadding,
            ) {
                item {
                    BookDetailHeader(
                        book = book,
                        onClickTitle = { clipboardManager.setText(AnnotatedString(it)) },
                        onClickAuthor = onClickAuthor,
                        onClickTag = onClickTag,
                    )
                }
                items(contents) { (title, description) ->
                    BookDetailListItem(
                        title = title,
                        description = description,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookDetailHeader(
    book: Book,
    onClickTitle: (String) -> Unit,
    onClickAuthor: (Author) -> Unit,
    onClickTag: (BookTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = book.thumbnailUrl,
                contentDescription = null,
                placeholder = painterResource(Res.drawable.placeholder),
                error = painterResource(Res.drawable.placeholder),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.width(150.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = book.buildTitleText(),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable { onClickTitle(book.buildTitleText()) }
                )
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    book.authors.forEach {
                        Text(
                            text = it.name,
                            textDecoration = TextDecoration.Underline,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.clickable { onClickAuthor(it) },
                        )
                    }
                }
                if (book.obi.isNotEmpty()) {
                    Text(
                        text = book.obi,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (book.memo.isNotEmpty()) {
                    Text(
                        text = book.memo,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (book.tags.isNotEmpty()) {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        book.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { onClickTag(tag) },
                                label = { Text(text = tag.title) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookDetailListItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = onClick?.let { TextDecoration.Underline },
            modifier = Modifier.clickable(
                enabled = onClick != null,
                onClick = onClick ?: {},
            )
        )
    }
}

@Composable
private fun BookDeletionConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmRequest) {
                Text("削除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("キャンセル")
            }
        },
        text = {
            Text(text = "本を削除しますか？")
        }
    )
}
