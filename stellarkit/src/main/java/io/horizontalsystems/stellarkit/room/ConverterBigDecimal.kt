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
