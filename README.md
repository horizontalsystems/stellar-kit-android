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


## Synchronization

The instance should be started to sync balances and operations. It also listens for updates. To do so you need to use the following methods:

```kotlin
// to start syncing process and to listen for updates
stellarKit.start()

// Observe syncing state
stellarKit.syncStateFlow.collect { syncState ->
    println("Sync State: $syncStateFlow")
}

// Observe syncing state of operations
stellarKit.operationsSyncStateFlow.collect { syncState ->
    println("Operations Sync State: $syncStateFlow")
}

// Refresh manually
stellarKit.refresh()

// You can stop the syncing process and updates listener by method stop
stellarKit.stop()
```

## Sending Payments

```kotlin
// Send native asset
stellarKit.sendNative("recipient_account_id", BigDecimal("10.0"), "optional memo")

// If the recipient account does not exist you can create it
stellarKit.createAccount("new_account_id", BigDecimal("1.0"), "Welcome!")

// Send custom asset
stellarKit.sendAsset("ASSET_CODE:ISSUER_ACCOUNT_ID", "recipient_account_id", BigDecimal("5.0"), "optional memo")
```

## Asset Management

```kotlin
// Enable a custom asset
stellarKit.enableAsset("ASSET_CODE:ISSUER_ACCOUNT_ID", "optional memo")

// Check if asset is enabled
val isEnabled = stellarKit.isAssetEnabled(StellarAsset.Asset("ASSET_CODE", "ISSUER_ACCOUNT_ID"))
```

## Observing Balances and Updates

```kotlin
// Observe balance changes for native asset
stellarKit.getBalanceFlow(StellarAsset.Native).collect { balance ->
    println("Native balance updated: $balance")
}
```

## License

The `StellarKit` is open source and available under the terms of the [MIT License](https://github.com/horizontalsystems/stellar-kit-android/blob/master/LICENSE)
