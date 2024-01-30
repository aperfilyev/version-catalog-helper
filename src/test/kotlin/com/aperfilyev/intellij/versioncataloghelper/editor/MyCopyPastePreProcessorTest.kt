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
            """.trimMargin(),
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
            """.trimMargin(),
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
            """.trimMargin(),
        )
    }

    fun testMultiline() {
        doTest(
            """
            |val apacheCommonsLang3 = "org.apache.commons:commons-lang3:3.11"
            |val apacheCommonsPool2 = "org.apache.commons:commons-pool2:2.11.1"
            |val assertj = "org.assertj:assertj-core:3.24.2"
            |val awaitility = "org.awaitility:awaitility:4.1.0"
            |val awaitilityKotlin = "org.awaitility:awaitility-kotlin:4.1.0"
            |val aws2Bom = "software.amazon.awssdk:bom:2.21.24"
            |val aws2Dynamodb = "software.amazon.awssdk:dynamodb:2.21.24"
            |val aws2DynamodbEnhanced = "software.amazon.awssdk:dynamodb-enhanced:2.21.24"
            |val awsAuth = "software.amazon.awssdk:auth:2.21.24"
            |val awsCore = "software.amazon.awssdk:aws-core:2.21.24"
            |val awsDynamodb = "com.amazonaws:aws-java-sdk-dynamodb:1.12.576"
            |val awsJavaSdkCore = "com.amazonaws:aws-java-sdk-core:1.12.576"
            |val awsRegions = "software.amazon.awssdk:regions:2.21.24"
            |val awsS3 = "com.amazonaws:aws-java-sdk-s3:1.12.576"
            |val awsSdkCore = "software.amazon.awssdk:sdk-core:2.21.24"
            |val awsSqs = "com.amazonaws:aws-java-sdk-sqs:1.12.576"
            |val bouncycastle = "org.bouncycastle:bcprov-jdk15on:1.70"
            |val bouncycastlePgp = "org.bouncycastle:bcpg-jdk15on:1.70"
            |val bucket4jCore = "com.bucket4j:bucket4j-core:8.5.0"
            |val bucket4jDynamoDbV1 = "com.bucket4j:bucket4j-dynamodb-sdk-v1:8.5.0"
            """.trimMargin(),
            """
            |[libraries]
            |<caret>
            """.trimMargin(),
            """
            |[libraries]
            |commons-lang3 = { group = "org.apache.commons", name = "commons-lang3", version = "3.11" }
            |commons-pool2 = { group = "org.apache.commons", name = "commons-pool2", version = "2.11.1" }
            |assertj-core = { group = "org.assertj", name = "assertj-core", version = "3.24.2" }
            |awaitility = { group = "org.awaitility", name = "awaitility", version = "4.1.0" }
            |awaitility-kotlin = { group = "org.awaitility", name = "awaitility-kotlin", version = "4.1.0" }
            |bom = { group = "software.amazon.awssdk", name = "bom", version = "2.21.24" }
            |dynamodb = { group = "software.amazon.awssdk", name = "dynamodb", version = "2.21.24" }
            |dynamodb-enhanced = { group = "software.amazon.awssdk", name = "dynamodb-enhanced", version = "2.21.24" }
            |auth = { group = "software.amazon.awssdk", name = "auth", version = "2.21.24" }
            |aws-core = { group = "software.amazon.awssdk", name = "aws-core", version = "2.21.24" }
            |aws-java-sdk-dynamodb = { group = "com.amazonaws", name = "aws-java-sdk-dynamodb", version = "1.12.576" }
            |aws-java-sdk-core = { group = "com.amazonaws", name = "aws-java-sdk-core", version = "1.12.576" }
            |regions = { group = "software.amazon.awssdk", name = "regions", version = "2.21.24" }
            |aws-java-sdk-s3 = { group = "com.amazonaws", name = "aws-java-sdk-s3", version = "1.12.576" }
            |sdk-core = { group = "software.amazon.awssdk", name = "sdk-core", version = "2.21.24" }
            |aws-java-sdk-sqs = { group = "com.amazonaws", name = "aws-java-sdk-sqs", version = "1.12.576" }
            |bcprov-jdk15on = { group = "org.bouncycastle", name = "bcprov-jdk15on", version = "1.70" }
            |bcpg-jdk15on = { group = "org.bouncycastle", name = "bcpg-jdk15on", version = "1.70" }
            |bucket4j-core = { group = "com.bucket4j", name = "bucket4j-core", version = "8.5.0" }
            |bucket4j-dynamodb-sdk-v1 = { group = "com.bucket4j", name = "bucket4j-dynamodb-sdk-v1", version = "8.5.0" }
            """.trimMargin(),
        )
    }

    fun testWrongBlock() {
        doTest(
            """implementation 'com.squareup.retrofit2:retrofit:2.9.0'""",
            """
            |[plugins]
            |<caret>
            """.trimMargin(),
            """
            |[plugins]
            |implementation 'com.squareup.retrofit2:retrofit:2.9.0'
            """.trimMargin(),
        )
    }

    fun testCapitalizedName() {
        doTest(
            """implementation 'com.zaxxer:HikariCP:4.0.3'""",
            """
            |[libraries]
            |<caret>
            """.trimMargin(),
            """
            |[libraries]
            |hikariCP = { group = "com.zaxxer", name = "HikariCP", version = "4.0.3" }
            """.trimMargin(),
        )
    }

    fun testNameContainsPeriod() {
        doTest(
            """implementation 'jakarta.inject:jakarta.inject-api:2.0.1'""",
            """
            |[libraries]
            |<caret>
            """.trimMargin(),
            """
            |[libraries]
            |jakarta-inject-api = { group = "jakarta.inject", name = "jakarta.inject-api", version = "2.0.1" }
            """.trimMargin(),
        )
    }

    private fun doTest(
        text: String,
        @Language("TOML") beforeText: String,
        @Language("TOML") afterText: String,
    ) {
        myFixture.configureByText(TomlFileType, beforeText)
        CopyPasteManager.getInstance().setContents(StringSelection(text))
        EditorTestUtil.performPaste(myFixture.editor)
        myFixture.checkResult(afterText.trimMargin())
    }
}
