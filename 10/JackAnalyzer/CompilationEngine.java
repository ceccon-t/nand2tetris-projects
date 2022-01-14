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
        xmlBuilder.append("<class>\n");
        
        this.level++;

        Token classT = eat("class");
        indent();
        xmlBuilder.append(classT.xmlRepresentation() + "\n");

        Token classNameT = eatIdentifier();
        indent();
        xmlBuilder.append(classNameT.xmlRepresentation() + "\n");

        Token curlyT = eat("{");
        indent();
        xmlBuilder.append(curlyT.xmlRepresentation() + "\n");

        Token next = tokenizer.getCurrent();
        while (JackGrammar.startsVarDeclaration(next)) {
            compileClassVarDec();
            next = tokenizer.getCurrent();
        }

        writeToXmlFile();
        
        System.out.println("ATTENTION: Process will enter an infinite loop now, as the remaining functionality is not finished. Kill the process and check the xml output.");

        while (JackGrammar.startsSubroutineDeclaration(next)) {
            compileSubroutine();
            next = tokenizer.getCurrent();
        }

        Token closeCurlyT = eat("}");
        xmlBuilder.append(closeCurlyT.xmlRepresentation() + "\n");

        this.level--;
        indent();

        xmlBuilder.append("</class>");

        if (!tokenizer.hasMoreTokens()) writeToXmlFile();
    }

    public void compileClassVarDec() {
        indent();
        xmlBuilder.append("<classVarDec>\n");

        this.level++;

        Token scopeT = eatAny();
        if (!scopeT.getRepresentation().equals("static") 
            && !scopeT.getRepresentation().equals("field")) {
                throw new RuntimeException("SYNTAX ERROR! Expected 'static' or 'field', but found '" + scopeT.getRepresentation() + "'");
        }
        indent(); 
        xmlBuilder.append(scopeT.xmlRepresentation() + "\n");

        Token typeT = eatAny();
        if (
            !typeT.getRepresentation().equals("int")
            && !typeT.getRepresentation().equals("char")
            && !typeT.getRepresentation().equals("boolean")
            && !typeT.getType().equals(TokenTypes.IDENTIFIER)
        ) {
            throw new RuntimeException("SYNTAX ERROR! Expected a type (int|char|boolean|className), but found '" + scopeT.getRepresentation() + "'");
        }
        indent();
        xmlBuilder.append(typeT.xmlRepresentation() + "\n");


        Token varNameT = eatIdentifier();
        indent();
        xmlBuilder.append(varNameT.xmlRepresentation() + "\n");

        // Parsing (',' varName)*
        Token nextCommaT, nextVarNameT;
        while (tokenizer.getCurrent().getRepresentation().equals(",")) {
            nextCommaT = eat(",");

            indent();
            xmlBuilder.append(nextCommaT.xmlRepresentation() + "\n");

            nextVarNameT = eatIdentifier();
            indent();
            xmlBuilder.append(nextVarNameT.xmlRepresentation() + "\n");
        }

        Token semicolonT = eat(";");
        indent();
        xmlBuilder.append(semicolonT.xmlRepresentation() + "\n");
        
        this.level--;

        indent();
        xmlBuilder.append("</classVarDec>\n");
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