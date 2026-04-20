package fr.tjiba.claudecommit.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import fr.tjiba.claudecommit.i18n.ClaudeCommitBundle
import fr.tjiba.claudecommit.service.CommitMessageService

class GenerateCommitMessageAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val commitMessageControl = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)
        val commitMessageDocument = e.getData(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT)
        val selectedPaths = collectSelectedPaths(e)

        object : Task.Backgroundable(project, "Generate Commit Message with Claude", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = ClaudeCommitBundle.message("action.generate.progress")
                val result = CommitMessageService(project).generateFromStagedChanges(selectedPaths)
                ApplicationManager.getApplication().invokeLater {
                    if (result.isSuccess) {
                        val message = result.getOrThrow()
                        val applied = applyMessage(commitMessageControl, commitMessageDocument, message)
                        if (applied) {
                            notify(project, NotificationType.INFORMATION, ClaudeCommitBundle.message("action.generate.success"))
                        } else {
                            notify(project, NotificationType.ERROR, ClaudeCommitBundle.message("action.generate.write.error"))
                        }
                    } else {
                        val error = result.exceptionOrNull()
                        val errorMsg = error?.message ?: ClaudeCommitBundle.message("action.generate.failed")
                        notify(project, NotificationType.ERROR, errorMsg)
                    }
                }
            }
        }.queue()
    }

    override fun update(e: AnActionEvent) {
        val hasProject = e.project != null
        e.presentation.isVisible = hasProject
        e.presentation.isEnabled = hasProject
    }

    private fun applyMessage(
        commitMessageControl: CommitMessageI?,
        document: Document?,
        message: String,
    ): Boolean {
        commitMessageControl?.setCommitMessage(message)
        if (commitMessageControl != null) return true

        document?.setText(message)
        return document != null
    }

     private fun notify(project: Project, type: NotificationType, content: String) {
          val (title, message) = when {
              type == NotificationType.ERROR && (content.contains("No tokens available", ignoreCase = true) ||
                                                  content.contains("insufficient quota", ignoreCase = true) ||
                                                  content.contains("credit limit", ignoreCase = true)) -> {
                  Pair(ClaudeCommitBundle.message("error.anthropic.insufficientQuota.title"), content)
              }
              type == NotificationType.ERROR && content.contains("Invalid API", ignoreCase = true) -> {
                  Pair("Invalid API Key", content)
              }
              type == NotificationType.ERROR && content.contains("rate limit", ignoreCase = true) -> {
                  Pair("API Rate Limited", content)
              }
              type == NotificationType.ERROR -> {
                  Pair("Generation Failed", content)
              }
              else -> Pair("Claude Commit", content)
          }

          NotificationGroupManager.getInstance()
              .getNotificationGroup("Claude Commit Notifications")
              .createNotification(title, message, type)
              .notify(project)
      }

    private fun collectSelectedPaths(e: AnActionEvent): List<String> {
        val selected = buildList {
            e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI)?.getIncludedChanges()?.let { addAll(it) }
            e.getData(VcsDataKeys.SELECTED_CHANGES)?.let { addAll(it.toList()) }
            e.getData(VcsDataKeys.SELECTED_CHANGES_IN_DETAILS)?.let { addAll(it.toList()) }
            if (isEmpty()) {
                e.getData(VcsDataKeys.CHANGES)?.let { addAll(it.toList()) }
            }
        }

        return selected.toSelectedPaths().distinct()
    }

    private fun List<Change>.toSelectedPaths(): List<String> = mapNotNull { change ->
        change.afterRevision?.file?.path ?: change.beforeRevision?.file?.path
    }

}


