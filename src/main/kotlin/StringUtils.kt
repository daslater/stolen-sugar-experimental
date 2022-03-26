fun String.startsWith(vararg prefixes: String): Boolean {
    return prefixes.map { this.startsWith(it) }
        .reduce { curr, next -> curr || next }
}

fun String.stripLeft(regex: Regex): String {
    val prefix = regex.find(this)
    return this.removePrefix(prefix?.value ?: "")
}

fun String.stripRight(regex: Regex): String {
    val suffix = regex.find(this)
    return this.removeSuffix(suffix?.value ?: "")
}