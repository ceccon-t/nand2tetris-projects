import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private String outputPath;
    private StringBuilder xmlBuilder;
    private int indentLevel;

    public CompilationEngine(JackTokenizer inputTokenizer, String outputFilePath) {
        this.tokenizer = inputTokenizer;
        this.outputPath = outputFilePath;
        this.xmlBuilder = new StringBuilder();
        this.indentLevel = 0;

        this.tokenizer.advance();
    }

    public void compileClass() {
        appendXmlIndentedLine("<class>");
        
        this.indentLevel++;

        Token classT = eat("class");
        appendXmlIndentedLine(classT.xmlRepresentation());

        Token classNameT = eatIdentifier();
        appendXmlIndentedLine(classNameT.xmlRepresentation());

        Token curlyT = eat("{");
        appendXmlIndentedLine(curlyT.xmlRepresentation());

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
        appendXmlIndentedLine(closeCurlyT.xmlRepresentation());

        this.indentLevel--;

        appendXmlIndentedLine("</class>");

        if (!tokenizer.hasMoreTokens()) writeToXmlFile();
    }

    public void compileClassVarDec() {
        appendXmlIndentedLine("<classVarDec>");

        this.indentLevel++;

        Token scopeT = eatAny();
        if (!JackGrammar.declaresScope(scopeT)) {
                throw new RuntimeException("SYNTAX ERROR! Expected 'static' or 'field', but found '" + scopeT.getRepresentation() + "'");
        }
        appendXmlIndentedLine(scopeT.xmlRepresentation());

        Token typeT = eatAny();
        if ( !JackGrammar.indicatesType(typeT) ) {
            throw new RuntimeException("SYNTAX ERROR! Expected a type (int|char|boolean|className), but found '" + scopeT.getRepresentation() + "'");
        }
        appendXmlIndentedLine(typeT.xmlRepresentation());


        Token varNameT = eatIdentifier();
        appendXmlIndentedLine(varNameT.xmlRepresentation());

        // Parsing (',' varName)*
        Token nextCommaT, nextVarNameT;
        while (tokenizer.getCurrent().getRepresentation().equals(",")) {
            nextCommaT = eat(",");
            appendXmlIndentedLine(nextCommaT.xmlRepresentation());

            nextVarNameT = eatIdentifier();
            appendXmlIndentedLine(nextVarNameT.xmlRepresentation());
        }

        Token semicolonT = eat(";");
        appendXmlIndentedLine(semicolonT.xmlRepresentation());
        
        this.indentLevel--;

        appendXmlIndentedLine("</classVarDec>");
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
        for (int i = 0; i < indentLevel; i++) {
            xmlBuilder.append("\t");
        }
    }

    private void appendXmlIndentedLine(String content) {
        indent();
        xmlBuilder.append(content + "\n");
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