package com.almightyalpaca.jetbrains.plugins.discord.plugin.listeners

import com.almightyalpaca.jetbrains.plugins.discord.plugin.components.ApplicationComponent
import com.almightyalpaca.jetbrains.plugins.discord.plugin.logging.Logging
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import java.time.OffsetDateTime

class FileDocumentManagerListener : com.intellij.openapi.fileEditor.FileDocumentManagerListener {

    override fun beforeAllDocumentsSaving() {}

    override fun beforeDocumentSaving(document: Document) {
        log { "FileDocumentManagerListener#beforeDocumentSaving($document)" }

        val editors = EditorFactory.getInstance().getEditors(document)
        val file = FileDocumentManager.getInstance().getFile(document)

        ApplicationComponent.instance.app {
            for (editor in editors) {
                update(editor.project) {
                    update(file) {
                        accessedAt = OffsetDateTime.now()
                    }
                }
            }
        }
    }

    override fun beforeFileContentReload(file: VirtualFile, document: Document) {}

    override fun fileWithNoDocumentChanged(file: VirtualFile) {}

    override fun fileContentReloaded(file: VirtualFile, document: Document) {}

    override fun fileContentLoaded(file: VirtualFile, document: Document) {}

    override fun unsavedDocumentsDropped() {}

    companion object : Logging()
}
