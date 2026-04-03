package fr.tjiba.claudecommit.git

import kotlin.test.Test
import kotlin.test.assertEquals
import java.nio.file.Paths

class GitDiffCollectorTest {
    @Test
    fun `toRelativeGitPath converts absolute path under root`() {
        val root = Paths.get("C:/repo").toAbsolutePath().normalize()
        val result = GitDiffCollector.toRelativeGitPath(root, "C:/repo/docs/README.md")

        assertEquals("docs/README.md", result)
    }

    @Test
    fun `toRelativeGitPath keeps path outside root`() {
        val root = Paths.get("C:/repo").toAbsolutePath().normalize()
        val result = GitDiffCollector.toRelativeGitPath(root, "C:/other/file.txt")

        assertEquals("C:/other/file.txt", result)
    }
}


