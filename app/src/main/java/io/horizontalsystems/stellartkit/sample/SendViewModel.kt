package io.horizontalsystems.stellartkit.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.stellarkit.StellarKit
import io.horizontalsystems.stellarkit.room.StellarAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.math.BigDecimal

class SendViewModel(private val stellarAsset: StellarAsset) : ViewModel() {

    private val kit = App.kit

    private var balance: BigDecimal? = null

    private var fee = kit.sendFee

    private var sendRecipient: String? = null
    private var sendAmount: BigDecimal? = null
    private var sendRecipientError: Throwable? = null

    private var sendInProgress = false
    private var sendResult = ""

    private var setRecipientJob: Job? = null

    private var assetCode = when (stellarAsset) {
        is StellarAsset.Asset -> stellarAsset.code
        StellarAsset.Native -> "XLM"
    }

    var uiState by mutableStateOf(
        SendUiState(
            balance = balance,
            fee = fee,
            sendInProgress = sendInProgress,
            sendResult = sendResult,
            sendRecipientError = sendRecipientError,
            sendEnabled = getSendEnabled(),
            assetCode = assetCode,
        )
    )
        private set

    init {
        viewModelScope.launch {
            kit.getBalanceFlow(stellarAsset).collect {
                balance = it?.balance

                emitState()
            }
        }
    }

    fun setAmount(amount: String) {
        sendAmount = amount.toBigDecimalOrNull()

        emitState()
    }

    fun setRecipient(recipient: String) {
        setRecipientJob?.cancel()
        setRecipientJob = viewModelScope.launch {
            delay(300)
            ensureActive()

            setRecipientInternal(recipient)
        }
    }

    private fun setRecipientInternal(recipient: String) {
        sendRecipientError = null
        sendRecipient = recipient

        try {
            StellarKit.validateAddress(recipient)
        } catch (e: Throwable) {
            sendRecipientError = e
        }

        emitState()
    }

    fun send() {
        viewModelScope.launch(Dispatchers.Default) {
            val sendRecipient1 = sendRecipient
            val sendAmount1 = sendAmount

            if (sendRecipient1 != null && sendAmount1 != null) {
                sendInProgress = true
                emitState()

                try {
                    when (stellarAsset) {
                        is StellarAsset.Asset -> {
                            kit.sendAsset(stellarAsset.id, sendRecipient1, sendAmount1, null)
                        }
                        is StellarAsset.Native -> {
                            kit.sendNative(sendRecipient1, sendAmount1, null)
                        }
                    }

                    sendResult = "Sent Success"
                } catch (e: Throwable) {
                    sendResult = "Sending Failed: $e"
                }

                sendInProgress = false
                emitState()
            }
        }
    }

    private fun emitState() {
        viewModelScope.launch {
            uiState = SendUiState(
                balance = balance,
                fee = fee,
                sendInProgress = sendInProgress,
                sendResult = sendResult,
                sendRecipientError = sendRecipientError,
                sendEnabled = getSendEnabled(),
                assetCode = assetCode
            )
        }
    }

    private fun getSendEnabled(): Boolean {
        if (sendRecipient == null) {
            return false
        }

        if (sendAmount == null) {
            return false
        }

        if (sendInProgress) {
            return false
        }

        if (sendRecipientError != null) {
            return false
        }

        return true
    }
}

data class SendUiState(
    val balance: BigDecimal?,
    val fee: BigDecimal,
    val sendInProgress: Boolean,
    val sendResult: String,
    val sendRecipientError: Throwable?,
    val sendEnabled: Boolean,
    val assetCode: String
)


