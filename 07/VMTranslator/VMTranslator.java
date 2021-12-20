import java.io.File;
import java.util.ArrayList;
import java.util.List;

class VMTranslator {

    private static final String VM_FILE_EXTENSION = ".vm";

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

            File[] filesInFolder = directory.listFiles();
            for (File file : filesInFolder) {
                String name = file.getName();
                if (isVmFile(name)) {
                    toProcess.add(source + name);
                }
            }
        }

        System.out.println("\nTranslating following files:");
        for (String filename : toProcess) {
            System.out.println(filename);
        }

    }

    private static boolean isVmFile(String path) {
        return path.endsWith(VM_FILE_EXTENSION);
    }

}

