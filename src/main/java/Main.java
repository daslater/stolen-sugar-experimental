import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        try {
            new GithubExplorerK().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //ParsersKt.processTalon();

    }
}
