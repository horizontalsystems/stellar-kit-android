package io.horizontalsystems.stellarkit.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        AssetBalance::class,
        Operation::class,
        OperationSyncState::class,
        Tag::class,
    ],
    // 2: Operation gained pathPayment + contractBalanceChanges. Destructive fallback is
    // fine — operations resync from Horizon (and the old versions never stored these ops'
    // payloads anyway).
    version = 2
)
@TypeConverters(
    ConverterBigDecimal::class,
    ConverterStellarAsset::class,
    ConverterStellarAssetAsset::class,
    ConverterListOfStrings::class,
    ConverterContractBalanceChanges::class,
)
abstract class KitDatabase : RoomDatabase() {
    abstract fun balanceDao(): BalanceDao
    abstract fun operationDao(): OperationDao

    companion object {
        fun getInstance(context: Context, name: String): KitDatabase {
            return Room.databaseBuilder(context, KitDatabase::class.java, name)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
