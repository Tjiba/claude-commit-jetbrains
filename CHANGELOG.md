# Claude Commit Message - Changelog

## [0.1.8] - 2026-04-20

### 🐛 Bug Fixes
- **Fixed API Error Handling** 🎯
  - API errors (401, 402, 429) no longer trigger model fallback
  - Users now receive proper error notifications immediately
  - Added specific error handling for token/quota exhaustion
  - Clear user messages for invalid API credentials
  - Rate limiting and insufficient quota errors now properly displayed

### 🔧 Technical Improvements
- Enhanced error detection: distinguishes between API errors and model errors
- Added `isApiError()` method for proper error classification
- Improved error message passing through Result chain
- Fixed fallback logic to prevent masking critical API issues

### 📋 Error Messages
- **Insufficient Quota (402)**: "No tokens available on your account. Please check your Anthropic account quota and billing status at https://console.anthropic.com"
- **Invalid Credentials (401)**: "Invalid API key or account. Please check your Anthropic API credentials in the settings."
- **Rate Limited (429)**: "API rate limit exceeded. Too many requests. Please wait a moment and try again."

---

## [0.1.7] - 2026-04-20

### ✨ New Features
- **Updated Claude Models Support** 🤖
  - Added Claude Opus 4.7 (latest 2025 model)
  - Added Claude Sonnet 4.6 (latest balanced model)
  - Added Claude Haiku 4.6 (latest fast model)
  - Live model refresh from Anthropic API

- **Effort Level Control** ⚡
  - **MINIMAL**: Fastest, most deterministic (100 tokens, 0.0 temp)
  - **LOW**: Quick & focused (150 tokens, 0.1 temp)
  - **MEDIUM**: Balanced mode (220 tokens, 0.2 temp) - *default*
  - **HIGH**: Thorough & creative (350 tokens, 0.4 temp)
  - **MAXIMUM**: Most detailed & creative (500 tokens, 0.7 temp)

### 🎯 Improvements
- Better model preset naming for clarity (e.g., "claude opus 4.7" instead of "claude-opus-4-7-20250219")
- Enhanced effort level dropdown with descriptive tooltips
- Improved API error handling with fallback to default model
- More responsive model refresh UI feedback

### 🔧 Technical
- Upgraded model list with latest Anthropic releases (2025-2026)
- Added temperature-based creativity control based on effort level
- Improved token budget allocation per effort level
- Enhanced settings panel with better organization

### 📋 Supported Models
- **Claude 3.7 Opus** (claude-opus-4-7-20250219) - Most capable
- **Claude 3.6 Sonnet** (claude-sonnet-4-20250514) - Balanced
- **Claude 3.6 Haiku** (claude-haiku-4-6) - Fast
- Plus 6+ additional model variants from 2024-2025 releases

---

## [0.1.6] - 2026-04-10

### ✨ Features
- Initial public release
- Three generation modes: AUTO, LOCAL, API
- Custom prompt templates with {diff} placeholder
- Secure API key storage in IDE Password Safe
- English and French localization

### 🔧 Technical
- Kotlin + JetBrains IDE SDK integration
- Anthropic API v1 support
- Local Claude CLI support
- Gradle build system

---

## Installation

Install from JetBrains Marketplace or build from source:

```bash
./gradlew runIde      # Run in sandboxed IDE
./gradlew buildPlugin # Build distribution package
./gradlew test        # Run tests
```

## Settings

Open **Settings → Tools → Claude Commit** to configure:
- API Key (stored securely)
- Claude Model (with live refresh)
- Effort Level (MINIMAL → MAXIMUM)
- Generation Mode (AUTO, LOCAL, or API)
- Custom prompt template

## License

MIT License - See LICENSE file for details

