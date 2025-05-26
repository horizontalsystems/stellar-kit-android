# StellarKit Android

StellarKit is a lightweight and modular client for interacting with the [Stellar](https://www.stellar.org) blockchain, written in Kotlin for Android applications.

## Features

- Supports sending and receiving XLM and Stellar-based assets
- Handles account creation
- Support for mainnet/testnet networks
- Transaction building and signing
- Account balance and transaction history retrieval

## Requirements

- Android 8.0 (API level 26) or higher
- Kotlin 2.0.0+
- Java 8+

## Installation

Add the JitPack to module build.gradle
```
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add the following to your `build.gradle`:
```
dependencies {
    implementation 'com.github.horizontalsystems:stellar-kit-android:<version>'
}
```

## Usage

Create an instance of `StellarKit` using the static method `getInstance`. It requires the following parameters:

- `stellarWallet`: an instance of `StellarWallet`, which can be one of:
  - `WatchOnly(addressStr: String)` — watch-only wallet with account address
  - `Seed(seed: ByteArray)` — wallet created from a seed byte array
  - `SecretKey(secretSeed: String)` — wallet created from a secret seed string
- `network`: specify the network with the enum `Network` which supports:
  - `MainNet`
  - `TestNet`
- `context`: Android `Context` object
- `walletId`: a unique string identifier for the wallet instance, useful to distinguish multiple StellarKit instances for different accounts

Example

```kotlin
val stellarWallet = StellarWallet.SecretKey("your_secret_seed_here")
val network = Network.TestNet
val walletId = "unique_wallet_id"

val stellarKit = StellarKit.getInstance(stellarWallet, network, context, walletId)

// Send native asset
stellarKit.sendNative("recipient_account_id", BigDecimal("10.0"), "optional memo")

// Send custom asset
stellarKit.sendAsset("ASSET_CODE:ISSUER_ACCOUNT_ID", "recipient_account_id", BigDecimal("5.0"), null)

// Create account
stellarKit.createAccount("new_account_id", BigDecimal("1.0"), "Welcome!")

// Enable asset
stellarKit.enableAsset("ASSET_CODE:ISSUER_ACCOUNT_ID", "memo")

// Check if asset is enabled for an account
val enabled = stellarKit.isAssetEnabled(StellarAsset.Asset("ASSET_CODE", "ISSUER_ACCOUNT_ID"))

// Listen to balance updates
val balanceFlow = stellarKit.getBalanceFlow(StellarAsset.Native)
balanceFlow.collect { balance ->
    println("Balance updated: $balance")
}

// Refresh data manually
stellarKit.refresh()
