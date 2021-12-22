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
        parse(newCommand);

        currentCommand++;
    }

    public Command commandType() {
        return currentCommantType;
    }

    public String arg1() {
        return arg1;
    }

    public int arg2() {
        return arg2;
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

        return !sanitized.equals("");
    }

    private void parse(String command) {
        String[] tokens = command.split(" ");

        parseCommandType(tokens[0].toLowerCase());

        // TODO: if necessary on next project, change this to parse commands specifically by types
        if (tokens.length > 1) {
            arg1 = tokens[1];
        }
        if (tokens.length > 2) {
            arg2 = Integer.parseInt(tokens[2]);
        }

        if (currentCommantType == Command.C_ARITHMETIC) {
            arg1 = tokens[0];
        }
    }

    private void parseCommandType(String strCommand) {
        // TODO: check if there is a better way to convert from str to enum
        if (strCommand.equals("push")) {
            currentCommantType = Command.C_PUSH;
        }
        else if (strCommand.equals("pop")) {
            currentCommantType = Command.C_POP;
        }
        else if (strCommand.equals("label")) {
            currentCommantType = Command.C_LABEL;
        }
        else if (strCommand.equals("goto")) {
            currentCommantType = Command.C_GOTO;
        }
        else if (strCommand.equals("if-goto")) {
            currentCommantType = Command.C_IF;
        }
        else if (strCommand.equals("function")) {
            currentCommantType = Command.C_FUNCTION;
        }
        else if (strCommand.equals("call")) {
            currentCommantType = Command.C_CALL;
        }
        else if (strCommand.equals("return")) {
            currentCommantType = Command.C_RETURN;
        }
        else {
            currentCommantType = Command.C_ARITHMETIC;
        }
    }
}
