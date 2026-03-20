package com.myoshita.bookshelf.feature.searchresult

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.model.BookTag
import com.myoshita.bookshelf.feature.common.BookGridItem
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class SearchResult(
    val word: String? = null,
    val author: String? = null,
    val title: String? = null,
    val tag: BookTag? = null,
)

@Composable
fun SearchResultScreen(
    route: SearchResult,
    onNavigateUp: () -> Unit,
    navigateToBookDetail: (Book) -> Unit,
    viewModel: SearchResultViewModel = koinViewModel { parametersOf(route) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearChResultScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onClickBook = navigateToBookDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearChResultScreen(
    uiState: SearchResultUiState,
    onNavigateUp: () -> Unit,
    onClickBook: (Book) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.appBarTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        content = { contentPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(SpanSize),
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = contentPadding,
            ) {
                item(span = { GridItemSpan(SpanSize) }) {
                    TopHeader(
                        bookCount = uiState.books.size,
                    )
                }
                items(uiState.books) { book ->
                    BookGridItem(
                        book = book,
                        onClick = {
                            onClickBook(book)
                        },
                        modifier = Modifier.height(230.dp),
                    )
                }
            }
        }
    )
}

@Composable
private fun TopHeader(
    bookCount: Int,
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
        )
    }
}

private const val SpanSize = 3
