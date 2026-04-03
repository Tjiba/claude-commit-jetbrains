# Claude Commit Plugin for JetBrains IDEs

Generate thoughtful, contextual Git commit messages using Anthropic's Claude AI directly from your IDE.

## Features

- **One-Click Generation** – Generate commit messages from the Tools menu
- **Multiple Execution Modes** – AUTO (smart fallback), LOCAL (Claude CLI), or API (Anthropic)
- **Smart Configuration** – Dedicated settings panel at Tools > Claude Commit with live model refresh
- **Context-Aware Analysis** – Reads staged changes and selected diffs for intelligent message generation
- **Secure Credential Storage** – API keys stored safely in JetBrains Password Safe
- **Customizable Prompts** – Define your own commit message templates with {diff} placeholder support

## Requirements

- JDK 17 or higher
- IntelliJ IDEA 2024.1 or compatible JetBrains IDE
- Gradle (included via wrapper)

## Configuration

1. Navigate to Settings > Tools > Claude Commit
2. Select Generation Mode:
   - `AUTO` – Attempts local Claude CLI first, then falls back to Anthropic API
   - `LOCAL` – Uses only local Claude CLI installation
   - `API` – Uses Anthropic API exclusively (requires API key)
3. Select Claude Model from the available list
4. (Optional) Click Refresh Models to fetch the latest model list from Anthropic
5. (Optional) Customize the commit prompt template using the {diff} placeholder

### Local Command Configuration

**Windows:**
```
claude -p "{prompt}"
```

**macOS and Linux:**
```
claude -p '{prompt}'
```

## Development

### Building and Testing

```bash
gradle test
gradle runIde
```

**Windows Command Line:**
```
gradlew.bat test
gradlew.bat runIde
```

### Project Structure

- Use `gradlew` (Unix/macOS) or `gradlew.bat` (Windows) to execute Gradle tasks
- No separate Gradle installation required

## Technical Notes

- Plugin action placement varies based on IDE version and available action groups
- AUTO mode provides graceful fallback from local execution to API when necessary
- Commit message generation is based on actual Git diff content for relevance and accuracy

---

Author: [Tjiba](mailto:tjiba@tjiba.fr)  
Powered by Anthropic Claude AI
