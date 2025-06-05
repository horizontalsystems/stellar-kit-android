package io.horizontalsystems.stellartkit.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.horizontalsystems.stellarkit.room.StellarAsset
import java.util.Date

val StellarAsset.code: String
    get() = when (this) {
        is StellarAsset.Asset -> code
        StellarAsset.Native -> "XLM"
    }

@Composable
fun TransactionsScreen() {
    val viewModel = viewModel<TransactionsViewModel>()

    val uiState = viewModel.uiState
    val operations = uiState.operations

    operations?.let {
        val scrollState = rememberLazyListState()
        scrollState.OnBottomReached(2) {
            viewModel.onBottomReached()
        }

        LazyColumn(state = scrollState) {
            items(operations) { operation ->
                Card(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                        Column {
                            Text("Hash: ${operation.transactionHash}")
                            Text("Type: ${operation.type}")

                            operation.payment?.let { payment ->
                                Text("Amount: ${payment.amount.toPlainString()} ${payment.asset.code}")
                                Text("From: ${payment.from}")
                                Text("To: ${payment.to}")
                            }

                            operation.fee?.let {
                                Text("Fee: ${it.toPlainString()}")
                            }
                            operation.memo?.let {
                                Text("Memo: $it")
                            }
                            val dateStr = Date(operation.timestamp * 1000).toString()
                            Text("Date: $dateStr")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LazyListState.OnBottomReached(
    // tells how many items before we reach the bottom of the list
    // to call onLoadMore function
    buffer: Int = 0,
    onLoadMore: () -> Unit
) {
    // Buffer must be positive.
    // Or our list will never reach the bottom.
    require(buffer >= 0) { "buffer cannot be negative, but was $buffer" }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf true

            // subtract buffer from the total items
            lastVisibleItem.index >= layoutInfo.totalItemsCount - 1 - buffer
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .collect { if (it) onLoadMore() }
    }
}
