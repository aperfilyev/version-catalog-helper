package com.aperfilyev.intellij.versioncataloghelper.editor

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.lang.annotations.Language
import org.toml.lang.psi.TomlFileType
import java.awt.datatransfer.StringSelection

class MyCopyPastePreProcessorTest : BasePlatformTestCase() {

    fun testEmptyFile() {
        doTest(
            """implementation 'com.squareup.retrofit2:retrofit:2.9.0'""",
            """
            |[versions]
            |[libraries]
            |<caret>
            |[plugins]
            """.trimMargin(),
            """
            |[versions]
            |[libraries]
            |retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version = "2.9.0" }
            |[plugins]
            """.trimMargin()
        )
    }

    fun testNonEmptyFile() {
        doTest(
            """implementation 'com.squareup.retrofit2:retrofit:2.9.0'""",
            """
            |[versions]
            |[libraries]
            |my-lib = "com.mycompany:mylib:1.4"
            |my-other-lib = { module = "com.mycompany:other", version = "1.4" }
            |my-other-lib2 = { group = "com.mycompany", name = "name", version = "1.4" }
            |<caret>
            |[plugins]
            """.trimMargin(),
            """
            |[versions]
            |[libraries]
            |my-lib = "com.mycompany:mylib:1.4"
            |my-other-lib = { module = "com.mycompany:other", version = "1.4" }
            |my-other-lib2 = { group = "com.mycompany", name = "name", version = "1.4" }
            |retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version = "2.9.0" }
            |[plugins]
            """.trimMargin()
        )
    }

    fun testOnlyLibrariesBlock() {
        doTest(
            """implementation 'com.squareup.retrofit2:retrofit:2.9.0'""",
            """
            |[libraries]
            |<caret>
            """.trimMargin(),
            """
            |[libraries]
            |retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version = "2.9.0" }
            """.trimMargin()
        )
    }


    private fun doTest(
        text: String,
        @Language("TOML") beforeText: String,
        @Language("TOML") afterText: String
    ) {
        myFixture.configureByText(TomlFileType, beforeText)
        CopyPasteManager.getInstance().setContents(StringSelection(text))
        EditorTestUtil.performPaste(myFixture.editor)
        myFixture.checkResult(afterText.trimMargin())
    }
}