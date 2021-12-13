import java.io.File;

class VMTranslator {

    public static void main(String[] args) throws Exception {
        System.out.println("Hello!");

        if (args.length < 1) throw new Exception("ERROR! No source specified. Usage: '$ VMTranslator <source>'");

        String source = args[0];

        File sourceFile = new File(source);
        if (sourceFile.exists() && !sourceFile.isDirectory()) {
            System.out.println("File exists");
        }
    }

}

