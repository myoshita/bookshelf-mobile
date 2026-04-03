package com.myoshita.bookshelf.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.myoshita.bookshelf.feature.barcodescan.BarcodeScanScreen
import com.myoshita.bookshelf.feature.barcodescan.BarcodeScanUiState
import com.myoshita.bookshelf.theme.AppTheme

@Preview
@Composable
fun PreviewBarcodeScanScreen() {
    val uiState = BarcodeScanUiState()
    AppTheme {
        BarcodeScanScreen(
            uiState = uiState,
            onScanBarcode = {},
            onClickReset = {},
            onNavigateUp = {},
            onTextFieldValueChange = {},
        )
    }
}
