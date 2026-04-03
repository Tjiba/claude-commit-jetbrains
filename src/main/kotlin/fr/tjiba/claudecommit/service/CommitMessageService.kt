package fr.tjiba.claudecommit.service

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import fr.tjiba.claudecommit.git.GitDiffCollector
import fr.tjiba.claudecommit.i18n.ClaudeCommitBundle
import fr.tjiba.claudecommit.llm.AnthropicClient
import fr.tjiba.claudecommit.llm.LocalClaudeClient
import fr.tjiba.claudecommit.llm.PromptBuilder
import fr.tjiba.claudecommit.settings.ClaudeCommitSecrets
import fr.tjiba.claudecommit.settings.ClaudeCommitSettingsState
import fr.tjiba.claudecommit.settings.ClaudeCommitSettingsState.GenerationMode

class CommitMessageService(private val project: Project) {
    fun generateFromStagedChanges(selectedPaths: List<String> = emptyList()): Result<String> {
        val settings = ClaudeCommitSettingsState.instance().state

        return GitDiffCollector.collectStagedDiff(project, selectedPaths).mapCatching { rawDiff ->
            val diff = rawDiff.take(settings.maxDiffChars)
            val prompt = PromptBuilder.build(settings.promptTemplate, diff)

            when (settings.getGenerationMode()) {
                GenerationMode.LOCAL -> generateByLocal(prompt, settings.localCommandTemplate).getOrElse { throw it }
                GenerationMode.API -> generateByApi(prompt).getOrElse { throw it }
                GenerationMode.AUTO -> {
                    val localFirst = isClaudePluginLikelyInstalled()
                    if (localFirst) {
                        generateByLocal(prompt, settings.localCommandTemplate).getOrElse {
                            generateByApi(prompt).getOrElse { apiError -> throw apiError }
                        }
                    } else {
                        generateByApi(prompt).getOrElse {
                            generateByLocal(prompt, settings.localCommandTemplate).getOrElse { localError -> throw localError }
                        }
                    }
                }
            }
        }
    }

    private fun generateByLocal(prompt: String, commandTemplate: String): Result<String> {
        if (commandTemplate.isBlank()) {
            return Result.failure(IllegalStateException(ClaudeCommitBundle.message("error.localCommand.empty")))
        }
        return LocalClaudeClient().generateCommitMessage(commandTemplate, prompt)
    }

    private fun generateByApi(prompt: String): Result<String> {
        val settings = ClaudeCommitSettingsState.instance().state
        val apiKey = ClaudeCommitSecrets.getApiKey() ?: return Result.failure(
            IllegalStateException(ClaudeCommitBundle.message("error.apiKey.missing"))
        )
        val selectedModel = settings.getEffectiveModel()
        val primary = AnthropicClient(apiKey = apiKey, model = selectedModel).generateCommitMessage(prompt)
        if (primary.isSuccess) return primary

        val fallbackModel = ClaudeCommitSettingsState.defaultModelId()
        if (fallbackModel == selectedModel) return primary
        return AnthropicClient(apiKey = apiKey, model = fallbackModel).generateCommitMessage(prompt)
    }

    private fun isClaudePluginLikelyInstalled(): Boolean {
        val knownIds = listOf("com.anthropic.claudecode", "com.claude.code")
        return knownIds.any { PluginManagerCore.isPluginInstalled(PluginId.getId(it)) }
    }
}


