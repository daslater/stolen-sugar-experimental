import java.nio.file.Files
import java.nio.file.Path

fun parseTalon() {

    val talon = Files.readString(Path.of("src/main/resources/eclipse.talon"))

    // Matches metadata at the top of a .talon file up to and including the single hyphen line "-"
    val talonMetadata = Regex("(?sm).*^-$")

    // Matches single or multiline Talon voice commands in a .talon file
    val talonCommandRegex = Regex(
        "(?m)^[a-z A-Z._<>{}()|^$\\[\\]]+(?<!\\(\\)):.*(?:\\R([ \\t]+)\\S.*(?:\\R\\1\\S.*\$)*)?")

    val commands = talonCommandRegex
        .findAll(talon.stripLeft(talonMetadata))
        .map { it.value }

    for (command in commands) println(command)


}

