import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private String outputPath;
    private StringBuilder xmlBuilder;
    private int level;

    public CompilationEngine(JackTokenizer inputTokenizer, String outputFilePath) {
        this.tokenizer = inputTokenizer;
        this.outputPath = outputFilePath;
        this.xmlBuilder = new StringBuilder();
        this.level = 0;

        this.tokenizer.advance();
    }

    public void compileClass() {
        xmlBuilder.append("<class>");
        
        this.level++;
        indent();

        Token classT = eat("class");
        xmlBuilder.append(classT.xmlRepresentation() + "\n");

        Token classNameT = eatIdentifier();
        xmlBuilder.append(classNameT.xmlRepresentation() + "\n");

        Token curlyT = eat("{");
        xmlBuilder.append(curlyT.xmlRepresentation() + "\n");

        Token next = eatAny();
        while (JackGrammar.startsVarDeclaration(next)) {
            compileClassVarDec();
            next = eatAny();
        }

        while (JackGrammar.startsSubroutineDeclaration(next)) {
            compileSubroutine();
            next = eatAny();
        }

        Token closeCurlyT = eat("}");
        xmlBuilder.append(closeCurlyT.xmlRepresentation() + "\n");

        this.level--;
        indent();

        xmlBuilder.append("</class>");

        if (!tokenizer.hasMoreTokens()) writeToXmlFile();
    }

    public void compileClassVarDec() {
        // TODO: Implement
    }

    public void compileSubroutine() {
        // TODO: Implement
    }

    public void compileParameterList() {
        // TODO: Implement
    }

    public void compileVarDec() {
        // TODO: Implement
    }

    public void compileStatements() {
        // TODO: Implement
    }

    public void compileDo() {
        // TODO: Implement
    }

    public void compileLet() {
        // TODO: Implement
    }

    public void compileWhile() {
        // TODO: Implement
    }

    public void compileReturn() {
        // TODO: Implement
    }

    public void compileIf() {
        // TODO: Implement
    }

    public void compileExpression() {
        // TODO: Implement
    }

    public void compileTerm() {
        // TODO: Implement
    }

    public void compileExpressionList() {
        // TODO: Implement
    }

    private Token eat(String expected) {
        Token eaten = tokenizer.getCurrent();
        if (eaten.getRepresentation().equals(expected)) {
            tokenizer.advance();
        } else {
            throw new RuntimeException("Expected to eat '" + expected + "', but found: '" + eaten.getRepresentation() + "'.");
        }
        return eaten;
    }

    private Token eatAny() {
        Token eaten = tokenizer.getCurrent();
        tokenizer.advance();
        return eaten;
    }

    private Token eatIdentifier() {
        Token eaten = tokenizer.getCurrent();
        if (eaten.getType() == TokenTypes.IDENTIFIER) {
            tokenizer.advance();
        } else {
            throw new RuntimeException("Expected to eat an identifier, but found: '" + eaten.getType().toString() + "'.");
        }
        return eaten;
    }

    private void indent() {
        for (int i = 0; i < level; i++) {
            xmlBuilder.append("\t");
        }
    }

    private void writeToXmlFile() {
        try {
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileWriter writer = new FileWriter(outputFile);
            writer.write(xmlBuilder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not write analyzed xml, for output file: " + outputPath);
        }
    }

    
}