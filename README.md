# Claude Commit Message — JetBrains Plugin

Generate meaningful, conventional Git commit messages straight from your IDE — powered by [Claude](https://claude.ai).

<img width="458" height="237" alt="image" src="https://github.com/user-attachments/assets/9d06f45e-78fe-4220-aaef-be5fd2d14c74" />


---

## Features

- **One-click generation** from the commit dialog toolbar or the VCS menu
- **Conventional Commits** format by default (`type(scope): summary`)
- **Three generation modes** — no internet? No problem
  - `AUTO` — tries local Claude CLI first, falls back to the Anthropic API
  - `LOCAL` — runs any local command (e.g. `claude -p "{prompt}"`)
  - `API` — calls the Anthropic API directly with your key
- **Smart diff scoping** — uses staged changes, selected files, or the full working-tree diff
- **Customizable prompt** — edit the system prompt and use `{diff}` as a placeholder
- **Live model list** — refresh available Claude models directly from the settings panel
- **Secure key storage** — API key stored in the IDE's Password Safe (never in plain text)
- **i18n** — English and French supported

<img width="1065" height="777" alt="image" src="https://github.com/user-attachments/assets/e174bf3e-0ae4-463b-b7a9-aeeb71ac0927" />


---

## Requirements

| Requirement | Version |
|---|---|
| JetBrains IDE | IntelliJ IDEA 2024.2+ (and other JetBrains IDEs) |
| JDK | 17+ |

---

## Installation

Install from the JetBrains Marketplace *(coming soon)*, or build from source:

```bash
./gradlew runIde
```

---

## Configuration

Open **Settings → Tools → Claude Commit**.

| Setting | Description |
|---|---|
| **API Key** | Your Anthropic API key (stored securely in Password Safe) |
| **Model** | Select a Claude model from the list |
| **Refresh models** | Fetches the latest model list from the Anthropic API |
| **Generation mode** | `AUTO`, `LOCAL`, or `API` |
| **Local command** | Command template with `{prompt}` placeholder |
| **Max diff size** | Maximum number of characters sent to the model (1 000 – 100 000) |
| **Custom prompt** | System prompt template — use `{diff}` to inject the Git diff |

### Local command examples

```bash
# macOS / Linux
claude -p '{prompt}'

# Windows (cmd.exe)
claude -p "{prompt}"

# Any other CLI tool
my-llm-tool --prompt "{prompt}"
```

### AUTO mode behavior

```
Local Claude CLI available?
  ├── Yes → generate locally (fast, no API cost)
  └── No  → fall back to Anthropic API
```

---

## Default prompt template

```
You are an assistant that writes professional Git commit messages.

Constraints:
- Reply only with the commit message.
- Preferred conventional format: type(scope): summary
- One title line <= 72 characters.
- Optional: one blank line then 2-4 short bullets.
- Base your response strictly on the provided diff.
- Do not invent changes that are not present.

Git Diff:
{diff}
```

---

## Development

```bash
# Run tests
./gradlew test

# Launch a sandboxed IDE instance
./gradlew runIde
```

---

## License

MIT
