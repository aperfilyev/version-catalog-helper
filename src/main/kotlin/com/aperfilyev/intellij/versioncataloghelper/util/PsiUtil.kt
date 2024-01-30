package com.aperfilyev.intellij.versioncataloghelper.util

import com.intellij.psi.PsiElement

fun <T : PsiElement?> getLastChildOfType(
    element: PsiElement,
    aClass: Class<T>,
): T? {
    var child = element.lastChild
    while (child != null) {
        if (aClass.isInstance(child)) {
            return aClass.cast(child)
        }
        child = child.prevSibling
    }

    return null
}
