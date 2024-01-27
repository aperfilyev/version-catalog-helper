package com.aperfilyev.intellij.versioncataloghelper.editor

import com.aperfilyev.intellij.versioncataloghelper.parseArtifact
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RawText
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.toml.lang.psi.TomlFile
import org.toml.lang.psi.TomlTable

class MyCopyPastePreProcessor : CopyPastePreProcessor {

    private val separator = "[\\n\\r\\s]+".toRegex()

    override fun preprocessOnPaste(
        project: Project,
        file: PsiFile,
        editor: Editor,
        text: String,
        rawText: RawText?
    ): String {
        if (file !is TomlFile) {
            return text
        }
        val artifactList = text.split(separator)
            .mapNotNull { it.parseArtifact().getOrNull() }
        if (artifactList.isEmpty()) {
            return text
        }
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return text
        val table = findContainingTable(element)?.header ?: return text
        val inLibs = table.textMatches("[libraries]")
        if (!inLibs) {
            return text
        }
        return artifactList.joinToString(System.lineSeparator()) { (groupId, artifactId, version) ->
            """$artifactId = { group = "$groupId", name = "$artifactId", version = "$version" }"""
        }
    }

    private fun findContainingTable(element: PsiElement): TomlTable? {
        return PsiTreeUtil.getPrevSiblingOfType(
            element,
            TomlTable::class.java
        ) ?: PsiTreeUtil.getParentOfType(
            element,
            TomlTable::class.java
        )
    }

    override fun preprocessOnCopy(
        project: PsiFile,
        startOffsets: IntArray,
        endOffsets: IntArray,
        text: String
    ): String? {
        return null
    }
}