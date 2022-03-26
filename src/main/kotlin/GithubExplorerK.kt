import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.kohsuke.github.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class GithubExplorerK {
    private val watchFiles: MutableSet<String>
    private val baseInvocationMap: MutableMap<String, Command>
    private val baseTargetMap: MutableMap<String, Command>
    private val changedWords: MutableMap<String, String>
    private val talonFilesProcessed: MutableSet<String>
    private val commandGroups: HashSet<CommandGroup>
    private var commandGroupMap: MutableMap<String, Map<String, String>>
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
            commandGroupMap[file] = commands
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
            .filter { it.startsWith("+", "-") }



        if (commitFile.fileName.endsWith(".py")) {

        }
    }

    private fun analyzeTalon(talon: GHContent) {

        println(talon.path)
        println("--------------------------------------")
        val talonCommands = processTalon(String(talon.read().readAllBytes()))
        val baseCommands = commandGroupMap[talon.path]

        println("Talon Commands")
        println(talonCommands)
        println("--------------------------------------")
        println("Base Commands")
        println(baseCommands)
        println("--------------------------------------")
        println("Base commands same? ${baseCommands == talonCommands}")
        println("Differences:")
        for ((key, value) in baseCommands!!.iterator()) {
            if (!talonCommands.containsKey(key)) {
                println("${key}: ${value}")
            }
        }
        for ((key, value) in talonCommands!!.iterator()) {
            if (!baseCommands.containsKey(key)) {
                println("${key}: ${value}")
            }
        }
        println("--------------------------------------")
        println("--------------------------------------")
        println("--------------------------------------")

//        for (key in baseCommands!!.keys) {
//            talonCommands.remove(key)
//        }

//        for ((key, value) in talonCommands) {
//            changedWords[baseTargetMap[value]?.invocation ?: ""] = key
//        }
    }
}