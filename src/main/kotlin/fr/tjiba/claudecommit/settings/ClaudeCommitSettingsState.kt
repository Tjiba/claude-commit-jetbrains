package fr.tjiba.claudecommit.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "ClaudeCommitSettings", storages = [Storage("claude-commit-settings.xml")])
class ClaudeCommitSettingsState : PersistentStateComponent<ClaudeCommitSettingsState.State> {
    data class ModelPreset(
        val id: String,
        val label: String,
    ) {
        override fun toString(): String = label
    }

    enum class GenerationMode {
        AUTO,
        LOCAL,
        API
    }

    data class State(
        var model: String = "claude-sonnet-4-6",
        var modelPresetId: String = "claude-sonnet-4-6",
        var cachedModelIds: MutableList<String> = mutableListOf(),
        var lastModelsSyncEpochMs: Long = 0,
        var generationMode: String = GenerationMode.AUTO.name,
        var useLocalClaude: Boolean = true,
        var localCommandTemplate: String = "claude -p \"{prompt}\"",
        var maxDiffChars: Int = 12000,
        var promptTemplate: String = DEFAULT_PROMPT_TEMPLATE,
    ) {
        fun getGenerationMode(): GenerationMode {
            return GenerationMode.entries.firstOrNull { it.name == generationMode }
                ?: if (useLocalClaude) GenerationMode.AUTO else GenerationMode.API
        }

        fun setGenerationMode(mode: GenerationMode) {
            generationMode = mode.name
            useLocalClaude = mode != GenerationMode.API
        }

        fun getEffectiveModel(): String {
            return modelPresetId.ifBlank { model.ifBlank { defaultModelId() } }
        }

        fun availableModels(): List<ModelPreset> {
            val dynamic = cachedModelIds.distinct().map { ModelPreset(it, it) }
            return (modelPresets() + dynamic).distinctBy { it.id }
        }

        fun updateCachedModels(ids: List<String>) {
            cachedModelIds = ids.filter { it.isNotBlank() }.distinct().toMutableList()
            lastModelsSyncEpochMs = System.currentTimeMillis()
            if (modelPresetId.isBlank()) {
                modelPresetId = defaultModelId()
            }
        }
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        const val DEFAULT_PROMPT_TEMPLATE = """
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
"""

        private val PRESET_MODELS = listOf(
            ModelPreset("claude-sonnet-4-6", "claude-sonnet-4-6"),
            ModelPreset("claude-opus-4-6", "claude-opus-4-6"),
            ModelPreset("claude-opus-4-5-20251101", "claude-opus-4-5-20251101"),
            ModelPreset("claude-haiku-4-5-20251001", "claude-haiku-4-5-20251001"),
            ModelPreset("claude-sonnet-4-5-20250929", "claude-sonnet-4-5-20250929"),
            ModelPreset("claude-opus-4-1-20250805", "claude-opus-4-1-20250805"),
            ModelPreset("claude-opus-4-20250514", "claude-opus-4-20250514"),
            ModelPreset("claude-sonnet-4-20250514", "claude-sonnet-4-20250514"),
        )

        fun modelPresets(): List<ModelPreset> = PRESET_MODELS

        fun defaultModelId(): String = PRESET_MODELS.first().id

        fun instance(): ClaudeCommitSettingsState = ApplicationManager.getApplication().getService(ClaudeCommitSettingsState::class.java)
    }
}


