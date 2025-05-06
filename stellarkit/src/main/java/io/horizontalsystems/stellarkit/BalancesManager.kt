package io.horizontalsystems.stellarkit

import android.util.Log
import io.horizontalsystems.stellarkit.room.AssetBalance
import io.horizontalsystems.stellarkit.room.BalanceDao
import io.horizontalsystems.stellarkit.room.StellarAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.stellar.sdk.Server
import java.math.BigDecimal

class BalancesManager(
    private val server: Server,
    private val balanceDao: BalanceDao,
    private val accountId: String
) {
    private val _syncStateFlow = MutableStateFlow<SyncState>(SyncState.NotSynced(StellarKit.SyncError.NotStarted))
    val syncStateFlow = _syncStateFlow.asStateFlow()

    private val assetBalanceMapFlow = balanceDao.getAssetBalancesFlow().map {
        it.map { it.asset to it.balance }.toMap()
    }

    fun getBalanceFlow(asset: StellarAsset): Flow<BigDecimal?> {
        return assetBalanceMapFlow.map { it[asset] }.distinctUntilChanged()
    }

    fun sync() {
        Log.d("AAA", "Syncing balances...")

        if (_syncStateFlow.value is SyncState.Syncing) {
            Log.d("AAA","Syncing balances is in progress")
            return
        }

        _syncStateFlow.update {
            SyncState.Syncing
        }

        try {
            val accounts = server.accounts()
            val account = accounts.account(accountId)

            val assetBalances = mutableListOf<AssetBalance>()

            account.balances.forEach { balance ->
                val asset = if (balance.assetType == "native") {
                    StellarAsset.Native
                } else {
                    StellarAsset.Asset(balance.assetCode, balance.assetIssuer)
                }

                assetBalances.add(
                    AssetBalance(
                        asset = asset,
                        balance = balance.balance.toBigDecimal(),
                    )
                )
            }

            balanceDao.deleteAllAndInsertNew(assetBalances)

            _syncStateFlow.update {
                SyncState.Synced
            }

        } catch (e: Throwable) {
            Log.e("AAA", "error on BalancesManager::sync() $e")
            _syncStateFlow.update {
                SyncState.NotSynced(e)
            }
        }
    }
}
