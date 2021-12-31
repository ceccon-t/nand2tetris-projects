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
            if (command.equals("add")) {
                writeAdd();
            }
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
            if (command == Command.C_PUSH) {
                writePush(segment, index);
            } 
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

    private void writePush(String segment, int index) {

        // Load value onto D register (assuming constant segment for now)
        try {
            outputFileWriter.write("@" + index + "\n");
            outputFileWriter.write("D=A" + "\n");
        } catch (IOException e) {
            handleException(e);
        }

        // General push logic, applies to any segment
        // Assumes desired value is loaded onto D register
        try {
            // Go to next pos on stack
            outputFileWriter.write("@SP" + "\n");
            outputFileWriter.write("A=M" + "\n");

            // Push value onto stack
            outputFileWriter.write("M=D" + "\n");

            // Increment stack pointer
            outputFileWriter.write("@SP" + "\n");
            outputFileWriter.write("M=M+1" + "\n");


        } catch (IOException e) {
            handleException(e);
        }

    }

    private void writeAdd() {
        try {
            // Get value on top of stack
            outputFileWriter.write("@SP" + "\n");
            outputFileWriter.write("A=M-1" + "\n");
            outputFileWriter.write("D=M" + "\n");

            // Focus on next value on the stack
            outputFileWriter.write("A=A-1" + "\n");

            // Do the Add
            outputFileWriter.write("D=M+D" + "\n");

            // Write result to stack
            outputFileWriter.write("M=D" + "\n");

            // Move stack pointer to next pos
            outputFileWriter.write("D=A+1" + "\n");
            outputFileWriter.write("@SP" + "\n");
            outputFileWriter.write("M=D" + "\n");


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
