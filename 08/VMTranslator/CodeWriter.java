import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {

    private FileWriter outputFileWriter;
    private String currentInputFile;
    private Integer lineCount = 0;
    private String labelPrefix;
    private String currentFunction = "";

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

        if (command == Command.C_PUSH) {
            writePush(segment, index);
        } else {
            writePop(segment, index);
        }

    }

    public void writeInit() {
        // TODO: Implement
        // Writes assembly code that effects the VM initilization, also called bootstrap code.
        // This code must be placed at the beginning of the output file.
    }

    public void writeLabel(String label) {
        // Writes assembly code that effects the label command.

        writeLine("// label " + label);

        String fullLabel = generateLabel(label);
        writeLine("(" + fullLabel + ")");
    }

    public void writeGoto(String label) {
        // Writes assembly code that effects the goto command.

        writeLine("// goto " + label);

        String fullLabel = generateLabel(label);
        writeLine("@" + fullLabel);
        writeLine("0;JMP");
    }

    public void writeIf(String label) {
        // Writes assembly code that effects the if-goto command.

        writeLine("// if-goto " + label);
        
        String fullLabel = generateLabel(label);

        // Pop topmost value on stack
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");

        // Jump if popped value is not zero
        writeLine("@" + fullLabel);
        writeLine("D;JNE");
    }

    public void writeCall(String functionName, int numArgs) {
        // Writes assembly code that effects the call command.

        writeLine("// call " + functionName + " " + numArgs);

        String return_address = generateInternalLabel("RETURN");

        //    push return-address
        writeLine("@" + return_address);
        writeLine("D=A");
        writeLine("@SP");
        writeLine("AM=M+1");  // SP++
        writeLine("A=A-1");  // corrects the "++" from the previous instruction
        writeLine("M=D");

        //    push LCL 
        writeLine("@LCL");
        writeLine("D=M");
        writeLine("@SP");
        writeLine("AM=M+1");
        writeLine("A=A-1");
        writeLine("M=D");

        //    push ARG
        writeLine("@ARG");
        writeLine("D=M");
        writeLine("@SP");
        writeLine("AM=M+1");
        writeLine("A=A-1");
        writeLine("M=D");

        //    push THIS
        writeLine("@THIS");
        writeLine("D=M");
        writeLine("@SP");
        writeLine("AM=M+1");
        writeLine("A=A-1");
        writeLine("M=D");

        //    push THAT
        writeLine("@THAT");
        writeLine("D=M");
        writeLine("@SP");
        writeLine("AM=M+1");
        writeLine("A=A-1");
        writeLine("M=D");

        //    ARG = SP-n-5
        writeLine("@SP");
        writeLine("D=M");
        writeLine("@" + numArgs);
        writeLine("D=D-A");
        writeLine("@5");
        writeLine("D=D-A");
        writeLine("@ARG");
        writeLine("M=D");

        //    LCL = SP
        writeLine("@SP");
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");

        //    goto f
        writeLine("@" + functionName);
        writeLine("0;JMP");

        //  (return-address)
        writeLine("(" + return_address + ")");

    }

    public void writeReturn() {
        // Writes assembly code that effecst the return command.

        writeLine("// return");

        // FRAME = LCL
        writeLine("@LCL");
        writeLine("D=M");
        writeLine("@R13");  // storing temp 'variable' FRAME on R13
        writeLine("M=D");

        // RET = *(FRAME-5)
        writeLine("@5");  // using A for arithmetic
        writeLine("D=D-A");  // D should be loaded with FRAME, as per previous code
        writeLine("A=D");  
        writeLine("D=M");
        writeLine("@R14");  // storing temp 'variable' RET on R14, using A for addressing
        writeLine("M=D");

        // *ARG = pop()
        // popping
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        // storing popped value
        writeLine("@ARG");
        writeLine("A=M");
        writeLine("M=D");

        // SP = ARG+1
        writeLine("@ARG");
        writeLine("D=M+1");
        writeLine("@SP");
        writeLine("M=D");

        // THAT = *(FRAME-1)
        writeLine("@R13");  //recovering FRAME
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@THAT");
        writeLine("M=D");

        // THIS = *(FRAME-2)
        writeLine("@R13");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@THIS");
        writeLine("M=D");

        // ARG = *(FRAME-3)
        writeLine("@R13");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@ARG");
        writeLine("M=D");

        // LCL = *(FRAME-4)
        writeLine("@R13");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");

        // goto RET
        writeLine("@R14");  // recovering RET
        writeLine("A=M");
        writeLine("0;JMP");

    }

    public void writeFunction(String functionName, int numLocals) {
        // Writes assembly code that effects the function command.
        currentFunction = functionName;

        writeLine("// function " + functionName + " " + numLocals);

        // (f)
        writeLine("(" + functionName + ")");

        // not strictly necessary, but avoids generating unnecessary code
        if (numLocals > 0) {    
            // initialization
            writeLine("@SP");
            writeLine("A=M");
            //   repeat k times:
            for (int i = 0; i < numLocals; i++) {
                //   PUSH 0
                writeLine("M=0");
                writeLine("A=A+1");
            }
            // wrapup
            writeLine("D=A");
            writeLine("@SP");
            writeLine("M=D");
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
        if (segment.equals("constant")) {
            // Desired value is already on "index" variable, so just load it onto D
            writeLine("@" + index);
            writeLine("D=A");

        } else if (segment.equals("local")
                    || segment.equals("argument")
                    || segment.equals("this")
                    || segment.equals("that")) {
            // Get pointer to desired segment, add it to index and load memory pointed by result to D 
            String registerName = segmentToRegisterName(segment);
            writeLine("@" + registerName);
            writeLine("D=M");
            writeLine("@" + index);
            writeLine("A=D+A");
            writeLine("D=M");

        } else if (segment.equals("pointer")
                    || segment.equals("temp")) {
            // Fixed positions: pointer starting at 3, temp starting at 5
            
            // Get semantic name of desired register
            String registerName = "";
            if (segment.equals("pointer")) {
                if (index == 0) {
                    registerName = "THIS";
                } else {
                    registerName = "THAT";
                }
            } else {    // segment.equals("temp")
                int correctedIndex = index + 5;
                registerName = "R" + correctedIndex;
            }

            // Load desired value onto D
            writeLine("@" + registerName);
            writeLine("D=M");

        } else if (segment.equals("static")) {
            // Use symbol X.index, where X is the name of the .vm file currently being processed
            writeLine("@" + currentInputFile + "." + index);
            writeLine("D=M");
        }

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

    private void writePop(String segment, int index) {

        // Load memory position to be written on to register R13

        // Handling each segment type with a specific if-block
        if (segment.equals("local")
            || segment.equals("argument")
            || segment.equals("this")
            || segment.equals("that")) {

            // Get pointer to desired segment, add it to index
            String registerName = segmentToRegisterName(segment);
            writeLine("@" + registerName);
            writeLine("D=M");
            writeLine("@" + index);
            writeLine("D=D+A");
            writeLine("@R13");
            writeLine("M=D");

        } else if (segment.equals("pointer")
                    || segment.equals("temp")) {
            // Fixed positions: pointer starting at 3, temp starting at 5
            
            // Get semantic name of desired register
            String registerName = "";
            if (segment.equals("pointer")) {
                if (index == 0) {
                    registerName = "THIS";
                } else {
                    registerName = "THAT";
                }
            } else {    // segment.equals("temp")
                int correctedIndex = index + 5;
                registerName = "R" + correctedIndex;
            }

            // Load memory pos to R13
            writeLine("@" + registerName);
            writeLine("D=A");
            writeLine("@R13");
            writeLine("M=D");

        } else if (segment.equals("static")) {
            // Use symbol X.index, where X is the name of the .vm file currently being processed
            writeLine("@" + currentInputFile + "." + index);
            writeLine("D=A");
            writeLine("@R13");
            writeLine("M=D");
        }

        // General pop logic, applies to any segment
        // Assumes position to be written to is loaded on register R13

        // Go to topmost value on stack and update stack pointer
        writeLine("@SP");
        writeLine("AM=M-1");

        // Load value onto D
        writeLine("D=M");

        // Point towards position to be written
        writeLine("@R13");
        writeLine("A=M");

        // Write popped value
        writeLine("M=D");

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
        String positiveLabel = generateInternalLabel("POSITIVE");
        String endLabel = generateInternalLabel("END");

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

    private String segmentToRegisterName(String segment) {
        String registerName = "";

        switch (segment) {
            case "local":
                registerName = "LCL";
                break;
            case "argument":
                registerName = "ARG";
                break;
            case "this":
                registerName = "THIS";
                break;
            case "that":
                registerName = "THAT";
                break;
        }

        return registerName;
    }

    private String generateInternalLabel(String description) {
        // Generates labels needed by the translation process, not present in the VM code itself
        String newLabel = labelPrefix + "_" + lineCount + "_" + description;
        return newLabel;
    }

    private String generateLabel(String label) {
        // Generates labels that are present in the VM code
        String newLabel = this.currentFunction + "$" + label;
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
