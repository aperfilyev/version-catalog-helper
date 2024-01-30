package com.aperfilyev.intellij.versioncataloghelper

import java.util.regex.Pattern

data class Artifact(
    val groupId: String,
    val artifactId: String,
    val version: String,
)

private val pattern = Pattern.compile("([^: '(\"]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: )'\"]+)")

fun String.parseArtifact(): Result<Artifact> {
    val m = pattern.matcher(this)
    return if (m.find()) {
        val groupId = m.group(1)
        val artifactId = m.group(2)
        val version = m.group(7)
        Result.success(Artifact(groupId, artifactId, version))
    } else {
        Result.failure(IllegalStateException("No match for $this"))
    }
}
