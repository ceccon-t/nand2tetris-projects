import java.io.File;
import java.util.ArrayList;
import java.util.List;

class VMTranslator {

    private static final String VM_FILE_EXTENSION = ".vm";
    private static final String TARGET_FILE_EXTENSION = ".asm";

    public static void main(String[] args) throws Exception {
        System.out.println("Starting translation...");

        if (args.length < 1) throw new Exception("ERROR! No source specified. Usage: '$ VMTranslator <source>'");

        String source = args[0];

        List<String> toProcess = new ArrayList<>();

        if (isVmFile(source)) {
            toProcess.add(source);
        } else {
            File directory = new File(source);
            if (!directory.isDirectory()) throw new Exception("ERROR! Source must either be a .vm file or a directory!");

            String inputFolderPath = normalizeInputFolderPath(source);

            File[] filesInFolder = directory.listFiles();
            for (File file : filesInFolder) {
                String name = file.getName();
                if (isVmFile(name)) {
                    toProcess.add(inputFolderPath + name);
                }
            }
        }

        System.out.println("\nTranslating following files:");
        for (String filename : toProcess) {
            System.out.println(filename);
        }

        String targetFile = generateTargetFilename(source);

        System.out.println("\nTarget file for translation is " + targetFile + "\n");
        CodeWriter writer = new CodeWriter(targetFile);

        for (String filepath: toProcess) { 
            System.out.println("Translating file " + filepath + "...");
            Parser currentParser = new Parser(filepath);

            writer.setFileName(getActualFilename(filepath));

            while (currentParser.hasMoreCommands()) {
                currentParser.advance();

                Command currentCommand = currentParser.commandType();

                if (currentCommand == Command.C_ARITHMETIC) {
                    writer.writeArithmetic(currentParser.arg1());
                } else if (currentCommand == Command.C_PUSH
                            || currentCommand == Command.C_POP) {
                    writer.writePushPop(currentCommand, currentParser.arg1(), currentParser.arg2());
                } else if (currentCommand == Command.C_LABEL) {
                    writer.writeLabel(currentParser.arg1());
                } else if (currentCommand == Command.C_GOTO) {
                    writer.writeGoto(currentParser.arg1());
                } else if (currentCommand == Command.C_IF) {
                    writer.writeIf(currentParser.arg1());
                }
            }
        }

        writer.close();

    }

    private static boolean isVmFile(String path) {
        return path.endsWith(VM_FILE_EXTENSION);
    }

    private static String generateTargetFilename(String source) {
        String target = "";

        if (isVmFile(source)) {
            int size = source.length();
            int prefixSize = size - VM_FILE_EXTENSION.length();
            target = source.substring(0, prefixSize) + TARGET_FILE_EXTENSION;
        } else {
            String inputFolderPath = normalizeInputFolderPath(source);
            inputFolderPath = inputFolderPath.substring(0, inputFolderPath.length()-1); // facilitates getting dir name

            int lastSeparator = inputFolderPath.lastIndexOf("/");
            String dirName = inputFolderPath.substring(lastSeparator+1);
            target = inputFolderPath + "/" + dirName + TARGET_FILE_EXTENSION;
        }

        return target;
    }

    private static String normalizeInputFolderPath(String folderPath) {
        String path = "";

        if (folderPath.endsWith("/")) {
            path = folderPath + "";
        } else {
            path = folderPath + "/";
        }

        return path;
    }

    private static String getActualFilename(String path) {
        String actual = path;
        if (actual.contains("/")) {
            int lastIndex = actual.lastIndexOf("/");
            actual = path.substring(lastIndex+1);
        }
        return actual;
    }

}

