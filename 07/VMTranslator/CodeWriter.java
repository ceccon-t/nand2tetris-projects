import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {

    private FileWriter outputFileWriter;
    private String currentInputFile;

    public CodeWriter(String outputFileName) {
        try {
            File outputFile = new File(outputFileName);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            outputFileWriter = new FileWriter(outputFileName);
        } catch (IOException e) {
            handleException(e);
        }
    }

    public void setFileName(String fileName) {
        currentInputFile = fileName;

        try {
            outputFileWriter.write("// " + currentInputFile + "\n");
        } catch (IOException e) {
            handleException(e);
        }
    }

    public void writeArithmetic(String command) {
        try {
            outputFileWriter.write("// " + command + "\n");

            // TODO: write hack code to output file
        } catch (IOException e) {
            handleException(e);
        }
    }

    public void writePushPop(Command command, String segment, int index) {
        try {
            outputFileWriter.write("// " 
                                    + commandToCommentRepresentation(command)
                                    + " " + segment
                                    + " " + index
                                    + "\n");

            // TODO: write hack code to output file
        } catch (IOException e) {
            handleException(e);
        }

    }

    public void close() {
        try {
            outputFileWriter.close();
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e.getMessage());
    }

    private String commandToCommentRepresentation(Command command) {
        String representation = "";

        switch (command) {
            case C_PUSH:
                representation = "push";
                break;
            case C_POP:
                representation = "pop";
                break;
            default:
                representation = "Unknown command";
        }

        return representation;
    }
    
}
