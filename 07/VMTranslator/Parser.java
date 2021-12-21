import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Parser {

    private List<String> allCommands;
    private int currentCommand = 0;   
    private Command currentCommantType;
    private String arg1;
    private int arg2;

    public Parser(String inputFileName) {

        try {
            allCommands = Files.lines(Paths.get(inputFileName), StandardCharsets.UTF_8)
                            .map(s -> sanitize(s))
                            .filter(s -> isCommand(s))
                            .collect(Collectors.toList());
        } catch (IOException e) {
            handleException(e);
        }

    }

    public boolean hasMoreCommands() {
        return currentCommand < allCommands.size();
    }

    public void advance() {
        String newCommand = allCommands.get(currentCommand);

        // parse command and populate fields

        currentCommand++;
    }

    public Command commandType() {
        return null;
    }

    public String arg1() {
        return null;
    }

    public int arg2() {
        return 0;
    }
    
    private void handleException(Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e.getMessage());
    }

    private String sanitize(String line) {
        String sanitized = line;

        if (sanitized.contains("//")) {
            int commentStart = sanitized.indexOf("//");
            sanitized = sanitized.substring(0, commentStart);
        }

        sanitized = sanitized.trim();

        return sanitized;
    }

    private boolean isCommand(String line) {
        String sanitized = sanitize(line);

        // if (normalized.)

        return !sanitized.equals("");
    }

}
