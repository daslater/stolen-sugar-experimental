fun String.startsWith(prefixes: List<String>): Boolean {
    return prefixes.fold(false) { curr, next -> curr || this.startsWith(next) }
}

fun String.splitFirst(separator: Regex): List<String> {
    val splits = this.split(separator)
    val rest = splits.subList(1, splits.size).joinToString(separator = "")

    return listOf(splits[0], rest)
}

@kotlin.ExperimentalStdlibApi
fun String.startsWith(regex: Regex): Boolean {
    return regex.matchesAt(this, 0)
}

fun String.removePrefix(regex: Regex): String {
    val prefix = regex.find(this)
    return this.removePrefix(prefix?.value ?: "")
}

fun String.removeSuffix(regex: Regex): String {
    val suffix = regex.find(this)
    return this.removeSuffix(suffix?.value ?: "")
}