import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GithubExplorer {

    private Set<String> watchFiles;
    private Map<String, Command> baseCommands;
    private Map<String, String> changedWords;
    private Gson gson;

    public GithubExplorer() {
        watchFiles = new HashSet<>();
        baseCommands = new HashMap<>();
        changedWords = new HashMap<>();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void run() throws IOException {

        populate();

        GitHub github = GitHubBuilder.fromPropertyFile().build();

        GHRepository talonRepo = github.getRepository("knausj85/knausj_talon");

        Set<String> baseCommits = talonRepo.listCommits().toList().stream()
                .map(x -> x.getSHA1()).collect(Collectors.toSet());

        GHRepository ronRepo = github.getRepository("RonWalker22/knausj_talon");

        List<GHCommit> ronCommits = ronRepo.listCommits().toList();

        for (var commit : ronCommits) {
            if (!baseCommits.contains(commit.getSHA1())) {
                for (var commitFile : commit.getFiles()) {
                    if (watchFiles.contains(commitFile.getFileName())) {
                        System.out.println(commitFile.getPatch());
                        System.out.println("-------------------------------------------------------------------------");
                        analyzeCommitFile(commitFile);
                    }
                }
            }
        }

        System.out.println(gson.toJson(changedWords));
    }

    private void populate() throws IOException {
        Type type = new TypeToken<ArrayList<CommandGroup>>(){}.getType();
        String jsonString = Files.readString(Path.of("src/main/resources/talon_commands.json"));
        List<CommandGroup> commandGroups = gson.fromJson(jsonString, type);

        for (var commandGroup : commandGroups) {
            watchFiles.add(commandGroup.getFile());
            for (var invocation : commandGroup.getCommands().keySet()) {
                baseCommands.put(invocation,
                        new Command(commandGroup.getCommands().get(invocation), invocation,
                                commandGroup.getFile(), commandGroup.getContext()));
            }
        }
    }

    private void analyzeCommitFile(GHCommit.File commitFile) {
        String patch = commitFile.getPatch();
        String[] lines = patch.split("\\n");

        for (int i = 1; i < lines.length; i++) {
            if (lines[i - 1].startsWith("-") && lines[i].startsWith("+")) {
                String[] oldWords = lines[i - 1].split(" ");
                String[] newWords = lines[i].split(" ");
                if (oldWords.length != newWords.length) {
                    continue;
                }
                int index = 0;
                while(index < oldWords.length) {
                    int relIndex = Arrays.mismatch(oldWords, index, oldWords.length, newWords, index, newWords.length);
                    if (relIndex != -1) {
                        index += relIndex;
                    } else {
                        break;
                    }
                    String oldWord = stripWord(oldWords[index]);
                    String newWord = stripWord(newWords[index]);
                    if (baseCommands.containsKey(oldWord) &&
                            baseCommands.get(oldWord).getFile().equals(commitFile.getFileName())) {
                        changedWords.put(oldWord, newWord);
                    }
                    index++;
                }
            }
        }
    }

    private String stripWord(String word) {
        StringBuilder strippedWord = new StringBuilder();
        for (var ch : word.toCharArray()) {
            if (Character.isLetter(ch)) {
                strippedWord.append(ch);
            }
        }
        return strippedWord.toString();
    }
}
