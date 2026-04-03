package fr.tjiba.claudecommit.llm

import kotlin.test.Test
import kotlin.test.assertTrue

class PromptBuilderTest {
    @Test
    fun `build includes diff content and instructions`() {
        val diff = "diff --git a/a.txt b/a.txt\n+hello"
        val prompt = PromptBuilder.build(diff)

        assertTrue(prompt.contains("Git commit messages"))
        assertTrue(prompt.contains(diff))
    }

    @Test
    fun `build with template replaces diff placeholder`() {
        val prompt = PromptBuilder.build("Analyze this diff:\n{diff}", "ABC")
        assertTrue(prompt.contains("Analyze this diff"))
        assertTrue(prompt.contains("ABC"))
    }

    @Test
    fun `build with template without placeholder appends diff`() {
        val prompt = PromptBuilder.build("Short message", "XYZ")
        assertTrue(prompt.contains("Short message"))
        assertTrue(prompt.contains("Git Diff"))
        assertTrue(prompt.contains("XYZ"))
    }
}


