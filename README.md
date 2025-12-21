# RamaPay - Ramestta Blockchain Wallet

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Open Source](https://img.shields.io/badge/Open%20Source-%E2%9D%A4-green.svg)](https://github.com/obidua/RamaPay-android)
[![Platform](https://img.shields.io/badge/Platform-Android-blue.svg)](https://play.google.com/store/apps/details?id=io.ramestta.wallet)
[![Ramestta](https://img.shields.io/badge/Ramestta-Ecosystem-purple.svg)](https://ramestta.com)

<p align="center">
  <img src="RamaLogo/Hotpot%20Design/Android%20Image/play_store_512.png" width="200" alt="RamaPay Logo"/>
</p>

**RamaPay** is an open-source, secure, and user-friendly mobile wallet for the **Ramestta blockchain**. Manage your RAMA tokens, interact with dApps, and explore the Ramestta ecosystem - all from your Android device.

---

## ✨ Features

### 🔐 Security First
- **HD Wallet** with BIP44 derivation path
- **Biometric Authentication** (Fingerprint & Face ID)
- **Encrypted Storage** for private keys
- **No Analytics** option for maximum privacy

### 💰 Token Management
- Send & receive **RAMA** and ERC20 tokens
- **Automatic token discovery**
- Real-time **price tickers**
- Transaction history with detailed breakdown

### 🌐 Web3 dApp Browser
- Built-in **Web3 browser** for dApps
- **WalletConnect** support
- Seamless DeFi interaction
- Sign transactions & messages securely

### 📱 User Experience
- **Beginner-friendly** interface
- **Dark mode** support
- **Multiple accounts** management
- **Bulk wallet creation** (1-50 accounts)
- QR code scanner

---

## 🌍 Supported Networks

| Network | Chain ID | Type | Explorer |
|---------|----------|------|----------|
| **Ramestta Mainnet** | 1370 | Production | [ramascan.com](https://ramascan.com) |
| **Ramestta Testnet (Pingaksha)** | 1377 | Testnet | [pingaksha.ramascan.com](https://pingaksha.ramascan.com) |
| Ethereum Mainnet | 1 | Production | [etherscan.io](https://etherscan.io) |
| Polygon | 137 | Production | [polygonscan.com](https://polygonscan.com) |
| BNB Smart Chain | 56 | Production | [bscscan.com](https://bscscan.com) |

*+ Many more EVM-compatible networks*

---

## 📲 Download

<a href="https://play.google.com/store/apps/details?id=io.ramestta.wallet">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="200" alt="Get it on Google Play"/>
</a>

Or build from source (see below).

---

## 🛠️ Build From Source

### Prerequisites

- [Android Studio](https://developer.android.com/studio/) (latest)
- JDK 17 (JetBrains JDK recommended)
- Git

### Quick Build

```bash
# Clone the repository
git clone https://github.com/obidua/RamaPay-android.git
cd RamaPay-android

# Build debug APK
./gradlew assembleNoAnalyticsDebug

# Install on device
adb install -r app/build/outputs/apk/noAnalytics/debug/RamaPay.apk
```

### Release Build

1. Configure signing in `~/.gradle/gradle.properties`:
```properties
RELEASE_STORE_FILE=/path/to/your/keystore.jks
RELEASE_STORE_PASSWORD=your_password
RELEASE_KEY_ALIAS=your_alias
RELEASE_KEY_PASSWORD=your_key_password
```

2. Build release:
```bash
./gradlew assembleAnalyticsRelease
# or for AAB (Play Store)
./gradlew bundleAnalyticsRelease
```

---

## 📁 Project Structure

```
RamaPay/
├── app/                    # Main Android application
│   ├── src/main/java/com/ramapay/app/
│   │   ├── entity/        # Data models
│   │   ├── repository/    # Data layer
│   │   ├── service/       # Background services
│   │   ├── ui/            # Activities & Fragments
│   │   ├── viewmodel/     # ViewModels
│   │   ├── web3/          # Web3 & dApp browser
│   │   └── widget/        # Custom views
│   └── src/main/res/      # Resources
├── lib/                    # Ethereum & token utilities
├── dmz/                    # TokenScript web handling
├── hardware_stub/          # Hardware wallet stub
└── util/                   # Utility modules
```

---

## 🔧 Configuration

### Key Configuration Files

| File | Purpose |
|------|---------|
| `app/build.gradle` | App version, package ID, dependencies |
| `app/src/main/java/.../C.java` | App constants & URLs |
| `app/src/main/java/.../MediaLinks.java` | Social media links |
| `lib/src/main/java/.../EthereumNetworkBase.java` | Network definitions |
| `app/src/main/res/values/strings.xml` | App strings |

### Adding Custom Networks

Edit `lib/src/main/java/com/ramapay/ethereum/EthereumNetworkBase.java`:

```java
public static final NetworkInfo YOUR_NETWORK = new NetworkInfo(
    "Your Network",           // Name
    "YOUR",                   // Symbol
    "https://rpc.your.network",
    "https://explorer.your.network/tx/",
    YOUR_CHAIN_ID,
    false                     // isTestnet
);
```

---

## 🍴 Fork & Create Your Own Wallet

RamaPay is designed to be easily forked. Create your own branded wallet!

### Steps to Rebrand

1. **Fork this repository**

2. **Update branding:**
   - `app/build.gradle` → Change `applicationId`
   - `app/src/main/res/values/strings.xml` → App name
   - `app/src/main/res/mipmap-*/` → App icons
   - `app/src/main/res/raw/rama_loader.json` → Splash animation

3. **Update package names** (optional full rebrand):
   ```bash
   # Rename directories and update imports
   find . -name "*.java" -exec sed -i '' 's/com.ramapay/com.yourpackage/g' {} \;
   ```

4. **Build & publish!**

### Stay Updated

```bash
git remote add upstream https://github.com/obidua/RamaPay-android.git
git fetch upstream
git merge upstream/master
```

---

## 🤝 Contributing

We welcome contributions! Here's how:

1. **Fork** the repository
2. **Create** your feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'Add amazing feature'`
4. **Push** to the branch: `git push origin feature/amazing-feature`
5. **Open** a Pull Request

### Bug Reports

Please include:
- Device model & Android version
- Steps to reproduce
- Screenshots if applicable
- Logs from `adb logcat`

---

## 📞 Contact & Community

| Platform | Link |
|----------|------|
| 🌐 Website | [ramestta.com](https://ramestta.com) |
| 🐦 Twitter | [@AltRamestta](https://twitter.com/AltRamestta) |
| 💬 Telegram | [Ramestta Community](https://t.me/raboratory) |
| 📧 Email | support@ramestta.com |
| 💻 GitHub | [github.com/ramestta](https://github.com/ramestta) |

---

## 📄 License

RamaPay is released under the **MIT License**.

```
MIT License

Copyright (c) 2024-2025 Ramestta (RamaPay)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

See [LICENSE](LICENSE) for full details.

---

## 🙏 Acknowledgments

- The Ramestta community for their support
- All open-source contributors
- Ethereum ecosystem developers

---

<p align="center">
  <b>Built with ❤️ for the Ramestta Ecosystem</b>
  <br><br>
  <a href="https://ramestta.com">🌐 Website</a> •
  <a href="https://twitter.com/AltRamestta">🐦 Twitter</a> •
  <a href="https://t.me/raboratory">💬 Telegram</a>
</p>
