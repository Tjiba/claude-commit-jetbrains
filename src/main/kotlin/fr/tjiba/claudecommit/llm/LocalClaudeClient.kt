package fr.tjiba.claudecommit.llm

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import fr.tjiba.claudecommit.i18n.ClaudeCommitBundle
import java.time.Duration

class LocalClaudeClient {
    fun generateCommitMessage(template: String, prompt: String): Result<String> {
        return runCatching {
            val commandLine = buildCommandLine(template, prompt)
            val output = CapturingProcessHandler(commandLine).runProcess(Duration.ofSeconds(45).toMillis().toInt())

            if (output.exitCode != 0) {
                val stderr = output.stderr.takeIf { it.isNotBlank() } ?: ClaudeCommitBundle.message("error.local.execFailed")
                val errorMsg = when {
                    stderr.contains("no tokens available", ignoreCase = true) ||
                    stderr.contains("insufficient quota", ignoreCase = true) ||
                    stderr.contains("credit limit", ignoreCase = true) ||
                    stderr.contains("usage_error", ignoreCase = true) ||
                    stderr.contains("no stdin data received", ignoreCase = true) -> {
                        ClaudeCommitBundle.message("error.anthropic.insufficientQuota")
                    }
                    else -> stderr
                }
                error(errorMsg)
            }

            val message = output.stdout.trim()

            // Check if output contains token/quota errors (in stdout or stderr)
            if (message.contains("no stdin data received", ignoreCase = true) ||
                message.contains("no tokens available", ignoreCase = true) ||
                message.contains("insufficient quota", ignoreCase = true)) {
                error(ClaudeCommitBundle.message("error.anthropic.insufficientQuota"))
            }

            if (message.isBlank()) {
                val stderr = output.stderr.takeIf { it.isNotBlank() } ?: ""
                val errorMsg = when {
                    stderr.contains("no tokens available", ignoreCase = true) ||
                    stderr.contains("insufficient quota", ignoreCase = true) ||
                    stderr.contains("credit limit", ignoreCase = true) ||
                    stderr.contains("usage_error", ignoreCase = true) ||
                    stderr.contains("no stdin data received", ignoreCase = true) -> {
                        ClaudeCommitBundle.message("error.anthropic.insufficientQuota")
                    }
                    else -> ClaudeCommitBundle.message("error.local.emptyOutput")
                }
                error(errorMsg)
            }
            message
        }
    }

    internal fun buildCommandLine(template: String, prompt: String): GeneralCommandLine {
        val tokens = tokenizeTemplate(template)
        require(tokens.isNotEmpty()) { ClaudeCommitBundle.message("error.localCommand.empty") }
        require(tokens.any { it.contains(PROMPT_PLACEHOLDER) }) {
            ClaudeCommitBundle.message("error.local.promptMissing")
        }

        val commandLine = GeneralCommandLine(tokens.first())
        tokens.drop(1).forEach { token ->
            commandLine.addParameter(token.replace(PROMPT_PLACEHOLDER, prompt))
        }
        return commandLine
    }

    internal fun tokenizeTemplate(template: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inSingleQuotes = false
        var inDoubleQuotes = false

        fun flush() {
            if (current.isNotEmpty()) {
                result += current.toString()
                current.setLength(0)
            }
        }

        template.trim().forEach { ch ->
            when (ch) {
                ' ' , '\t' -> {
                    if (inSingleQuotes || inDoubleQuotes) {
                        current.append(ch)
                    } else {
                        flush()
                    }
                }
                '\'' -> {
                    if (inDoubleQuotes) {
                        current.append(ch)
                    } else {
                        inSingleQuotes = !inSingleQuotes
                    }
                }
                '"' -> {
                    if (inSingleQuotes) {
                        current.append(ch)
                    } else {
                        inDoubleQuotes = !inDoubleQuotes
                    }
                }
                else -> current.append(ch)
            }
        }

        flush()
        return result
    }

    private companion object {
        const val PROMPT_PLACEHOLDER = "{prompt}"
    }
}


