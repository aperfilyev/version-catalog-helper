package com.aperfilyev.intellij.versioncataloghelper.refactoring

import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.lang.annotations.Language
import org.toml.lang.psi.TomlFileType

class IntroduceVersionHandlerTest : BasePlatformTestCase() {
    fun testSingleOccurrenceLongNotation() {
        doTest(
            """
            |[versions]
            |[libraries]
            |my-lib = "com.mycompany:mylib:1.4"
            |my-other-lib2 = { group = "com.mycompany", name = "name", version = "1<caret>.4" }
            """.trimMargin(),
            """
            |[versions]
            |mycompanyVersion = "1.4"
            |[libraries]
            |my-lib = "com.mycompany:mylib:1.4"
            |my-other-lib2 = { group = "com.mycompany", name = "name", version.ref = "mycompanyVersion" }
            """.trimMargin(),
        )
    }

    fun testSingleOccurrenceShortNotation() {
        doTest(
            """
            |[versions]
            |[libraries]
            |my-lib = "com.mycompany:mylib:1.4"
            |my-other-lib = { module = "com.mycompany:other", version = "1<caret>.4" }
            """.trimMargin(),
            """
            |[versions]
            |mycompanyVersion = "1.4"
            |[libraries]
            |my-lib = "com.mycompany:mylib:1.4"
            |my-other-lib = { module = "com.mycompany:other", version.ref = "mycompanyVersion" }
            """.trimMargin(),
        )
    }

    fun testMultipleOccurrence() {
        doTest(
            """
            |[versions]
            |[libraries]
            |my-lib = "com.mycompany:mylib:1.4"
            |my-other-lib = { module = "com.mycompany:other", version = "1<caret>.4" }
            |my-other-lib2 = { group = "com.mycompany", name = "name", version = "1.4" }
            """.trimMargin(),
            """
            |[versions]
            |mycompanyVersion = "1.4"
            |[libraries]
            |my-lib = "com.mycompany:mylib:1.4"
            |my-other-lib = { module = "com.mycompany:other", version.ref = "mycompanyVersion" }
            |my-other-lib2 = { group = "com.mycompany", name = "name", version.ref = "mycompanyVersion" }
            """.trimMargin(),
        )
    }

    private fun doTest(
        @Language("TOML") before: String,
        @Language("TOML") after: String,
    ) {
        myFixture.configureByText(TomlFileType, before)
        val context = SimpleDataContext.builder().build()
        val versionHandler = IntroduceVersionHandler()
        versionHandler.invoke(myFixture.project, myFixture.editor, myFixture.file, context)

        myFixture.checkResult(after)
    }
}
