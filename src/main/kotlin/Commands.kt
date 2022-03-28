data class Command(val target: String, val invocation: String, val file: String, val context: String)

data class CommandGroup(val file: String, val context: String, val commands: Map<String, String>)

data class CommandMap(val invocationMap: Map<String, String>, val targetMap: Map<String, String>)