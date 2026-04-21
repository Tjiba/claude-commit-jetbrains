package fr.tjiba.claudecommit.llm

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import fr.tjiba.claudecommit.i18n.ClaudeCommitBundle
import java.time.Duration

class LocalClaudeClient {
    fun generateCommitMessage(template: String, prompt: String, model: String = ""): Result<String> {
        return runCatching {
            val commandLine = buildCommandLine(template, prompt, model)
            val handler = CapturingProcessHandler(commandLine)
            @Suppress("UNNECESSARY_SAFE_CALL")
            handler.processInput?.close()
            val output = handler.runProcess(Duration.ofSeconds(45).toMillis().toInt())

            if (output.exitCode != 0) {
                val detail = listOfNotNull(
                    output.stderr.takeIf { it.isNotBlank() },
                    output.stdout.takeIf { it.isNotBlank() }
                ).firstOrNull()
                error(detail ?: ClaudeCommitBundle.message("error.local.execFailed"))
            }

            val message = output.stdout.trim()
            require(message.isNotBlank()) { ClaudeCommitBundle.message("error.local.emptyOutput") }
            message
        }
    }

    internal fun buildCommandLine(template: String, prompt: String, model: String = ""): GeneralCommandLine {
        val tokens = tokenizeTemplate(template)
        require(tokens.isNotEmpty()) { ClaudeCommitBundle.message("error.localCommand.empty") }
        require(tokens.any { it.contains(PROMPT_PLACEHOLDER) }) {
            ClaudeCommitBundle.message("error.local.promptMissing")
        }

        val commandLine = GeneralCommandLine(tokens.first())
        tokens.drop(1).forEach { token ->
            commandLine.addParameter(
                token.replace(PROMPT_PLACEHOLDER, prompt)
                     .replace(MODEL_PLACEHOLDER, model)
            )
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
        const val MODEL_PLACEHOLDER = "{model}"
    }
}


