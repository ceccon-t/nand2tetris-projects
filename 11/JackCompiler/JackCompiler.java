import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JackCompiler {

    private static final String JACK_FILE_EXTENSION = ".jack";
    private static final String TARGET_FILE_EXTENSION = ".xml";
    
    public static void main(String[] args) {
        System.out.println("Starting compilation...");

        if (args.length < 1) throw new RuntimeException("ERROR! No source specified. Usage: '$ JackCompiler <source>'");

        String source = args[0];

        List<String> toProcess = new ArrayList<>();
        

        if (isJackFile(source)) {
            toProcess.add(source);
        } else {
            File directory = new File(source);
            if (!directory.isDirectory()) throw new RuntimeException("ERROR! Source must either be a .jack file or a directory!");

            String inputFolderPath = normalizeInputFolderPath(source);

            File[] filesInFolder = directory.listFiles();
            for (File file : filesInFolder) {
                String name = file.getName();
                if (isJackFile(name)) {
                    toProcess.add(inputFolderPath + name);
                }
            }
        }

        System.out.println("\nCompiling following files:");
        for (String filename : toProcess) {
            System.out.println(filename);
        }

        for (String filepath : toProcess) {
            String targetFile = generateTargetFilename(source);

            System.out.println("Compiling file " + filepath + ", target: " + targetFile);
            JackTokenizer tokenizer = new JackTokenizer(filepath);
            CompilationEngine engine = new CompilationEngine(tokenizer, filepath.replace(".jack", "-Output.xml"));
            engine.compileClass();
        }
    }

    private static boolean isJackFile(String path) {
        return path.endsWith(JACK_FILE_EXTENSION);
    }

    private static String generateTargetFilename(String source) {
        String target = "";

        if (isJackFile(source)) {
            int size = source.length();
            int prefixSize = size - JACK_FILE_EXTENSION.length();
            target = source.substring(0, prefixSize) + TARGET_FILE_EXTENSION;
        } else {
            String inputFolderPath = normalizeInputFolderPath(source);
            inputFolderPath = inputFolderPath.substring(0, inputFolderPath.length()-1);
            
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