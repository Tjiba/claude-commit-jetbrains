<h1 align="center">Claude Commit Message — JetBrains Plugin</h1>

<p align="center">Generate meaningful, conventional Git commit messages straight from your IDE.</p>

<p align="center">
  <img width="458" height="237" alt="image" src="https://github.com/user-attachments/assets/9d06f45e-78fe-4220-aaef-be5fd2d14c74" />
</p>

---

<h2 align="center">Features</h2>

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

<p align="center">
  <img width="1065" height="777" alt="image" src="https://github.com/user-attachments/assets/e174bf3e-0ae4-463b-b7a9-aeeb71ac0927" />
</p>

---

<h2 align="center">Requirements</h2>

| Requirement | Version |
|---|---|
| JetBrains IDE | IntelliJ IDEA 2024.2+ (and other JetBrains IDEs) |
| JDK | 17+ |

---

<h2 align="center">Installation</h2>

<p align="center">Install from the JetBrains Marketplace <em>(coming soon)</em>, or build from source:</p>

```bash
./gradlew runIde
```

---

<h2 align="center">Configuration</h2>

<p align="center">Open <strong>Settings → Tools → Claude Commit</strong>.</p>

| Setting | Description |
|---|---|
| **API Key** | Your Anthropic API key (stored securely in Password Safe) |
| **Model** | Select a Claude model from the list |
| **Refresh models** | Fetches the latest model list from the Anthropic API |
| **Generation mode** | `AUTO`, `LOCAL`, or `API` |
| **Local command** | Command template with `{prompt}` placeholder |
| **Max diff size** | Maximum number of characters sent to the model (1 000 – 100 000) |
| **Custom prompt** | System prompt template — use `{diff}` to inject the Git diff |

<h3 align="center">Local command examples</h3>

```bash
# macOS / Linux
claude -p '{prompt}'

# Windows (cmd.exe)
claude -p "{prompt}"

# Any other CLI tool
my-llm-tool --prompt "{prompt}"
```

<h3 align="center">AUTO mode behavior</h3>

```
Local Claude CLI available?
  ├── Yes → generate locally (fast, no API cost)
  └── No  → fall back to Anthropic API
```

---

<h2 align="center">Default prompt template</h2>

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

<h2 align="center">Development</h2>

```bash
# Run tests
./gradlew test

# Launch a sandboxed IDE instance
./gradlew runIde
```

---

<h2 align="center">License</h2>

<p align="center">MIT</p>
