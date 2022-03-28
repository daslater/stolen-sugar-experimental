import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        try {
            new GithubSandbox().run();
//            ParsersKt.processTalon(Files.readString(Paths.get("src/main/resources/c.talon")));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
