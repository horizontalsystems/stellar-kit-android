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
    version = 1
)
@TypeConverters(
    ConverterBigDecimal::class,
    ConverterStellarAsset::class,
    ConverterStellarAssetAsset::class,
    ConverterListOfStrings::class,
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
