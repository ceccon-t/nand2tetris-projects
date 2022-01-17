import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {

    private String outputPath;
    private StringBuilder output;

    public VMWriter(String outputPath) {
        this.outputPath = outputPath;
        this.output = new StringBuilder();
    }

    public void writePush(Segment seg, Integer index) {
        String command = "push " + segmentString(seg) + " " + index;
        appendLine(command);
    }

    public void writePop(Segment seg, Integer index) {
        String command = "pop " + segmentString(seg) + " " + index;
        appendLine(command);
    }

    public void writeArithmetic(VMCommand command) {
        String commandStr = commandString(command);
        appendLine(commandStr);
    }

    public void writeLabel(String label) {
        String command = "label " + label;
        appendLine(command);
    }

    public void writeGoto(String label) {
        String command = "goto " + label;
        appendLine(command);
    }

    public void writeIf(String label) {
        String command = "if-goto " + label;
        appendLine(command);
    }

    public void writeCall(String name, Integer nArgs) {
        String command = "call " + name + " " + nArgs;
        appendLine(command);
    }

    public void writeFunction(String name, Integer nLocals) {
        String command = "function " + name + " " + nLocals;
        appendLine(command);
    }

    public void writeReturn() {
        String command = "return";
        appendLine(command);
    }

    /**
     * Finishes the generation of output.
     * Since the output vm code is kept in memory, this method opens the output file,
     *   writes to it, then closes it.
     * No output is written to disk until this method is called.
     */
    public void close() {
        try {
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileWriter writer = new FileWriter(outputFile);
            writer.write(output.toString());
            writer.flush();
            writer.close();
        } catch(IOException e) {
            throw new RuntimeException("Could not write to output file: " + outputPath);
        }
    }

    /**
     * Appends a line to the output being created in memory
     */
    private void appendLine(String line) {
        output.append(line + "\n");
    }

    /**
     * Maps commands from enum to the relevant string representation on code
     */
    private String commandString(VMCommand command) {
        String str = "";

        switch(command) {
            case ADD:
                str = "add";
                break;
            case SUB:
                str = "sub";
                break;
            case NEG:
                str = "neg";
                break;
            case EQ:
                str = "eq";
                break;
            case GT:
                str = "gt";
                break;
            case LT:
                str = "lt";
                break;
            case AND:
                str = "and";
                break;
            case OR:
                str = "or";
                break;
            case NOT:
                str = "not";
                break;
        }

        return str;
    }

    /**
     * Maps segments from enum to the relevant string representation on code
     */
    private String segmentString(Segment segment) {
        String str = "";

        switch(segment) {
            case CONST:
                str = "constant";
                break;
            case ARG:
                str = "argument";
                break;
            case LOCAL:
                str = "local";
                break;
            case STATIC:
                str = "static";
                break;
            case THIS:
                str = "this";
                break;
            case THAT:
                str = "that";
                break;
            case POINTER:
                str = "pointer";
                break;
            case TEMP:
                str = "temp";
                break;
        }

        return str;
    }

}
