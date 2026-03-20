package com.myoshita.bookshelf.feature.top

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import bookshelf.shared.generated.resources.Res
import bookshelf.shared.generated.resources.icon_book_2
import com.myoshita.bookshelf.feature.common.BookGridItem
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.model.Sort
import com.myoshita.bookshelf.model.SortKey
import com.myoshita.bookshelf.model.SortOrder
import com.myoshita.bookshelf.model.Suggestion
import com.myoshita.bookshelf.util.plus
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object Top

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopScreen(
    viewModel: TopViewModel = koinViewModel(),
    onClickBarcodeScan: () -> Unit,
    onClickManualRegistration: () -> Unit,
    onClickBook: (Book) -> Unit,
    navigateToWordSearchResult: (String) -> Unit,
    navigateToSuggestionSearchResult: (Suggestion) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisibleSortModal by remember { mutableStateOf(false) }
    var isVisibleSettingDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TopScreen(
            uiState = uiState,
            onClickBarcodeScan = onClickBarcodeScan,
            onClickManualRegistration = onClickManualRegistration,
            onClickBook = onClickBook,
            onSearch = navigateToWordSearchResult,
            onSearchQueryChange = viewModel::onQueryChange,
            onSearchSuggestion = navigateToSuggestionSearchResult,
            onClickSort = { isVisibleSortModal = true },
            onClickSetting = { isVisibleSettingDialog = true },
        )
        if (isVisibleSortModal) {
            var sort by remember { mutableStateOf(uiState.sort) }
            ModalBottomSheet(
                onDismissRequest = {
                    if (sort != uiState.sort) {
                        viewModel.onChangeSort(sort)
                    }
                    isVisibleSortModal = false
                },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SortKey.entries.forEachIndexed { index, key ->
                            SegmentedButton(
                                selected = sort.key == key,
                                onClick = { sort = sort.copy(key = key) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = SortKey.entries.size,
                                ),
                                label = { Text(key.title) },
                            )
                        }
                    }
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SortOrder.entries.forEachIndexed { index, order ->
                            SegmentedButton(
                                selected = sort.order == order,
                                onClick = { sort = sort.copy(order = order) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = SortOrder.entries.size,
                                ),
                                label = { Text(order.title) },
                            )
                        }
                    }
                }
            }
        }
        if (isVisibleSettingDialog) {
            Dialog(
                onDismissRequest = { isVisibleSettingDialog = false },
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column {
                        IconButton(onClick = { isVisibleSettingDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                            )
                        }
                        Column(modifier = Modifier.padding(8.dp)) {
                        }
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopScreen(
    uiState: TopUiState,
    onClickManualRegistration: () -> Unit,
    onClickBarcodeScan: () -> Unit,
    onClickBook: (Book) -> Unit,
    onSearch: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSuggestion: (Suggestion) -> Unit,
    onClickSort: () -> Unit,
    onClickSetting: () -> Unit,
) {
    Scaffold(
        topBar = {
            var expanded by remember { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() }
            val horizontalPadding by animateDpAsState(
                targetValue = if (expanded) 0.dp else 16.dp,
            )
            LaunchedEffect(expanded) {
                if (expanded) {
                    focusRequester.requestFocus()
                } else {
                    onSearchQueryChange("")
                }
            }
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearch = onSearch,
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text("検索") },
                        leadingIcon = {
                            if (expanded) {
                                IconButton(
                                    onClick = { expanded = false },
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { expanded = true },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            if (expanded) {
                                if (uiState.searchQuery.isNotEmpty())
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                        )
                                    }
                            } else {
                                IconButton(
                                    onClick = onClickSetting,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                ) {
                    uiState.suggestions.forEach { suggestion ->
                        ListItem(
                            headlineContent = { Text(suggestion.name) },
                            leadingContent = {
                                val vector = when (suggestion) {
                                    is Suggestion.Title -> vectorResource(Res.drawable.icon_book_2)
                                    is Suggestion.Author -> Icons.Default.Person
                                }
                                Icon(
                                    imageVector = vector,
                                    contentDescription = null,
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = SearchBarDefaults.colors().containerColor,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSearchSuggestion(suggestion) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        onClickManualRegistration()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                    )
                }
                FloatingActionButton(
                    onClick = {
                        onClickBarcodeScan()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                    )
                }
            }
        },
        content = { contentPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(SpanSize),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = contentPadding + PaddingValues(8.dp),
            ) {
                item(span = { GridItemSpan(SpanSize) }) {
                    TopHeader(
                        bookCount = uiState.books.size,
                        sort = uiState.sort,
                        onClickSort = onClickSort,
                    )
                }
                items(uiState.books) { book ->
                    BookGridItem(
                        book = book,
                        onClick = { onClickBook(book) },
                        modifier = Modifier.height(230.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun TopHeader(
    bookCount: Int,
    sort: Sort,
    onClickSort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "$bookCount 冊",
            style = MaterialTheme.typography.titleSmall,
            modifier = modifier.weight(1f)
        )
        AssistChip(
            onClick = onClickSort,
            label = { Text(sort.key.title) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                )
            }
        )
    }
}

private const val SpanSize = 3
