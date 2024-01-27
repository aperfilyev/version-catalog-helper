package com.aperfilyev.intellij.versioncataloghelper.refactoring.inplace

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.introduce.inplace.InplaceVariableIntroducer
import org.toml.lang.psi.TomlKeySegment

class MyVariableIntroducer(
    elementToRename: PsiNamedElement,
    editor: Editor,
    project: Project,
    title: String
) : InplaceVariableIntroducer<TomlKeySegment>(elementToRename, editor, project, title, emptyArray(), null) {

    override fun getNameIdentifier(): PsiElement? {
        return myElementToRename
    }
}