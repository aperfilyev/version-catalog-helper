package com.aperfilyev.intellij.versioncataloghelper.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringActionHandler
import org.toml.lang.psi.TomlKeyValue

class MyRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isAvailable(context: PsiElement): Boolean {
        val parent = PsiTreeUtil.getParentOfType(context, TomlKeyValue::class.java) ?: return false
        return parent.key.textMatches("version") && parent.value == context.parent
    }

    override fun getIntroduceConstantHandler(): RefactoringActionHandler = IntroduceVersionHandler()
}
