import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JackTokenizer {

    // private String rawInput;

    private String originalPath;

    private List<String> inputLines;
    private String sanitizedInput;
    private List<Token> tokens;


    public JackTokenizer(String inputFilePath) {
        originalPath = inputFilePath;

        try {
            // rawInput = Files.readString(Path.of(inputFilePath));

            inputLines = Files.lines(Paths.get(inputFilePath), StandardCharsets.UTF_8)
                            .map(s -> removeInlineComments(s))
                            .filter(s -> !isEmptyLine(s))
                            .collect(Collectors.toList());

        } catch(IOException e) {
            throw new RuntimeException("Could not read from input file: " + inputFilePath);
        }

        buildProcessedInput();

        sanitizedInput = removeMultilineComments(sanitizedInput);

        parseTokens();

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

    /**
     * Walk through sanitized input parsing existing tokens and adding them to 'tokens' variable.
     * Assumes sanitizedInput has already been initialized correctly and cleansed of comments.
     */
    private void parseTokens() {
        tokens = new ArrayList<Token>();

        int i = 0, j;
        int end = sanitizedInput.length();

        // Walk through sanitized input using variables i and j as index
        while (i < end) {

            // If is whitespace, eat all adjacent whitespaces
            if (Character.isWhitespace(sanitizedInput.charAt(i))) {
                j = i+1;
                while (Character.isWhitespace(sanitizedInput.charAt(j)) && j < end) {
                    j++;
                }
                i = j;
            }

            // If is symbol, add symbol as token
            else if (JackGrammar.isSymbol(sanitizedInput.charAt(i))) {
                tokens.add(new Token(sanitizedInput.substring(i, i+1)));
            }

            // If is double quote, find end of string constant

            // If is digit, find end of integer constant

            // If is letter or underscore, find end of word (which can be either keyword or identifier)
            
            // Move on
            i++;
        }

    }
    
    private void printTokensToFile() {
        String outputFileNameTxt = originalPath.replace(".jack", "_T-Output.txt");
        String outputFileNameXml = originalPath.replace(".jack", "_T-Output.xml");

        try {
            // Create txt file with sanitized code
            File outputFile = new File(outputFileNameTxt);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            FileWriter writer = new FileWriter(outputFile);
            // System.out.println(sanitizedInput);
            writer.write(sanitizedInput);
            writer.flush();
            writer.close();

            // Create xml file with tokens
            File outputFileXml = new File(outputFileNameXml);
            if (outputFileXml.exists()) {
                outputFileXml.delete();
            }
            FileWriter xmlWriter = new FileWriter(outputFileXml);
            xmlWriter.write("<tokens>\n");
            for (Token token : tokens) {
                xmlWriter.append("\t<token>" + token.getRepresentation() + "</token>\n");
            }
            xmlWriter.append("</tokens>\n");
            xmlWriter.flush();
            xmlWriter.close();

        } catch (IOException e) {
            throw new RuntimeException("Could not write tokens to file, for input file: " + originalPath);
        }

    }
}