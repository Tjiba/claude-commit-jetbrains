package fr.tjiba.claudecommit.git

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import fr.tjiba.claudecommit.i18n.ClaudeCommitBundle
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration

object GitDiffCollector {
    fun collectStagedDiff(project: Project, selectedPaths: List<String> = emptyList()): Result<String> {
        return runCatching {
            val root = ProjectLevelVcsManager.getInstance(project).allVcsRoots.firstOrNull()?.path
                ?: error(ClaudeCommitBundle.message("error.git.repoMissing"))

            val rootPath = root.toNioPath().normalize()
            val stagedDiff = runDiff(rootPath, selectedPaths, cached = true)
            if (stagedDiff.isNotBlank()) {
                return@runCatching stagedDiff
            }

            if (selectedPaths.isNotEmpty()) {
                val workingTreeDiff = runDiff(rootPath, selectedPaths, cached = false)
                if (workingTreeDiff.isNotBlank()) {
                    return@runCatching workingTreeDiff
                }
            }

            error(ClaudeCommitBundle.message("error.git.diffEmpty"))
        }
    }

    internal fun buildDiffCommand(rootPath: Path, selectedPaths: List<String> = emptyList(), cached: Boolean = true): GeneralCommandLine {
        val commandLine = GeneralCommandLine("git", "diff", *if (cached) arrayOf("--cached") else emptyArray(), "--no-color")
            .withWorkDirectory(rootPath.toFile())

        selectedPaths
            .mapNotNull { toRelativeGitPath(rootPath, it) }
            .distinct()
            .takeIf { it.isNotEmpty() }
            ?.let { commandLine.addParameters("--", *it.toTypedArray()) }

        return commandLine
    }

    internal fun runDiff(rootPath: Path, selectedPaths: List<String>, cached: Boolean): String {
        val commandLine = buildDiffCommand(rootPath, selectedPaths, cached)
        val output = CapturingProcessHandler(commandLine).runProcess(Duration.ofSeconds(15).toMillis().toInt())
        if (output.exitCode != 0) {
            error(output.stderr.takeIf { it.isNotBlank() } ?: ClaudeCommitBundle.message("error.git.diffRead"))
        }

        return output.stdout.trim()
    }

    internal fun toRelativeGitPath(rootPath: Path, absolutePath: String): String? {
        val filePath = runCatching { Paths.get(absolutePath).normalize() }.getOrNull() ?: return null
        return runCatching {
            if (filePath.startsWith(rootPath)) rootPath.relativize(filePath).toString() else filePath.toString()
        }.getOrNull()?.replace('\\', '/')
    }
}


