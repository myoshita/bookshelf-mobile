package com.myoshita.bookshelf.feature.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import bookshelf.shared.generated.resources.Res
import bookshelf.shared.generated.resources.placeholder
import coil3.compose.AsyncImage
import com.myoshita.bookshelf.model.Author
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.model.BookTag
import com.myoshita.bookshelf.util.plus
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditableList(
    bookStateValue: Book,
    onBookValueUpdated: (Book) -> Unit,
    onClickUploadThumbnail: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    authors: List<Author> = emptyList(),
) {
    var isModalVisible by remember { mutableStateOf(false) }
    var isThumbnailUrlEditMode by remember { mutableStateOf(false) }
    val (editingAuthor, onEditingAuthorUpdate) = remember { mutableStateOf<Author?>(null) }
    val (deletingAuthor, onDeletingAuthorUpdate) = remember { mutableStateOf<Author?>(null) }

    if (isModalVisible) {
        ModalBottomSheet(
            onDismissRequest = { isModalVisible = false },
        ) {
            ListItem(
                headlineContent = { Text("サムネイルをアップロード") },
                colors = ListItemDefaults.colors(
                    containerColor = BottomSheetDefaults.ContainerColor,
                ),
                modifier = Modifier.clickable {
                    onClickUploadThumbnail()
                    isModalVisible = false
                }
            )
            ListItem(
                headlineContent = { Text("URLを編集") },
                colors = ListItemDefaults.colors(
                    containerColor = BottomSheetDefaults.ContainerColor,
                ),
                modifier = Modifier.clickable {
                    isThumbnailUrlEditMode = true
                    isModalVisible = false
                }
            )
        }
    }

    if (isThumbnailUrlEditMode) {
        var thumbnailUrl by rememberSaveable { mutableStateOf(bookStateValue.thumbnailUrl) }
        val focusRequester = remember { FocusRequester() }
        AlertDialog(
            onDismissRequest = { isThumbnailUrlEditMode = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBookValueUpdated(bookStateValue.copy(thumbnailUrl = thumbnailUrl))
                        isThumbnailUrlEditMode = false
                    }
                ) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isThumbnailUrlEditMode = false }
                ) {
                    Text(text = "キャンセル")
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = thumbnailUrl,
                        onValueChange = { thumbnailUrl = it },
                        label = { Text("サムネイルURL") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                        ),
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                    LaunchedEffect(focusRequester) {
                        focusRequester.requestFocus()
                    }
                    if (bookStateValue.googleBookThumbnailUrl.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                thumbnailUrl = bookStateValue.googleBookThumbnailUrl
                            },
                        ) {
                            Text(text = "Google BooksのURLに変更")
                        }
                    }
                    if (bookStateValue.ndlThumbnailUrl.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                thumbnailUrl = bookStateValue.ndlThumbnailUrl
                            },
                        ) {
                            Text(text = "NDLのURLに変更")
                        }
                    }
                }
            }
        )
    }

    if (editingAuthor != null) {
        var newAuthor by remember { mutableStateOf(editingAuthor) }
        AuthorEditDialog(
            value = newAuthor,
            onValueChange = { newAuthor = it },
            onDismissRequest = { onEditingAuthorUpdate(null) },
            onConfirmRequest = {
                val newAuthors = if (editingAuthor.name.isEmpty()) {
                    // 新規追加
                    if (newAuthor.name.isEmpty()) {
                        bookStateValue.authors
                    } else {
                        bookStateValue.authors + newAuthor
                    }
                } else {
                    // 編集
                    if (newAuthor.name.isEmpty()) {
                        bookStateValue.authors.toMutableList().apply {
                            remove(editingAuthor)
                        }
                    } else {
                        bookStateValue.authors.map {
                            if (it == editingAuthor) newAuthor else it
                        }
                    }
                }

                Napier.d("newAuthors: $newAuthors, newAuthor: $newAuthor")
                onBookValueUpdated(bookStateValue.copy(authors = newAuthors))
                onEditingAuthorUpdate(null)
            },
            authors = authors,
        )
    }

    if (deletingAuthor != null) {
        AlertDialog(
            onDismissRequest = { onDeletingAuthorUpdate(null) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBookValueUpdated(
                            bookStateValue.copy(
                                authors = bookStateValue.authors.toMutableList().apply {
                                    remove(deletingAuthor)
                                }
                            )
                        )
                        onDeletingAuthorUpdate(null)
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDeletingAuthorUpdate(null) }
                ) {
                    Text("キャンセル")
                }
            },
            text = {
                Text("著者「${deletingAuthor.name}」を削除しますか？")
            }
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding + PaddingValues(horizontal = 8.dp),
        modifier = modifier.imePadding()
    ) {
        item {
            BookEditableListItem(
                label = "タイトル",
                value = bookStateValue.title,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(title = it)) },
            )
        }
        item {
            BookEditableListItem(
                label = "タイトル (読み)",
                value = bookStateValue.titleTranscription,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(titleTranscription = it)) },
            )
        }
        item {
            BookEditableListItem(
                label = "エディション",
                value = bookStateValue.edition,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(edition = it)) },
            )
        }
        item {
            BookEditableListItem(
                label = "巻名",
                value = bookStateValue.volume,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(volume = it)) },
            )
        }
        item {
            AuthorListItem(
                authors = bookStateValue.authors,
                onClickAuthor = onEditingAuthorUpdate,
                onClickAdd = { onEditingAuthorUpdate(Author(id = 0, name = "", transcription = "")) },
                onClickDelete = { author ->
                    onDeletingAuthorUpdate(author)
                },
            )
        }
        item {
            BookEditableListItem(
                label = "シリーズタイトル",
                value = bookStateValue.seriesTitle,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(seriesTitle = it)) },
            )
        }
        item {
            BookEditableListItem(
                label = "出版社",
                value = bookStateValue.publisher,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(publisher = it)) },
            )
        }
        item {
            TagListItem(
                tags = bookStateValue.tags,
                onClickTag = { tag ->
                    val mutableTags = bookStateValue.tags.toMutableList()
                    if (mutableTags.contains(tag)) {
                        mutableTags.remove(tag)
                    } else {
                        mutableTags.add(tag)
                    }
                    onBookValueUpdated(bookStateValue.copy(tags = mutableTags))
                },
            )
        }
        item {
            BookEditableListItem(
                label = "説明",
                value = bookStateValue.description,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(description = it)) },
            )
        }
        item {
            BookEditableListItem(
                label = "ページ数",
                value = bookStateValue.extent.toString(),
                onValueChange = { onBookValueUpdated(bookStateValue.copy(extent = it.toInt())) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
            )
        }
        item {
            BookEditableListItem(
                label = "出版日",
                value = bookStateValue.issued,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(issued = it)) },
            )
        }
        item {
            BookEditableListItem(
                label = "帯情報",
                value = bookStateValue.obi,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(obi = it)) },
            )
        }
        item {
            BookEditableListItem(
                label = "備考",
                value = bookStateValue.memo,
                onValueChange = { onBookValueUpdated(bookStateValue.copy(memo = it)) },
            )
        }
        item {
            ThumbnailListItem(
                thumbnailUrl = bookStateValue.thumbnailUrl,
                onClick = { isModalVisible = true },
            )
        }
        item {
            Spacer(
                Modifier
                    .height(32.dp)
                    .windowInsetsBottomHeight(WindowInsets.systemBars)
            )
        }
    }
}

@Composable
private fun BookEditableListItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        trailingIcon = trailingIcon,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuthorListItem(
    authors: List<Author>,
    onClickAuthor: (Author) -> Unit,
    onClickAdd: () -> Unit,
    onClickDelete: (Author) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "著者",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            authors.forEach { author ->
                InputChip(
                    selected = false,
                    onClick = { onClickAuthor(author) },
                    label = { Text(author.name) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.clickable { onClickDelete(author) }
                        )
                    }
                )
            }
            InputChip(
                selected = true,
                onClick = onClickAdd,
                label = { Text("追加") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagListItem(
    tags: List<BookTag>,
    onClickTag: (BookTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "タグ",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BookTag.entries.forEach { tag ->
                val selected = tags.contains(tag)
                val leadingIcon: @Composable (() -> Unit)? = if (selected) {
                    @Composable {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                        )
                    }
                } else {
                    null
                }
                FilterChip(
                    selected = selected,
                    onClick = { onClickTag(tag) },
                    label = { Text(tag.title) },
                    leadingIcon = leadingIcon,
                )
            }
        }
    }
}

@Composable
private fun ThumbnailListItem(
    thumbnailUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = "サムネイル",
            style = MaterialTheme.typography.bodySmall,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable(onClick = onClick),
        ) {
            AsyncImage(
                model = thumbnailUrl,
                placeholder = painterResource(Res.drawable.placeholder),
                error = painterResource(Res.drawable.placeholder),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .width(100.dp),
            )
            Text(
                text = "編集",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun AuthorEditDialog(
    value: Author,
    onValueChange: (Author) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    modifier: Modifier = Modifier,
    authors: List<Author> = emptyList(),
) {
    val focusRequester = remember { FocusRequester() }
    var isAuthorSelectMode by remember { mutableStateOf(false) }
    var selectedAuthor: Author? by remember { mutableStateOf(null) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmRequest) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "キャンセル")
            }
        },
        text = {
            if (isAuthorSelectMode) {
                LazyColumn(modifier = Modifier.selectableGroup()) {
                    items(authors) { author ->
                        ListItem(
                            headlineContent = { Text(author.name) },
                            leadingContent = {
                                RadioButton(
                                    selected = selectedAuthor == author,
                                    onClick = null
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = AlertDialogDefaults.containerColor,
                            ),
                            modifier = Modifier.selectable(
                                selected = selectedAuthor == author,
                                onClick = {
                                    onValueChange(author)
                                    selectedAuthor = author
                                }
                            )
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BookEditableListItem(
                        label = "著者",
                        value = value.name,
                        onValueChange = { onValueChange(value.copy(name = it)) },
                        modifier = modifier.focusRequester(focusRequester)
                    )
                    BookEditableListItem(
                        label = "著者 (読み)",
                        value = value.transcription,
                        onValueChange = { onValueChange(value.copy(transcription = it)) },
                    )
                    if (authors.isNotEmpty()) {
                        TextButton(onClick = { isAuthorSelectMode = true }) {
                            Text("登録済みの著者を選択")
                        }
                    }
                }
            }
        },
        modifier = modifier
    )

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}
