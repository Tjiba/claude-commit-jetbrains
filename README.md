# Claude Commit Plugin (JetBrains)

IntelliJ plugin to generate Git commit messages with Claude.

## Features

- Direct `Generate Claude Commit Message` button in the commit message toolbar (depends on IDE version).
- Action available in VCS/commit menus.
- Dedicated page in `Settings > Tools > Claude Commit` with mode selection: `AUTO`, `LOCAL`, `API`.
- Reads changes from Git diff (staged first, then selected working-tree diff fallback).
- Two generation modes:
  - local command mode (for example: `claude -p "{prompt}"`)
  - Anthropic API mode with key stored in Password Safe

## Requirements

- JDK 17+
- IntelliJ IDEA 2024.2+
- Gradle (or wrapper)

## Configuration (IntelliJ and other JetBrains IDEs)

- Open `Settings > Tools > Claude Commit`.
- Choose `Generation mode`:
  - `AUTO`: try local Claude (Claude Code/CLI) then API.
  - `LOCAL`: force local command.
  - `API`: force Anthropic API.
- Choose a `Claude model` from the latest-model list.
- Use `Refresh models` to fetch the newest list from the Anthropic API.
- Edit `Custom commit prompt` with the `{diff}` placeholder.
- `Local command` examples:
  - Windows (`cmd.exe`): `claude -p "{prompt}"`
  - macOS/Linux: `claude -p '{prompt}'`

## Quick Development

```bash
gradle test
gradle runIde
```

On Windows `cmd.exe`:

```bat
gradle test
gradle runIde
```

## Notes

- The commit-toolbar button depends on available IntelliJ action groups for your IDE version.
- If local mode fails, the plugin falls back to Anthropic API when configured.

