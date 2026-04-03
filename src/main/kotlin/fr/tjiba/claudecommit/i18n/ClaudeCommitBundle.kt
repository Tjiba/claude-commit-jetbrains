package fr.tjiba.claudecommit.i18n

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

private const val CLAUDE_COMMIT_BUNDLE = "claudeCommitBundle"

object ClaudeCommitBundle : DynamicBundle(CLAUDE_COMMIT_BUNDLE) {

    fun message(@PropertyKey(resourceBundle = CLAUDE_COMMIT_BUNDLE) key: String, vararg params: Any): String {
        return getMessage(key, *params)
    }
}




