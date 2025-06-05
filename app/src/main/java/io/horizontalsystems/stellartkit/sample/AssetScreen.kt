package io.horizontalsystems.stellartkit.sample

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AssetScreen(assetId: String) {
    val viewModel = viewModel<AssetViewModel>(initializer = {
        AssetViewModel(assetId)
    })

    val uiState = viewModel.uiState

    uiState.assetBalance?.let { assetBalance ->
        SendScreen(assetBalance.asset)
    }
}
