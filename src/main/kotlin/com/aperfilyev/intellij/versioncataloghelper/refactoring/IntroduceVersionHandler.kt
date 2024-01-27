package com.aperfilyev.intellij.versioncataloghelper.refactoring

import com.aperfilyev.intellij.versioncataloghelper.refactoring.inplace.MyVariableIntroducer
import com.aperfilyev.intellij.versioncataloghelper.util.getLastChildOfType
import com.intellij.ide.plugins.PluginManagerCore.isUnitTestMode
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.toml.lang.psi.TomlKeyValue
import org.toml.lang.psi.TomlTable
import org.toml.lang.psi.TomlValue

class IntroduceVersionHandler : RefactoringActionHandler {

    override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext) {
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return
        val versionKeyValue = PsiTreeUtil.getParentOfType(element, TomlKeyValue::class.java) ?: return
        val entry = PsiTreeUtil.getTopmostParentOfType(versionKeyValue, TomlKeyValue::class.java) ?: return
        val groupId = findGroupId(entry) ?: return
        val allTables = PsiTreeUtil.getChildrenOfType(file, TomlTable::class.java)?.toList() ?: return

        val matchingVersions = findAllOccurrences(allTables, groupId, versionKeyValue)
        if (matchingVersions.isEmpty()) {
            return
        }

        val suggestedName = "${groupId.removeSurrounding("\"").substringAfterLast(".")}Version"
        val text = "\n$suggestedName = ${element.text}"
        val replacement = "version.ref = \"$suggestedName\""
        val insertionPlace = findInsertionPlace(allTables) ?: return
        val documentManager = PsiDocumentManager.getInstance(project)
        showOccurrencesChooser(editor, versionKeyValue.value!!, matchingVersions) { versions ->
            val document = editor.document
            WriteCommandAction.runWriteCommandAction(project) {
                val markers = versions.mapNotNull { value ->
                    val tomlKeyValue = PsiTreeUtil.getParentOfType(value, TomlKeyValue::class.java)
                    tomlKeyValue?.let { document.createRangeMarker(it.startOffset, tomlKeyValue.endOffset) }
                }
                for (marker in markers) {
                    document.replaceString(marker.startOffset, marker.endOffset, replacement)
                }
                document.insertString(insertionPlace.endOffset, text)
            }
            documentManager.commitDocument(document)

            documentManager.performForCommittedDocument(document) action@{
                val addedElement = insertionPlace.lastChild as? TomlKeyValue ?: return@action
                val keySegment = addedElement.key.segments.firstOrNull() ?: return@action
                editor.caretModel.moveToOffset(keySegment.startOffset)
                MyVariableIntroducer(keySegment, editor, project, "Choose a name")
                    .performInplaceRefactoring(linkedSetOf(suggestedName))
            }
        }
    }

    private fun findGroupId(entry: TomlKeyValue?): String? {
        val groupKeyValue = PsiTreeUtil.getChildOfType(entry?.value, TomlKeyValue::class.java)
        val groupId = when {
            groupKeyValue?.key?.textMatches("module") == true -> groupKeyValue.value?.text?.substringBefore(":")
            groupKeyValue?.key?.textMatches("group") == true -> groupKeyValue.value?.text
            else -> null
        }
        return groupId?.replace("\"", "")
    }

    private fun findAllOccurrences(
        allTables: List<TomlTable>,
        groupId: String,
        versionKeyValue: TomlKeyValue
    ): List<TomlValue> {
        val libsTable = allTables.firstOrNull { it.header.textMatches("[libraries]") } ?: return emptyList()
        return libsTable.entries.filter { findGroupId(it) == groupId }
            .mapNotNull {
                val versionKv = getLastChildOfType(it.value!!, TomlKeyValue::class.java)
                if (versionKv?.textMatches(versionKeyValue) == true) versionKv.value else null
            }
    }

    private fun findInsertionPlace(allTables: List<TomlTable>): TomlTable? {
        return allTables.firstOrNull { it.header.textMatches("[versions]") }
    }

    private fun showOccurrencesChooser(
        editor: Editor,
        selected: TomlValue,
        occurrences: List<TomlValue>,
        callback: (List<TomlValue>) -> Unit
    ) {
        if (!isUnitTestMode) {
            OccurrencesChooser.simpleChooser<TomlValue>(editor)
                .showChooser(selected, occurrences, Pass.create {
                    if (it == OccurrencesChooser.ReplaceChoice.ALL) {
                        callback(occurrences)
                    } else {
                        callback(listOf(selected))
                    }
                })
        } else {
            callback(occurrences)
        }
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
    }
}
