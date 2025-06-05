package io.horizontalsystems.stellartkit.sample

import android.app.Application
import io.horizontalsystems.stellarkit.Network
import io.horizontalsystems.stellarkit.StellarKit
import io.horizontalsystems.stellarkit.StellarWallet

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initKit()
    }

    private fun initKit() {
        val walletId = "wallet-${stellarWallet.javaClass.simpleName}"
//        val walletId = UUID.randomUUID().toString()

        val network = Network.MainNet
        kit = StellarKit.getInstance(
            stellarWallet,
            network,
            this,
            walletId
        )
    }

    companion object {
        val stellarWallet = StellarWallet.WatchOnly("GADCIJ2UKQRWG6WHHPFKKLX7BYAWL7HDL54RUZO7M7UIHNQZL63C2I4Z")

        lateinit var kit: StellarKit
    }
}
