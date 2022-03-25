import java.nio.file.Files
import java.nio.file.Path

fun parseTalon() {

//    val lines = talon.split("\\n".toRegex())
//        .filter { it[0] != '#' && !it.startsWith("tag") && !it.startsWith("settings") }
    val talon = Files.readString(Path.of("src/main/resources/eclipse.talon"))
    val commands = "^[a-z A-Z._<>()|^$\\[\\]]+ *:.*$(?:\\n^( +)\\S.*\$(?:\\n^\\1\\S.*\$)*)?".toRegex(RegexOption.MULTILINE).findAll(talon)

//    (?:(\n +)\S+$)??(?:\1.*$)*

    for (command in commands) {
        println(command.value)
    }


//    /
//    ^(\s*)\S.*$    #Find a line with some number of spaces
//    (?:^\1\S.*$)*  #Find more lines with the same starting spaces
//    ^.*$           #This is the line you want here
//    /xm            #x to ignore whitespace in the regex.
//    #m to have ^and $ match all lines

}