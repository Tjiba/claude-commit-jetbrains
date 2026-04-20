package fr.tjiba.claudecommit.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe

object ClaudeCommitSecrets {
    private const val SERVICE_NAME = "ClaudeCommitPlugin/AnthropicApiKey"
    private val attributes = CredentialAttributes(SERVICE_NAME)

    fun getApiKey(): String? = PasswordSafe.instance.get(attributes)?.getPasswordAsString()?.takeIf { it.isNotBlank() }

    fun setApiKey(apiKey: String?) {
        val creds = apiKey?.takeIf { it.isNotBlank() }?.let { Credentials(SERVICE_NAME, it) }
        PasswordSafe.instance.set(attributes, creds)
    }
}
