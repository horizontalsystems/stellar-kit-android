package io.horizontalsystems.stellarkit.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {
    fun deleteAllAndInsertNew(balances: List<AssetBalance>) {
        delete()
        insertAll(balances)
    }

    @Query("DELETE FROM AssetBalance")
    fun delete()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(balances: List<AssetBalance>)

    @Query("SELECT * FROM AssetBalance")
    fun getAssetBalancesFlow(): Flow<List<AssetBalance>>
}
