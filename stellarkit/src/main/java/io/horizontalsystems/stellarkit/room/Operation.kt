package io.horizontalsystems.stellarkit.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.horizontalsystems.stellarkit.room.Tag.Type
import org.stellar.sdk.MemoText
import org.stellar.sdk.responses.operations.ChangeTrustOperationResponse
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.InvokeHostFunctionOperationResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PathPaymentBaseOperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
data class Operation(
    @PrimaryKey
    val id: Long,
    val timestamp: Long,
    val pagingToken: String,
    val sourceAccount: String,
    val transactionHash: String,
    val transactionSuccessful: Boolean,
    val fee: BigDecimal?,
    val memo: String?,
    val type: String,
    @Embedded(prefix = "payment_")
    val payment: Payment?,
    @Embedded(prefix = "account_")
    val accountCreated: AccountCreated?,
    @Embedded(prefix = "trust_")
    val changeTrust: ChangeTrust?,
    @Embedded(prefix = "pathpayment_")
    val pathPayment: PathPayment? = null,
    // invoke_host_function only: the SAC balance movements Horizon derived from the
    // contract call's events. Empty for calls that touch no classic-wrapped asset.
    val contractBalanceChanges: List<ContractBalanceChange>? = null,
) {
    data class Payment(val amount: BigDecimal, val asset: StellarAsset, val from: String, val to: String)
    data class AccountCreated(val startingBalance: BigDecimal, val funder: String, val account: String)
    data class ChangeTrust(
        val trustor: String,
        val trustee: String,
        val asset: StellarAsset.Asset,
        val limit: BigDecimal,
        val liquidityPoolId: String?
    )

    // path_payment_strict_send / path_payment_strict_receive: spends sourceAsset, delivers
    // asset. from == to is a swap on the sender's own account (the Stellar DEX shape).
    data class PathPayment(
        val from: String,
        val to: String,
        val asset: StellarAsset,
        val amount: BigDecimal,
        val sourceAsset: StellarAsset,
        val sourceAmount: BigDecimal,
    )

    data class ContractBalanceChange(
        val type: String, // transfer | mint | burn | clawback
        val from: String?,
        val to: String?,
        val asset: StellarAsset,
        val amount: BigDecimal,
    )

    fun tags(accountId: String): List<Tag> {
        val tags = mutableListOf<Tag>()

        accountCreated?.let { accountCreated ->
            if (accountCreated.funder == accountId) {
                tags.add(Tag(id, Type.Outgoing, StellarAsset.Native.id, listOf(accountCreated.account)))
            }

            if (accountCreated.account == accountId) {
                tags.add(Tag(id, Type.Incoming, StellarAsset.Native.id, listOf(accountCreated.funder)))
            }
        }

        payment?.let { data ->
            if (data.from == accountId) {
                tags.add(Tag(id, Type.Outgoing, data.asset.id, listOf(data.to)))
            }

            if (data.to == accountId) {
                tags.add(Tag(id, Type.Incoming, data.asset.id, listOf(data.from)))
            }
        }

        changeTrust?.let { changeTrust ->
            if (changeTrust.trustee == accountId) {
                tags.add(Tag(id, Type.Outgoing, changeTrust.asset.id, listOf(changeTrust.trustor)))
            }

            if (changeTrust.trustor == accountId) {
                tags.add(Tag(id, Type.Incoming, changeTrust.asset.id, listOf(changeTrust.trustee)))
            }
        }

        pathPayment?.let { pathPayment ->
            if (pathPayment.from == accountId && pathPayment.to == accountId) {
                // A swap on the own account: visible under both assets' histories.
                tags.add(Tag(id, Type.Swap, pathPayment.sourceAsset.id, listOf()))
                tags.add(Tag(id, Type.Swap, pathPayment.asset.id, listOf()))
            } else {
                if (pathPayment.from == accountId) {
                    tags.add(Tag(id, Type.Outgoing, pathPayment.sourceAsset.id, listOf(pathPayment.to)))
                }
                if (pathPayment.to == accountId) {
                    tags.add(Tag(id, Type.Incoming, pathPayment.asset.id, listOf(pathPayment.from)))
                }
            }
        }

        contractBalanceChanges?.let { changes ->
            val outgoing = changes.filter { it.from == accountId }
            val incoming = changes.filter { it.to == accountId }

            if (outgoing.isNotEmpty() && incoming.isNotEmpty()) {
                // Sent one asset and received another within one contract call — a swap
                // (Soroswap / Aquarius route).
                (outgoing + incoming).map { it.asset.id }.distinct().forEach { assetId ->
                    tags.add(Tag(id, Type.Swap, assetId, listOf()))
                }
            } else {
                outgoing.forEach { change ->
                    tags.add(Tag(id, Type.Outgoing, change.asset.id, listOfNotNull(change.to)))
                }
                incoming.forEach { change ->
                    tags.add(Tag(id, Type.Incoming, change.asset.id, listOfNotNull(change.from)))
                }
            }
        }

        return tags
    }

    companion object {
        fun fromApi(operationResponse: OperationResponse): Operation {
            var payment: Payment? = null
            var accountCreated: AccountCreated? = null
            var changeTrust: ChangeTrust? = null
            var pathPayment: PathPayment? = null
            var contractBalanceChanges: List<ContractBalanceChange>? = null

            when (operationResponse) {
                is PaymentOperationResponse -> {
                    payment = Payment(
                        amount = operationResponse.amount.toBigDecimal(),
                        asset = StellarAsset.fromSdkModel(operationResponse.asset),
                        from = operationResponse.from,
                        to = operationResponse.to,
                    )
                }
                is PathPaymentBaseOperationResponse -> {
                    pathPayment = PathPayment(
                        from = operationResponse.from,
                        to = operationResponse.to,
                        asset = StellarAsset.fromSdkModel(operationResponse.asset),
                        amount = operationResponse.amount.toBigDecimal(),
                        sourceAsset = StellarAsset.fromSdkModel(operationResponse.sourceAsset),
                        sourceAmount = operationResponse.sourceAmount.toBigDecimal(),
                    )
                }
                is InvokeHostFunctionOperationResponse -> {
                    contractBalanceChanges = operationResponse.assetBalanceChanges
                        ?.mapNotNull { change ->
                            val asset = change.asset?.let { StellarAsset.fromSdkModel(it) }
                                ?: return@mapNotNull null
                            ContractBalanceChange(
                                type = change.type,
                                from = change.from,
                                to = change.to,
                                asset = asset,
                                amount = change.amount.toBigDecimal(),
                            )
                        }
                        ?.takeIf { it.isNotEmpty() }
                }
                is CreateAccountOperationResponse -> {
                    accountCreated = AccountCreated(
                        startingBalance = operationResponse.startingBalance.toBigDecimal(),
                        funder = operationResponse.funder,
                        account = operationResponse.account,
                    )
                }
                is ChangeTrustOperationResponse -> {
                    changeTrust = ChangeTrust(
                        trustor = operationResponse.trustor,
                        trustee = operationResponse.trustee,
                        asset = StellarAsset.Asset(operationResponse.assetCode, operationResponse.assetIssuer),
                        limit = operationResponse.limit.toBigDecimal(),
                        liquidityPoolId = operationResponse.liquidityPoolId,
                    )
                }
            }

            return Operation(
                id = operationResponse.id,
                timestamp = OffsetDateTime.parse(operationResponse.createdAt).toEpochSecond(),
                pagingToken = operationResponse.pagingToken,
                sourceAccount = operationResponse.sourceAccount,
                transactionHash = operationResponse.transactionHash,
                transactionSuccessful = operationResponse.transactionSuccessful,
                fee = operationResponse.transaction?.feeCharged?.let {
                    BigDecimal(it).movePointLeft(7).stripTrailingZeros()
                },
                memo = (operationResponse.transaction?.memo as? MemoText)?.text,
                type = operationResponse.type,
                payment = payment,
                accountCreated = accountCreated,
                changeTrust = changeTrust,
                pathPayment = pathPayment,
                contractBalanceChanges = contractBalanceChanges,
            )
        }
    }
}

data class OperationInfo(
    val operations: List<Operation>,
    val initial: Boolean,
)

@Entity
data class OperationSyncState(
    @PrimaryKey
    val id: String,
    val allSynced: Boolean,
) {
    constructor(allSynced: Boolean) : this("unique_id", allSynced)
}
