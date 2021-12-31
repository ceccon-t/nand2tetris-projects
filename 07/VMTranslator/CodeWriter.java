import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {

    private FileWriter outputFileWriter;
    private String currentInputFile;
    private Integer lineCount = 0;
    private String labelPrefix;

    public CodeWriter(String outputFileName) {
        try {
            File outputFile = new File(outputFileName);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            outputFileWriter = new FileWriter(outputFileName);

            labelPrefix = outputFileName
                            .replace("/", "")
                            .replace(".asm", "")
                            .toUpperCase();
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
        writeLine("// " + command);

        // TODO: write hack code to output file
        // if (command.equals("add")
        //     || command.equals("sub")
        //     || command.equals("and")
        //     || command.equals("or")) {
        //     writeBinaryNumericalOperation(command);
        // } 
        switch(command) {
            case "add":
            case "sub":
            case "and":
            case "or":
                writeBinaryNumericalOperation(command);
                break;
            case "neg":
            case "not":
                writeUnaryOperation(command);
                break;
            case "eq":
            case "gt":
            case "lt":
                writeComparisonOperation(command);
                break;
        }
    }

    public void writePushPop(Command command, String segment, int index) {
        writeLine("// " 
                    + commandToCommentRepresentation(command)
                    + " " + segment
                    + " " + index);

        // TODO: write hack code to output file
        if (command == Command.C_PUSH) {
            writePush(segment, index);
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
        writeLine("@" + index);
        writeLine("D=A");

        // General push logic, applies to any segment
        // Assumes desired value is loaded onto D register

        // Go to next pos on stack
        writeLine("@SP");
        writeLine("A=M");

        // Push value onto stack
        writeLine("M=D");

        // Increment stack pointer
        writeLine("@SP");
        writeLine("M=M+1");

    }

    private void writeBinaryNumericalOperation(String operation) {
        // Get value on top of stack
        writeLine("@SP");
        writeLine("A=M-1");
        writeLine("D=M");

        // Focus on next value on the stack
        writeLine("A=A-1");

        // Do the operation and save to the top of stack
        switch(operation) {
            case "add":
                writeLine("M=M+D");
                break;
            case "sub":
                writeLine("M=M-D");
                break;
            case "and":
                writeLine("M=M&D");
                break;
            case "or":
                writeLine("M=M|D");
                break;
        }

        // Move stack pointer to next pos
        writeLine("D=A+1");
        writeLine("@SP");
        writeLine("M=D");

    }

    private void writeUnaryOperation(String operation) {
        // Go to top-most value of stack
        writeLine("@SP");
        writeLine("A=M-1");

        // Aplly operation substituting value in place
        switch(operation) {
            case "neg":
                writeLine("M=-M");
                break;
            case "not":
                writeLine("M=!M");
                break;
        }
    }

    private void writeComparisonOperation(String operation) {
        // Get value on top of stack
        writeLine("@SP");
        writeLine("A=M-1");
        writeLine("D=M");

        // Focus on next value on the stack
        writeLine("A=A-1");

        // Make comparison and store result on D
        writeLine("D=M-D");

        // Generate labels
        String positiveLabel = generateLabel("POSITIVE");
        String endLabel = generateLabel("END");

        // Branch based on comparison
        writeLine("@" + positiveLabel);
        switch(operation) {
            case "eq":
                writeLine("D;JEQ");
                break;
            case "gt":
                writeLine("D;JGT");
                break;
            case "lt":
                writeLine("D;JLT");
                break;
        }

        // Handle negative case
        writeLine("D=0");   // To VM, False=0
        // Go to the end of the branching
        writeLine("@" + endLabel);
        writeLine("0;JMP");

        // Handle positive case
        writeLine("(" + positiveLabel + ")"); // Labeling
        writeLine("D=-1");  // To VM, True=-1

        // End of brancing
        writeLine("(" + endLabel + ")"); // Labeling
        
        // Write result to stack and update stack pointer

        //   Go to correct position to place result
        writeLine("@SP");
        writeLine("A=M-1");
        writeLine("A=A-1"); // Will remove the 2 topmost values

        //   Write result to stack
        writeLine("M=D");

        //   Update stack pointer
        writeLine("@SP");
        writeLine("M=M-1");

    }

    private void writeLine(String line) {
        try {
            outputFileWriter.write(line + "\n");
            lineCount++;
        } catch (IOException e) {
            handleException(e);
        }
    }

    private String generateLabel(String description) {
        String newLabel = labelPrefix + "_" + lineCount + "_" + description;
        return newLabel;
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
