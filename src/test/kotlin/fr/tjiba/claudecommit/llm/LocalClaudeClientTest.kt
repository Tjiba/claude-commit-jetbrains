package fr.tjiba.claudecommit.llm

import kotlin.test.Test
import kotlin.test.assertEquals

class LocalClaudeClientTest {
    private val client = LocalClaudeClient()

    @Test
    fun `tokenize simple claude template`() {
        assertEquals(
            listOf("claude", "-p", "{prompt}"),
            client.tokenizeTemplate("claude -p \"{prompt}\"")
        )
    }

    @Test
    fun `tokenize quoted executable path with spaces`() {
        assertEquals(
            listOf("C:\\Program Files\\Claude\\claude.exe", "-p", "{prompt}"),
            client.tokenizeTemplate("\"C:\\Program Files\\Claude\\claude.exe\" -p \"{prompt}\"")
        )
    }
}


