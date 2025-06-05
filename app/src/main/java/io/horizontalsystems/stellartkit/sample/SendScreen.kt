package io.horizontalsystems.stellartkit.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import io.horizontalsystems.stellarkit.room.StellarAsset

@Composable
fun SendScreen(stellarAsset: StellarAsset) {
    val viewModel = viewModel<SendViewModel>(initializer = {
        SendViewModel(stellarAsset)
    })

    val uiState = viewModel.uiState

    Column {
        var amountStr by remember { mutableStateOf("") }
        var recipientStr by remember { mutableStateOf("") }

        Text(text = "SEND ${uiState.assetCode}")

        HorizontalDivider()

        Text(text = "Fee: ${uiState.fee.toPlainString()}")

        Text(
            modifier = Modifier.clickable {
                uiState.balance?.let {
                    amountStr = it.toString()
                    viewModel.setAmount(amountStr)
                }
            },
            text = "Balance: ${uiState.balance}",
        )

        TextField(
            value = recipientStr,
            onValueChange = {
                recipientStr = it
                viewModel.setRecipient(it)
            },
            label = { Text("Recipient") },
            isError = uiState.sendRecipientError != null
        )

        TextField(
            value = amountStr,
            onValueChange = {
                amountStr = it
                viewModel.setAmount(it)
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
        )

        Button(
            onClick = {
                viewModel.send()
            },
            enabled = uiState.sendEnabled
        ) {
            val text = when {
                uiState.sendInProgress -> "Sending..."
                else -> "Send"
            }
            Text(text = text)
        }

        Text(text = "Send Result: ${uiState.sendResult}")

        uiState.sendRecipientError?.let {
            Text(text = "Recipient Error: ${it.message}", color = Color.Red)
        }
    }
}