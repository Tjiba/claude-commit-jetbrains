package fr.tjiba.claudecommit.llm

object PromptBuilder {
    private const val DIFF_PLACEHOLDER = "{diff}"

    fun build(diff: String): String = """
You are an assistant that writes professional Git commit messages.

Constraints:
- Reply only with the commit message.
- Preferred conventional format: type(scope): summary
- One title line <= 72 characters.
- Optional: one blank line then 2-4 short bullets.
- Base your response strictly on the provided diff.
- Do not invent changes that are not present.

Git Diff (staged):
$diff
""".trimIndent()

    fun build(template: String, diff: String): String {
        val normalizedTemplate = template.trim().ifEmpty { build(diff) }
        return if (normalizedTemplate.contains(DIFF_PLACEHOLDER)) {
            normalizedTemplate.replace(DIFF_PLACEHOLDER, diff)
        } else {
            """
$normalizedTemplate

Git Diff:
$diff
""".trimIndent()
        }
    }
}


