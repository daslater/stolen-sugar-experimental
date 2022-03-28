import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.kohsuke.github.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class GithubSandbox {
    private val watchFiles: MutableSet<String>
    private val baseInvocationMap: MutableMap<String, Command>
    private val baseTargetMap: MutableMap<String, Command>
    private val changedWords: MutableMap<String, MutableMap<String, String>>
    private val talonFilesProcessed: MutableSet<String>
    private val commandGroups: HashSet<CommandGroup>
    private var commandGroupMap: MutableMap<String, CommandMap>
    private val github: GitHub
    private val gson: Gson

    init {
        watchFiles = HashSet()
        baseInvocationMap = HashMap()
        baseTargetMap = HashMap()
        changedWords = HashMap()
        talonFilesProcessed = HashSet()
        gson = GsonBuilder().setPrettyPrinting().create()
        val type = object : TypeToken<HashSet<CommandGroup?>?>() {}.type
        val jsonString = Files.readString(Path.of("src/main/resources/talon_commands.json"))
        commandGroups = gson.fromJson(jsonString, type)
        commandGroupMap = HashMap()
        github = GitHubBuilder.fromPropertyFile().build()
    }

    @Throws(IOException::class)
    fun run() {
        populate()
        val talonRepo = github.getRepository("knausj85/knausj_talon")
        val baseCommitShas = talonRepo.queryCommits()
            .pageSize(100)
            .list()
            .map { x: GHCommit -> x.shA1 }
            .toSet()

        val ronRepo = github.getRepository("RonWalker22/knausj_talon")
        val ronCommits = ronRepo.queryCommits()
            .pageSize(100)
            .list()
            .toList()

        for (commit in ronCommits) {
            if (commit.shA1 !in baseCommitShas) {
                for (commitFile in commit.files) {
                    if (commitFile.fileName in watchFiles) {
//                        println(commitFile.fileName)
//                        println(commitFile.patch)
//                        println("-------------------------------------------------------------------------")
                        if (commitFile.fileName.endsWith(".talon")) {
                            val talonFile = ronRepo.getFileContent(commitFile.fileName)
                            analyzeTalon(talonFile)
                        }
                        //analyzeCommitFile(commitFile)
                    }
                }
            }
        }
        println(gson.toJson(changedWords))
    }

    @Throws(IOException::class)
    private fun populate() {
        for ((file, context, commands) in commandGroups) {
            watchFiles.add(file)
            val targetMap = commands
                .map { (k, v) -> Pair(v, k) }
                .toMap()
            commandGroupMap[file] = CommandMap(invocationMap = commands, targetMap = targetMap)
            for (invocation in commands.keys) {
                baseInvocationMap[invocation] = Command(
                    commands[invocation]!!, invocation,
                    file, context
                )
            }
        }
    }

    private fun analyzeCommitFile(commitFile: GHCommit.File) {
        val patch = commitFile.patch
        val lines = patch.split("\\n".toRegex())
            .filter { it.startsWith(listOf("+", "-")) }



        if (commitFile.fileName.endsWith(".py")) {

        }
    }

    private fun analyzeTalon(talon: GHContent) {

        println(talon.path)
        println("--------------------------------------")
        val talonCommands = processTalon(String(talon.read().readAllBytes()))
        val baseTargets = commandGroupMap[talon.path]!!.targetMap
        val baseInvocations = commandGroupMap[talon.path]!!.invocationMap
        val localChanges: MutableMap<String, String> = HashMap()

        println("Talon Commands")
        println(gson.toJson(talonCommands))
        println("--------------------------------------")
        println("Base Commands")
        println(gson.toJson(baseInvocations))
        println("--------------------------------------")
        println("Base commands same? ${baseInvocations == talonCommands}")
        println("Differences:")
        println("Old:")
        for ((k, v) in (baseInvocations.entries - talonCommands.entries)) {
            println("$k: $v")
        }
        println("New: ")
        for ((k, v) in (talonCommands.entries - baseInvocations.entries)) {
            println("$k: $v")
        }
        for ((key, value) in talonCommands.iterator()) {
            if (!baseInvocations.containsKey(key)) {
//                println("Old: ${baseTargets.getOrDefault(value, "")}")
//                println("New: ${key}")
                if (baseTargets.containsKey(value)) {
                    val oldWord: String = baseTargets.getOrDefault(value, "")
                    val newWord = key
                    localChanges[oldWord] = newWord
                }
            }
        }

        changedWords[talon.path] = localChanges

        println("--------------------------------------")
        println("--------------------------------------")
        println("--------------------------------------")
    }

    private fun analyzePython(commitFile: GHCommit.File) {

    }
}