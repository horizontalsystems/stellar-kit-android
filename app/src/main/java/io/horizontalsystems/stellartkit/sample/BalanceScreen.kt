package io.horizontalsystems.stellartkit.sample

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.horizontalsystems.stellarkit.room.StellarAsset

@Composable
fun BalanceScreen(viewModel: MainViewModel, uiState: MainUiState, navController: NavHostController) {
    val address = viewModel.address

    SelectionContainer {
        Column {
            Text(text = "Address: $address")
            Text(text = "Balance: ${uiState.totalBalance}")
            Text(text = "Minimum Balance: ${uiState.minimumBalance}")
            Text(text = "Sync State: ${uiState.syncState.description}")
            Text(text = "Operations Sync State: ${uiState.operationsSyncState.description}")

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = viewModel::start) {
                    Text(text = "start")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = viewModel::stop) {
                    Text(text = "Stop")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("ASSETS")
            Spacer(modifier = Modifier.height(10.dp))

            Crossfade(uiState.assetBalanceMap.isNotEmpty(), label = "") { isNotEmpty ->
                if (isNotEmpty) {
                    LazyColumn {
                        items(uiState.assetBalanceMap.values.toList()) { assetBalance ->
                            val asset = assetBalance.asset
                            Card(
                                modifier = Modifier
                                    .padding(bottom = 12.dp)
                                    .clickable {
                                        navController.navigate(AssetPage(asset.id))
                                    }
                            ) {
                                Row(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
//                                GlideImage(
//                                    modifier = Modifier.size(50.dp),
//                                    model = asset.image,
//                                    contentDescription = "",
//                                )
//                                Spacer(Modifier.width(8.dp))
                                    Column {
                                        when (asset) {
                                            is StellarAsset.Asset -> Text(asset.code)
                                            StellarAsset.Native -> Text("XLM (Native)")
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(assetBalance.balance.toPlainString())
                                }
                            }
                        }
                    }
                } else {
                    Text("No assets")
                }
            }
        }
    }
}