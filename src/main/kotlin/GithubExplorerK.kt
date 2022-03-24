import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.kohsuke.github.GHCommit
import org.kohsuke.github.GHCommitQueryBuilder
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.PagedIterable
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class GithubExplorerK {
    private val watchFiles: MutableSet<String>
    private val baseCommands: MutableMap<String, Command>
    private val changedWords: MutableMap<String, String>
    private val gson: Gson

    init {
        watchFiles = HashSet()
        baseCommands = HashMap()
        changedWords = HashMap()
        gson = GsonBuilder().setPrettyPrinting().create()
    }

    @Throws(IOException::class)
    fun run() {
        populate()
        val github = GitHubBuilder.fromPropertyFile().build()

        val talonRepo = github.getRepository("knausj85/knausj_talon")
        val baseCommits = talonRepo.queryCommits()
            .pageSize(100)
            .list()
        val baseCommitShas = baseCommits
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
                    if (watchFiles.contains(commitFile.fileName)) {
                        println(commitFile.patch)
                        println("-------------------------------------------------------------------------")
                        analyzeCommitFile(commitFile)
                    }
                }
            }
        }
        println(gson.toJson(changedWords))
    }

    @Throws(IOException::class)
    private fun populate() {
        val type = object : TypeToken<ArrayList<CommandGroup?>?>() {}.type
        val jsonString = Files.readString(Path.of("src/main/resources/talon_commands.json"))
        val commandGroups = gson.fromJson<List<CommandGroup>>(jsonString, type)

        for ((file, context, commands) in commandGroups) {
            watchFiles.add(file)
            for (invocation in commands.keys) {
                baseCommands[invocation] = Command(
                    commands[invocation]!!, invocation,
                    file, context
                )
            }
        }
    }

    private fun analyzeCommitFile(commitFile: GHCommit.File) {
        val patch = commitFile.patch
        val lines = patch.split("\\n".toRegex()).toTypedArray()



        for (i in 1 until lines.size) {
            if (lines[i - 1].startsWith("-") && lines[i].startsWith("+")) {
                val oldWords = lines[i - 1].split(" ".toRegex()).toTypedArray()
                val newWords = lines[i].split(" ".toRegex()).toTypedArray()
                if (oldWords.size != newWords.size) {
                    continue
                }
                var index = 0
                while (index < oldWords.size) {
                    val relIndex = Arrays.mismatch(oldWords, index, oldWords.size, newWords, index, newWords.size)
                    index += if (relIndex != -1) {
                        relIndex
                    } else {
                        break
                    }
                    val oldWord = stripWord(oldWords[index])
                    val newWord = stripWord(newWords[index])
                    if (baseCommands.containsKey(oldWord) && baseCommands[oldWord]!!.file == commitFile.fileName) {
                        changedWords[oldWord] = newWord
                    }
                    index++
                }
            }
        }
    }

    private fun stripWord(word: String): String {
        return word.filter { Character.isLetter(it) }
    }
}