package fr.tjiba.claudecommit.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.panel
import fr.tjiba.claudecommit.i18n.ClaudeCommitBundle
import fr.tjiba.claudecommit.llm.AnthropicModelsClient
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPasswordField
import javax.swing.JScrollPane

class ClaudeCommitConfigurable : BoundConfigurable("Claude Commit") {
    private companion object {
        val REFRESH_BUTTON_TEXT = ClaudeCommitBundle.message("settings.refresh.button")
        val REFRESHING_BUTTON_TEXT = ClaudeCommitBundle.message("settings.refresh.inProgress")
    }

    private val state = ClaudeCommitSettingsState.instance().state
    private val modeValues = ClaudeCommitSettingsState.GenerationMode.entries.toTypedArray()
    private var availableModels = state.availableModels()

    private val modelPresetField = JComboBox(availableModels.toTypedArray())
    private val refreshModelsButton = JButton(REFRESH_BUTTON_TEXT)
    private val generationModeField = JComboBox(modeValues)
    private val localCommandField = JBTextField(state.localCommandTemplate)
    private val maxDiffField = JBTextField(state.maxDiffChars.toString())
    private val promptTemplateArea = JBTextArea(state.promptTemplate, 10, 80)
    private val apiKeyField = JPasswordField(ClaudeCommitSecrets.getApiKey().orEmpty())

    init {
        generationModeField.selectedItem = state.getGenerationMode()
        modelPresetField.selectedItem = availableModels.firstOrNull { it.id == state.modelPresetId }
            ?: availableModels.firstOrNull { it.id == state.model }
            ?: availableModels.firstOrNull()
        promptTemplateArea.lineWrap = true
        promptTemplateArea.wrapStyleWord = true
        modelPresetField.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean,
            ) = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).also {
                val preset = value as? ClaudeCommitSettingsState.ModelPreset ?: return@also
                text = if (preset.id == state.getEffectiveModel()) {
                    ClaudeCommitBundle.message("settings.model.currentSuffix", preset.label)
                } else {
                    preset.label
                }
            }
        }
        refreshModelsButton.addActionListener { refreshModelsFromApi() }
    }

    override fun createPanel(): DialogPanel = panel {
        row(ClaudeCommitBundle.message("settings.apiKey.label")) {
            cell(apiKeyField)
                .comment(ClaudeCommitBundle.message("settings.apiKey.comment"))
        }
        row(ClaudeCommitBundle.message("settings.model.label")) {
            cell(modelPresetField)
                .comment(ClaudeCommitBundle.message("settings.model.comment"))
        }
        row {
            cell(refreshModelsButton)
                .comment(ClaudeCommitBundle.message("settings.refresh.comment"))
        }
        row(ClaudeCommitBundle.message("settings.generationMode.label")) {
            cell(generationModeField)
                .comment(ClaudeCommitBundle.message("settings.generationMode.comment"))
        }
        row(ClaudeCommitBundle.message("settings.localCommand.label")) {
            cell(localCommandField)
                .comment(ClaudeCommitBundle.message("settings.localCommand.comment"))
        }
        row(ClaudeCommitBundle.message("settings.maxDiff.label")) {
            cell(maxDiffField)
                .bindIntText(state::maxDiffChars)
        }
        row(ClaudeCommitBundle.message("settings.prompt.label")) {
            cell(JScrollPane(promptTemplateArea))
                .comment(ClaudeCommitBundle.message("settings.prompt.comment"))
        }
    }

    override fun apply() {
        val selectedPreset = modelPresetField.selectedItem as? ClaudeCommitSettingsState.ModelPreset
            ?: state.availableModels().firstOrNull()
            ?: ClaudeCommitSettingsState.modelPresets().first()
        state.modelPresetId = selectedPreset.id
        state.model = state.getEffectiveModel().ifEmpty { ClaudeCommitSettingsState.defaultModelId() }
        state.setGenerationMode(generationModeField.selectedItem as? ClaudeCommitSettingsState.GenerationMode ?: ClaudeCommitSettingsState.GenerationMode.AUTO)
        state.localCommandTemplate = localCommandField.text.trim().ifEmpty { "claude -p \"{prompt}\"" }
        state.maxDiffChars = maxDiffField.text.trim().toIntOrNull()?.coerceIn(1000, 100000) ?: 12000
        state.promptTemplate = promptTemplateArea.text.trim().ifEmpty { ClaudeCommitSettingsState.DEFAULT_PROMPT_TEMPLATE }
        ClaudeCommitSecrets.setApiKey(String(apiKeyField.password).trim())
        modelPresetField.repaint()
        super.apply()
    }

    override fun isModified(): Boolean {
        val selectedMode = generationModeField.selectedItem as? ClaudeCommitSettingsState.GenerationMode
            ?: ClaudeCommitSettingsState.GenerationMode.AUTO
        val selectedModelId = (modelPresetField.selectedItem as? ClaudeCommitSettingsState.ModelPreset)?.id.orEmpty()
        val maxDiff = maxDiffField.text.trim().toIntOrNull()?.coerceIn(1000, 100000) ?: 12000
        val promptValue = promptTemplateArea.text.trim().ifEmpty { ClaudeCommitSettingsState.DEFAULT_PROMPT_TEMPLATE }
        val apiKey = String(apiKeyField.password).trim()
        val storedApiKey = ClaudeCommitSecrets.getApiKey().orEmpty()

        return selectedMode != state.getGenerationMode() ||
            selectedModelId != state.modelPresetId ||
            localCommandField.text.trim() != state.localCommandTemplate ||
            maxDiff != state.maxDiffChars ||
            promptValue != state.promptTemplate ||
            apiKey != storedApiKey
    }

    override fun reset() {
        availableModels = state.availableModels()
        modelPresetField.removeAllItems()
        availableModels.forEach { modelPresetField.addItem(it) }
        modelPresetField.selectedItem = availableModels.firstOrNull { it.id == state.modelPresetId }
            ?: availableModels.firstOrNull { it.id == state.model }
            ?: availableModels.firstOrNull()

        generationModeField.selectedItem = state.getGenerationMode()
        localCommandField.text = state.localCommandTemplate
        maxDiffField.text = state.maxDiffChars.toString()
        promptTemplateArea.text = state.promptTemplate
        apiKeyField.text = ClaudeCommitSecrets.getApiKey().orEmpty()
        refreshModelsButton.text = REFRESH_BUTTON_TEXT
        refreshModelsButton.isEnabled = true
        modelPresetField.repaint()
        super.reset()
    }

    private fun refreshModelsFromApi() {
        val apiKey = String(apiKeyField.password).trim().ifEmpty { ClaudeCommitSecrets.getApiKey().orEmpty() }
        if (apiKey.isBlank()) {
            refreshModelsButton.text = ClaudeCommitBundle.message("settings.refresh.needApiKey")
            return
        }

        refreshModelsButton.isEnabled = false
        refreshModelsButton.text = REFRESHING_BUTTON_TEXT
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = AnthropicModelsClient(apiKey).fetchModelIds()
            ApplicationManager.getApplication().invokeLater {
                try {
                    if (result.isSuccess) {
                        val ids = result.getOrThrow()
                        state.updateCachedModels(ids)
                        availableModels = state.availableModels()
                        val currentId = (modelPresetField.selectedItem as? ClaudeCommitSettingsState.ModelPreset)?.id
                        modelPresetField.removeAllItems()
                        availableModels.forEach { modelPresetField.addItem(it) }
                        modelPresetField.selectedItem = availableModels.firstOrNull { it.id == currentId }
                            ?: availableModels.firstOrNull { it.id == state.modelPresetId }
                            ?: availableModels.firstOrNull()
                        modelPresetField.repaint()
                    }
                } finally {
                    refreshModelsButton.isEnabled = true
                    refreshModelsButton.text = REFRESH_BUTTON_TEXT
                }
            }
        }
    }
}


