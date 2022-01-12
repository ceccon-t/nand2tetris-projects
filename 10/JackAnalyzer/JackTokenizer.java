import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class JackTokenizer {

    // private String rawInput;

    private String originalPath;

    private List<String> inputLines;
    private String sanitizedInput;


    public JackTokenizer(String inputFilePath) {
        originalPath = inputFilePath;

        try {
            // rawInput = Files.readString(Path.of(inputFilePath));

            inputLines = Files.lines(Paths.get(inputFilePath), StandardCharsets.UTF_8)
                            .map(s -> removeInlineComments(s))
                            .filter(s -> !isEmptyLine(s))
                            .collect(Collectors.toList());

            buildProcessedInput();

            sanitizedInput = removeMultilineComments(sanitizedInput);
        } catch(IOException e) {
            throw new RuntimeException("Could not read from input file: " + inputFilePath);
        }

        // System.out.println(sanitizedInput);
        printTokensToFile();

    }

    public Boolean hasMoreTokens() {
        // TODO: Implement
        return false;
    }

    public void advance() {
        // TODO: Implement
    }

    public TokenTypes tokenType() {
        // TODO: Implement
        return null;
    }

    public Keywords keyWord() {
        // TODO: Implement
        return null;        
    }

    public Character symbol() {
        // TODO: Implement
        return null;
    }

    public String identifier() {
        // TODO: Implement
        return null;
    }

    public Integer intVal() {
        // TODO: Implement
        return null;
    }

    public String stringVal() {
        // TODO: Implement
        return null;
    }

    private String removeInlineComments(String line) {
        String sanitized = line;

        if (sanitized.contains("//")) {
            int commentStart = sanitized.indexOf("//");
            sanitized = sanitized.substring(0, commentStart);
        }

        sanitized = sanitized.trim();

        return sanitized;
    }

    private Boolean isEmptyLine(String line) {
        String sanitized = line.trim();
        return sanitized.equals("");
    }

    private String removeMultilineComments(String segment) {
        StringBuilder builder = new StringBuilder();
        int size = segment.length();
        int i = 0, j = 0;
        boolean foundEnd;

        while (i < size-1) {    // will be looking at both s[i] and s[i+1] for '/*'
            if ((segment.charAt(i) == '/') && (segment.charAt(i+1) == '*')) {
                // Traverse until end of comment
                j = i+2;
                foundEnd = false;
                while ( (j < size-1) && !foundEnd ) {
                    if ( (segment.charAt(j) == '*') && (segment.charAt(j+1) == '/') ) {
                        foundEnd = true;
                    } else {
                        j++;
                    }
                }
                i = j+2;
            } else {
                builder.append(segment.charAt(i));
            }

            i++;
        }

        return builder.toString();
    }
    
    /**
     * Merge all lines on inputLines into one String and assign sanitizedInput to it.
     * Assumes inputLines has already been initialized.
     */
    private void buildProcessedInput() {
        StringBuilder builder = new StringBuilder();

        inputLines.stream().forEach(cur -> builder.append(cur + "\n") );

        sanitizedInput = builder.toString();
    }

    private void printTokensToFile() {
        String outputFileName = originalPath.replace(".jack", "_T-Output.txt");

        try {
            File outputFile = new File(outputFileName);
            if (outputFile.exists()) {
                    outputFile.delete();
            }
            outputFile.createNewFile();

            FileWriter writer = new FileWriter(outputFile);
            // System.out.println(sanitizedInput);
            writer.write(sanitizedInput);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Could not write tokens to file, for input file: " + originalPath);
        }

    }
}