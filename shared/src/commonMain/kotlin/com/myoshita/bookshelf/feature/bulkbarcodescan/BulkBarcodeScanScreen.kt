package com.myoshita.bookshelf.feature.bulkbarcodescan

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.myoshita.bookshelf.model.BookInfo
import com.myoshita.bookshelf.util.isIsbn
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.publicvalue.multiplatform.qrcode.CameraPosition
import org.publicvalue.multiplatform.qrcode.CodeType
import org.publicvalue.multiplatform.qrcode.ScannerWithPermissions

@Serializable
object BulkBarcodeScan

@Composable
fun BulkBarcodeScanScreen(
    viewModel: BulkBarcodeScanViewModel = koinViewModel(),
    onNavigateUp: () -> Unit,
    onClickManualSearch: () -> Unit,
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

    BulkBarcodeScanScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onScanBarcode = viewModel::onScanBarcode,
        onClickRegister = viewModel::onClickRegister,
        onClickBookInfo = viewModel::onClickBookInfo,
        onClickManualSearch = onClickManualSearch,
    )
}

@VisibleForTesting
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun BulkBarcodeScanScreen(
    uiState: BulkBarcodeScanUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onScanBarcode: (String) -> Unit,
    onClickRegister: () -> Unit,
    onClickBookInfo: (BookInfo) -> Unit,
    onClickManualSearch: () -> Unit,
) {
    Scaffold(
        topBar = {
            Box {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onNavigateUp()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                )
                if (uiState.isLoading || uiState.isRegistering) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    stickyHeader {
                        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                            ScannerWithPermissions(
                                onScanned = { data ->
                                    val code = data.split(",").firstOrNull { it.isIsbn() }
                                    if (code != null) {
                                        onScanBarcode(code)
                                    }
                                    uiState.isLoading
                                },
                                types = listOf(CodeType.EAN13),
                                cameraPosition = CameraPosition.BACK,
                                enableTorch = false,
                                modifier = Modifier
                                    .height(150.dp)
                                    .clipToBounds(),
                            )
                            TextButton(
                                onClick = onClickManualSearch,
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Text("ISBNを手動で入力する")
                            }
                        }
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
                            },
                        )
                    }
                }

                Button(
                    onClick = onClickRegister,
                    enabled = uiState.checkedBookInfos.isNotEmpty() && uiState.isRegistering.not(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                ) {
                    Text(text = "登録")
                }
            }
        },
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
                modifier = Modifier.width(60.dp),
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
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

private fun buildTitleText(bookInfo: BookInfo): String = buildString {
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
