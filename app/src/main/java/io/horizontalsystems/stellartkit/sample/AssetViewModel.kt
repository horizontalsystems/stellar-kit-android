package io.horizontalsystems.stellartkit.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.stellarkit.room.AssetBalance
import io.horizontalsystems.stellarkit.room.StellarAsset
import kotlinx.coroutines.launch
import java.math.BigDecimal

class AssetViewModel(private val assetId: String) : ViewModel() {
    private val kit = App.kit

    private val asset = StellarAsset.fromId(assetId)

    private var balance: BigDecimal = BigDecimal.ZERO
    private var assetBalance: AssetBalance? = null

    var uiState by mutableStateOf(
        AssetUiState(
            balance = balance,
            assetBalance = assetBalance,
        )
    )
        private set

    init {
        viewModelScope.launch {
            kit.getBalanceFlow(asset).collect {
                assetBalance = it

                emitState()
            }
        }

    }

    private fun emitState() {
        viewModelScope.launch {
            uiState = AssetUiState(
                balance = balance,
                assetBalance = assetBalance
            )
        }
    }
}

data class AssetUiState(
    val balance: BigDecimal,
    val assetBalance: AssetBalance?
)
