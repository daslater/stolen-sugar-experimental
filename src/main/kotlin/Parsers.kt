import java.nio.file.Files
import java.nio.file.Path

fun processTalon(talon: String): MutableMap<String, String> {

    // Matches metadata at the top of a .talon file up to and including the single hyphen line "-"
    val talonMetadata = Regex("(?sm).*^-$")

    // Matches single or multiline Talon voice commands in a .talon file
    val talonCommandRegex =
        Regex("(?m)^[a-z A-Z._<>{}()|^$\\[\\]]+(?<!\\(\\)):.*(?:\\R([ \\t]+)\\S.*(?:\\R\\1\\S.*\$)*)?")

    return talonCommandRegex
        .findAll(talon.removePrefix(talonMetadata))
        .map { it.value.splitFirst(Regex(": *\\R?")) }
        .associateTo(mutableMapOf()) {
            Pair(it[0]
                .removePrefix("^")
                .removeSuffix("$"),
                it[1]
                    .trimIndent()
                    .lines()
                    .filterNot { it.startsWith("#") }
                    .joinToString(separator = "\n"))
        }
}



