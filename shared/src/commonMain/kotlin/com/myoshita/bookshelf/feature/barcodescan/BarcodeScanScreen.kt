package com.myoshita.bookshelf.feature.barcodescan

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.myoshita.bookshelf.util.isIsbn
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.publicvalue.multiplatform.qrcode.CameraPosition
import org.publicvalue.multiplatform.qrcode.CodeType
import org.publicvalue.multiplatform.qrcode.ScannerWithPermissions

@Serializable
object BarcodeScan

@Composable
fun BarcodeScanScreen(
    viewModel: BarcodeScanViewModel = koinViewModel(),
    onNavigateUp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    BarcodeScanScreen(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onScanBarcode = viewModel::onScanBarcode,
        onClickReset = viewModel::onClickReset,
        onTextFieldValueChange = viewModel::onIsbnFieldChange,
    )
}

@VisibleForTesting
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanScreen(
    uiState: BarcodeScanUiState,
    onNavigateUp: () -> Unit,
    onScanBarcode: (isbn: String) -> Unit,
    onClickReset: () -> Unit,
    onTextFieldValueChange: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateUp()
                        },
                    ) {
                        Image(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                val book = uiState.book
                if (book == null) {
                    Box {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            ScannerWithPermissions(
                                onScanned = { data ->
                                    val code = data.split(",").firstOrNull { it.isIsbn() }
                                    if (code != null) {
                                        onScanBarcode(code)
                                        true
                                    } else {
                                        false
                                    }
                                },
                                types = listOf(CodeType.EAN13),
                                cameraPosition = CameraPosition.BACK,
                                enableTorch = false,
                                modifier = Modifier
                                    .height(150.dp)
                                    .clipToBounds(),
                            )
                            TextField(
                                value = uiState.inputIsbn,
                                onValueChange = onTextFieldValueChange,
                                label = { Text("ISBN") },
                                maxLines = 1,
                                isError = uiState.showIsbnError,
                            )
                        }

                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {},
                                    ),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    ) {
                        val error = rememberAsyncImagePainter(model = book.googleBookThumbnailUrl)
                        AsyncImage(
                            model = book.thumbnailUrl,
                            error = error,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.width(100.dp)
                                .heightIn(max = 200.dp),
                        )
                        Text(book.toString())
                        Button(onClick = onClickReset) {
                            Text("戻る")
                        }
                    }
                }
            }
        },
    )
}
