package io.horizontalsystems.stellarkit.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity
data class AssetBalance(
    @PrimaryKey
    val asset: StellarAsset,
    val balance: BigDecimal,
)
