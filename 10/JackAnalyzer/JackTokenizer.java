import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JackTokenizer {

    private String originalPath;

    private List<String> inputLines;
    private String sanitizedInput;
    private List<Token> tokens;

    private int currentPos;


    public JackTokenizer(String inputFilePath) {
        originalPath = inputFilePath;

        try {
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

        // The call below generates the xml of the tokens, to test the first part of Project 10
        // printTokensToFile();

        currentPos = -1;  // "Initially there is no current token", as per specification

    }

    public Boolean hasMoreTokens() {
        return currentPos < (tokens.size()-1);
    }

    public void advance() {
        currentPos++;
    }

    public TokenTypes tokenType() {
        return tokens.get(currentPos).getType();
    }

    public Keywords keyWord() {
        return JackGrammar.getKeywordEnum(tokens.get(currentPos));
    }

    public Character symbol() {
        return tokens.get(currentPos).getRepresentation().charAt(0);
    }

    public String identifier() {
        return tokens.get(currentPos).getRepresentation();
    }

    public Integer intVal() {
        return Integer.valueOf(tokens.get(0).getRepresentation());
    }

    public String stringVal() {
        return tokens.get(currentPos).getRepresentation();
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
                i = j-1;
            }

            // If is symbol, add symbol as token
            else if (JackGrammar.isSymbol(sanitizedInput.charAt(i))) {
                tokens.add(new Token(sanitizedInput.substring(i, i+1)));
            }

            // If is double quote, find end of string constant
            else if (sanitizedInput.charAt(i) == '"') { 
                j = i+1; 
                // Find closing double quote
                while (sanitizedInput.charAt(j) != '"') {
                    j++; 
                }

                tokens.add(new Token(sanitizedInput.substring(i, j+1)));
                i = j;
            }

            // If is digit, find end of integer constant
            else if (Character.isDigit(sanitizedInput.charAt(i))) {
                j = i+1;
                // Find end of digit sequence
                while ( Character.isDigit(sanitizedInput.charAt(j)) && j < end) {
                    j++;
                }

                tokens.add(new Token(sanitizedInput.substring(i, j)));
                i = j-1;
            }

            // If is letter or underscore, find end of word (which can be either keyword or identifier)
            else if (JackGrammar.startsWord(sanitizedInput.charAt(i))) {
                j = i+1;
                // Find end of word
                while ( !JackGrammar.breaksWord(sanitizedInput.charAt(j))) {
                    j++;
                }

                tokens.add(new Token(sanitizedInput.substring(i, j)));
                i = j-1;
            }
            
            // Move on
            i++;
        }

    }

    private void printTokensToFile() {
        String outputFileNameXml = originalPath.replace(".jack", "_T-Output.xml");

        try {
            // Code below can be useful to debug the removal of comments:

            // // Create txt file with sanitized code
            // String outputFileNameTxt = originalPath.replace(".jack", "_WithoutComments.txt");
            // File outputFile = new File(outputFileNameTxt);
            // if (outputFile.exists()) {
            //     outputFile.delete();
            // }
            // outputFile.createNewFile();

            // FileWriter writer = new FileWriter(outputFile);
            // writer.write(sanitizedInput);
            // writer.flush();
            // writer.close();

            // // END OF - Create txt file with sanitized code

            // Create xml file with tokens
            File outputFileXml = new File(outputFileNameXml);
            if (outputFileXml.exists()) {
                outputFileXml.delete();
            }
            FileWriter xmlWriter = new FileWriter(outputFileXml);
            xmlWriter.write("<tokens>\n");
            for (Token token : tokens) {
                xmlWriter.append(token.xmlRepresentation() + "\n");
            }
            xmlWriter.append("</tokens>\n");
            xmlWriter.flush();
            xmlWriter.close();

        } catch (IOException e) {
            throw new RuntimeException("Could not write tokens to file, for input file: " + originalPath);
        }

    }
}