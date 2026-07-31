package io.horizontalsystems.stellarkit.room

import androidx.room.TypeConverter
import java.math.BigDecimal

interface Converter<T> {
    fun fromString(s: String?): T?
    fun toString(v: T?): String?
}

class ConverterBigDecimal : Converter<BigDecimal> {
    @TypeConverter
    override fun fromString(s: String?) = try {
        s?.let { BigDecimal(it) }
    } catch (e: Exception) {
        null
    }

    @TypeConverter
    override fun toString(v: BigDecimal?) = v?.toPlainString()
}

class ConverterListOfStrings : Converter<List<String>> {
    @TypeConverter
    override fun fromString(s: String?) = s?.split("|")

    @TypeConverter
    override fun toString(v: List<String>?) = v?.joinToString("|")
}

class ConverterStellarAsset : Converter<StellarAsset> {
    @TypeConverter
    override fun fromString(s: String?) = try {
        s?.let { StellarAsset.fromId(it) }
    } catch (e: Exception) {
        null
    }

    @TypeConverter
    override fun toString(v: StellarAsset?) = v?.id
}

class ConverterStellarAssetAsset : Converter<StellarAsset.Asset> {
    @TypeConverter
    override fun fromString(s: String?) = try {
        s?.let { StellarAsset.fromId(it) as? StellarAsset.Asset }
    } catch (e: Exception) {
        null
    }

    @TypeConverter
    override fun toString(v: StellarAsset.Asset?) = v?.id
}

// Serialized as `type/fromOrEmpty/toOrEmpty/assetId/amount` per change, `;`-joined.
// None of the fields can contain the separators: addresses and asset codes are
// alphanumeric (asset ids use `:`), amounts are plain decimals, type is a bare word.
class ConverterContractBalanceChanges : Converter<List<Operation.ContractBalanceChange>> {
    @TypeConverter
    override fun fromString(s: String?) = s
        ?.takeIf { it.isNotEmpty() }
        ?.split(";")
        ?.mapNotNull { entry ->
            val parts = entry.split("/")
            if (parts.size != 5) return@mapNotNull null

            try {
                Operation.ContractBalanceChange(
                    type = parts[0],
                    from = parts[1].takeIf { it.isNotEmpty() },
                    to = parts[2].takeIf { it.isNotEmpty() },
                    asset = StellarAsset.fromId(parts[3]),
                    amount = BigDecimal(parts[4]),
                )
            } catch (e: Exception) {
                null
            }
        }

    @TypeConverter
    override fun toString(v: List<Operation.ContractBalanceChange>?) = v?.joinToString(";") {
        listOf(it.type, it.from.orEmpty(), it.to.orEmpty(), it.asset.id, it.amount.toPlainString()).joinToString("/")
    }
}
