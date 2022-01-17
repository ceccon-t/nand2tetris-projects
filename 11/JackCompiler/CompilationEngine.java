import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private String outputPath;
    private SymbolTable symbolTable;
    private VMWriter vmWriter;
    private StringBuilder xmlBuilder;
    private int indentLevel;

    public CompilationEngine(JackTokenizer inputTokenizer, String outputFilePath) {
        this.tokenizer = inputTokenizer;
        this.outputPath = outputFilePath;
        this.indentLevel = 0;

        this.symbolTable = new SymbolTable();
        this.vmWriter = new VMWriter(this.outputPath);
        this.xmlBuilder = new StringBuilder();

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
        while (JackGrammar.startsClassVarDeclaration(next)) {
            compileClassVarDec();
            next = tokenizer.getCurrent();
        }

        while (JackGrammar.startsSubroutineDeclaration(next)) {
            compileSubroutine();
            next = tokenizer.getCurrent();
        }

        Token closeCurlyT = eat("}");
        appendXmlIndentedLine(closeCurlyT.xmlRepresentation());

        this.indentLevel--;

        appendXmlIndentedLine("</class>");

        if (!tokenizer.hasMoreTokens()) {
            vmWriter.close();
            writeToXmlFile();
        }
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
            throw new RuntimeException("SYNTAX ERROR! Expected a type (int|char|boolean|className), but found '" + typeT.getRepresentation() + "'");
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
        appendXmlIndentedLine("<subroutineDec>");
        this.indentLevel++;

        // compilation logic

        // Parsing ( 'constructor' | 'function' | 'method' )
        Token subroutineTypeT = eatAny();
        if (!JackGrammar.startsSubroutineDeclaration(subroutineTypeT)) {
            throw new RuntimeException("SYNTAX ERROR! Expected declaration of subroutine type, but instead found '" + subroutineTypeT.getRepresentation() + "'");
        }
        appendXmlIndentedLine(subroutineTypeT.xmlRepresentation());

        // Parsing ( 'void' | type )
        Token returnTypeT = eatAny();
        if ( !JackGrammar.indicatesSubroutineReturnType(returnTypeT) ) {
            throw new RuntimeException("SYNTAX ERROR! Expected declaration of return type of subroutine, but instead found '" + returnTypeT.getRepresentation() + "'");
        }
        appendXmlIndentedLine(returnTypeT.xmlRepresentation());

        // Parsing subroutineName
        Token subroutineNameT = eatIdentifier();
        appendXmlIndentedLine(subroutineNameT.xmlRepresentation());

        // Parsing '('
        Token openParenT = eat("(");
        appendXmlIndentedLine(openParenT.xmlRepresentation());

        // Parsing parameterList
        compileParameterList();

        // Parsing ')'
        Token closeParenT = eat(")");
        appendXmlIndentedLine(closeParenT.xmlRepresentation());

        // Parsing subroutineBody
            appendXmlIndentedLine("<subroutineBody>");
            this.indentLevel++;

            // Parsing '{'
            Token openCurlyT = eat("{");
            appendXmlIndentedLine(openCurlyT.xmlRepresentation());

            // Parsing varDec*
            while (JackGrammar.startsLocalVariableDeclaration(tokenizer.getCurrent())) {
                compileVarDec();
            }

            // Parsing statements
            compileStatements();

            // Parsing '}'
            Token closeCurlyT = eat("}");
            appendXmlIndentedLine(closeCurlyT.xmlRepresentation());

            this.indentLevel--;
            appendXmlIndentedLine("</subroutineBody>");


        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</subroutineDec>");
    }

    public void compileParameterList() {
        appendXmlIndentedLine("<parameterList>");
        this.indentLevel++;

        if (JackGrammar.indicatesType(tokenizer.getCurrent())) {
            // compilation logic
    
            // Parsing type
            Token typeT = eatAny();
            if (!JackGrammar.indicatesType(typeT)) { // already checked above, but safe-proofing in case of moving code around later
                throw new RuntimeException("SYNTAX ERROR! Expected a type indication, but instead found '" + typeT.getRepresentation() + "'");
            }
            appendXmlIndentedLine(typeT.xmlRepresentation());
    
            // Parsing varName
            Token varNameT = eatIdentifier();
            appendXmlIndentedLine(varNameT.xmlRepresentation());
    
            // Parsing (',' type varName)*
            Token commaT, nextTypeT, nextVarNameT;
            while (tokenizer.getCurrent().getRepresentation().equals(",")) {
                commaT = eat(",");
                appendXmlIndentedLine(commaT.xmlRepresentation());
    
                nextTypeT = eatAny();
                if (!JackGrammar.indicatesType(nextTypeT)) {
                    throw new RuntimeException("SYNTAX ERROR! Expected a type indication, but instead found '" + nextTypeT.getRepresentation() + "'");
                }
                appendXmlIndentedLine(nextTypeT.xmlRepresentation());
    
                nextVarNameT = eatIdentifier();
                appendXmlIndentedLine(nextVarNameT.xmlRepresentation());
            }
    
        }
        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</parameterList>");
    }

    public void compileVarDec() {
        appendXmlIndentedLine("<varDec>");
        this.indentLevel++;

        // compilation logic
        
        // Parsing 'var'
        Token varT = eat("var");
        appendXmlIndentedLine(varT.xmlRepresentation());

        // Parsing type
        Token typeT = eatAny();
        if (!JackGrammar.indicatesType(typeT)) {
            throw new RuntimeException("SYNTAX ERROR! Expected a type (int|char|boolean|className), but found '" + typeT.getRepresentation() + "'");
        }
        appendXmlIndentedLine(typeT.xmlRepresentation());

        // Parsing varName
        Token varNameT = eatIdentifier();
        appendXmlIndentedLine(varNameT.xmlRepresentation());

        // Parsing (',' varName)*
        Token commaT, nextVarNameT;
        while (tokenizer.getCurrent().getRepresentation().equals(",")) {
            commaT = eat(",");
            appendXmlIndentedLine(commaT.xmlRepresentation());

            nextVarNameT = eatIdentifier();
            appendXmlIndentedLine(nextVarNameT.xmlRepresentation());
        }

        // Parsing ';'
        Token closeCurlyT = eat(";");
        appendXmlIndentedLine(closeCurlyT.xmlRepresentation());

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</varDec>");
    }

    public void compileStatements() {
        appendXmlIndentedLine("<statements>");
        this.indentLevel++;

        // compilation logic
        Token statementInitT = tokenizer.getCurrent();
        while ( JackGrammar.startsStatement(statementInitT) ) {

            // Parse statement
            String rep = statementInitT.getRepresentation();
            
            if (rep.equals("let")) {
                compileLet();
            } else if (rep.equals("if")) {
                compileIf();
            } else if (rep.equals("while")) {
                compileWhile();
            } else if (rep.equals("do")) {
                compileDo();
            } else if (rep.equals("return")) {
                compileReturn();
            } else {
                throw new RuntimeException("SYNTAX ERROR. Unrecognized statement initialization: '" + rep + "'");
            }

            statementInitT = tokenizer.getCurrent();
        }
        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</statements>");
    }

    public void compileDo() {
        appendXmlIndentedLine("<doStatement>");
        this.indentLevel++;

        // compilation logic

        // Parsing 'do'
        Token doT = eat("do");
        appendXmlIndentedLine(doT.xmlRepresentation());

        // Parsing subroutineCall
        if (isNext("(")) {
            // Parsing the pattern: subroutineName '(' expressionList ')'

            // Parsing subroutineName
            Token subroutineNameT = eatIdentifier();
            appendXmlIndentedLine(subroutineNameT.xmlRepresentation());

            // Parsing '('
            Token openParenT = eat("(");
            appendXmlIndentedLine(openParenT.xmlRepresentation());

            // Parsing expressionList
            compileExpressionList();

            // Parsing ')'
            Token closeParenT = eat(")");
            appendXmlIndentedLine(closeParenT.xmlRepresentation());


        } else {
            // Parsing the pattern: (className | varName) '.' subroutineName '(' expressionList ')'

            // Parsing (className | varName)
            Token nameT = eatIdentifier();
            appendXmlIndentedLine(nameT.xmlRepresentation());

            // Parsing '.'
            Token dotT = eat(".");
            appendXmlIndentedLine(dotT.xmlRepresentation());

            // Parsing subroutineName
            Token subroutineNameT = eatIdentifier();
            appendXmlIndentedLine(subroutineNameT.xmlRepresentation());

            // Parsing '('
            Token openParenT = eat("(");
            appendXmlIndentedLine(openParenT.xmlRepresentation());

            // Parsing expressionList
            compileExpressionList();

            // Parsing ')'
            Token closeParenT = eat(")");
            appendXmlIndentedLine(closeParenT.xmlRepresentation());

        }

        // Parsing ';'
        Token semicolonT = eat(";");
        appendXmlIndentedLine(semicolonT.xmlRepresentation());

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</doStatement>");
    }

    public void compileLet() {
        appendXmlIndentedLine("<letStatement>");
        this.indentLevel++;

        // compilation logic

        // Parsing 'let'
        Token letT = eat("let");
        appendXmlIndentedLine(letT.xmlRepresentation());

        // Parsing varName
        Token varNameT = eatIdentifier();
        appendXmlIndentedLine(varNameT.xmlRepresentation());

        // Parsing ('[' expression ']')?
        if (tokenizer.getCurrent().getRepresentation().equals("[")) {
            // Parsing '['
            Token openSquareT = eat("[");
            appendXmlIndentedLine(openSquareT.xmlRepresentation());

            // Parsing expression
            compileExpression();

            // Parsing ']'
            Token closeSquareT = eat("]");
            appendXmlIndentedLine(closeSquareT.xmlRepresentation());
        }

        // Parsing '='
        Token equalsT = eat("=");
        appendXmlIndentedLine(equalsT.xmlRepresentation());

        // Parsing expression
        compileExpression();

        // Parsing ';'
        Token semicolonT = eat(";");
        appendXmlIndentedLine(semicolonT.xmlRepresentation());

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</letStatement>");
    }

    public void compileWhile() {
        appendXmlIndentedLine("<whileStatement>");
        this.indentLevel++;

        // Pattern: 'while' '(' expression ')' '{' statements '}'
        // compilation logic

        // Parsing 'while'
        Token whileT = eat("while");
        appendXmlIndentedLine(whileT.xmlRepresentation());

        // Parsing '('
        Token openParenT = eat("(");
        appendXmlIndentedLine(openParenT.xmlRepresentation());

        // Parsing expression
        compileExpression();

        // Parsing ')'
        Token closeParenT = eat(")");
        appendXmlIndentedLine(closeParenT.xmlRepresentation());

        // Parsing '{'
        Token openCurlyT = eat("{");
        appendXmlIndentedLine(openCurlyT.xmlRepresentation());

        // Parsing statements
        compileStatements();

        // Parsing '}'
        Token closeCurlyT = eat("}");
        appendXmlIndentedLine(closeCurlyT.xmlRepresentation());

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</whileStatement>");
    }

    public void compileReturn() {
        appendXmlIndentedLine("<returnStatement>");
        this.indentLevel++;

        // Pattern: 'return' expression? ';'
        // compilation logic

        // Parsing 'return'
        Token returnT = eat("return");
        appendXmlIndentedLine(returnT.xmlRepresentation());

        // Parsing expression?
        if (!tokenizer.getCurrent().getRepresentation().equals(";")) {
            compileExpression();
        }

        // Parsing ';'
        Token semicolonT = eat(";");
        appendXmlIndentedLine(semicolonT.xmlRepresentation());

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</returnStatement>");
    }

    public void compileIf() {
        appendXmlIndentedLine("<ifStatement>");
        this.indentLevel++;

        // Pattern: 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
        // compilation logic

        // Parsing 'if'
        Token ifT = eat("if");
        appendXmlIndentedLine(ifT.xmlRepresentation());

        // Parsing '('
        Token openParenT = eat("(");
        appendXmlIndentedLine(openParenT.xmlRepresentation());

        // Parsing expression
        compileExpression();

        // Parsing ')'
        Token closeParenT = eat(")");
        appendXmlIndentedLine(closeParenT.xmlRepresentation());

        // Parsing '{'
        Token openCurlyT = eat("{");
        appendXmlIndentedLine(openCurlyT.xmlRepresentation());

        // Parsing statements
        compileStatements();

        // Parsing '}'
        Token closeCurlyT = eat("}");
        appendXmlIndentedLine(closeCurlyT.xmlRepresentation());

        // Parsing pattern: ('else' '{' statements '}')?
        if (tokenizer.getCurrent().getRepresentation().equals("else")) {
            // Parsing 'else'
            Token elseT = eat("else");
            appendXmlIndentedLine(elseT.xmlRepresentation());

            // Parsing '{'
            Token anotherOpenCurlyT = eat("{");
            appendXmlIndentedLine(anotherOpenCurlyT.xmlRepresentation());

            // Parsing statements
            compileStatements();

            // Parsing '}'
            Token anotherCloseCurlyT = eat("}");
            appendXmlIndentedLine(anotherCloseCurlyT.xmlRepresentation());
        }

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</ifStatement>");
    }

    public void compileExpression() {
        appendXmlIndentedLine("<expression>");
        this.indentLevel++;

        // Pattern: term (op term)*
        // compilation logic

        // Parsing term
        compileTerm();

        // Parsing pattern: (op term)*
        while (JackGrammar.isOp(tokenizer.getCurrent())) {
            // Parse op
            Token opT = eatAny();
            appendXmlIndentedLine(opT.xmlRepresentation());

            // Parse term
            compileTerm();
        }

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</expression>");
    }

    public void compileTerm() {
        appendXmlIndentedLine("<term>");
        this.indentLevel++;

        // compilation logic
        Token firstT = eatAny();
        TokenTypes firstType = firstT.getType();

        if (firstType == TokenTypes.STRING_CONST
            || firstType == TokenTypes.INT_CONST
            || firstType == TokenTypes.KEYWORD) 
        {
            appendXmlIndentedLine(firstT.xmlRepresentation());
        }
        else if (JackGrammar.isUnaryOp(firstT)) {
            // Pattern: unaryOp term
            appendXmlIndentedLine(firstT.xmlRepresentation());
            compileTerm();
        }
        else if (firstT.getRepresentation().equals("(")) {
            // Pattern: '(' expression ')'
            appendXmlIndentedLine(firstT.xmlRepresentation());

            compileExpression();

            Token closeParenT = eat(")");
            appendXmlIndentedLine(closeParenT.xmlRepresentation());
        }
        else {
            // Is one of these possibilities: varName | varName '[' expression ']' | subroutineCall
            String nextRep = tokenizer.getCurrent().getRepresentation();

            if (nextRep.equals("[")) {
                // Pattern: varName '[' expression ']'
                appendXmlIndentedLine(firstT.xmlRepresentation());

                Token openSquareT = eat("[");
                appendXmlIndentedLine(openSquareT.xmlRepresentation());
                
                compileExpression();

                Token closeSquareT = eat("]");
                appendXmlIndentedLine(closeSquareT.xmlRepresentation());
            }
            else if (nextRep.equals("(") || nextRep.equals(".")) {
                // is a subroutine call
                // Parsing subroutineCall
                if (nextRep.equals("(")) {
                    // Parsing the pattern: subroutineName '(' expressionList ')'

                    // Parsing subroutineName
                    appendXmlIndentedLine(firstT.xmlRepresentation());

                    // Parsing '('
                    Token openParenT = eat("(");
                    appendXmlIndentedLine(openParenT.xmlRepresentation());

                    // Parsing expressionList
                    compileExpressionList();

                    // Parsing ')'
                    Token closeParenT = eat(")");
                    appendXmlIndentedLine(closeParenT.xmlRepresentation());


                } else {
                    // Parsing the pattern: (className | varName) '.' subroutineName '(' expressionList ')'

                    // Parsing (className | varName)
                    appendXmlIndentedLine(firstT.xmlRepresentation());

                    // Parsing '.'
                    Token dotT = eat(".");
                    appendXmlIndentedLine(dotT.xmlRepresentation());

                    // Parsing subroutineName
                    Token subroutineNameT = eatIdentifier();
                    appendXmlIndentedLine(subroutineNameT.xmlRepresentation());

                    // Parsing '('
                    Token openParenT = eat("(");
                    appendXmlIndentedLine(openParenT.xmlRepresentation());

                    // Parsing expressionList
                    compileExpressionList();

                    // Parsing ')'
                    Token closeParenT = eat(")");
                    appendXmlIndentedLine(closeParenT.xmlRepresentation());

                }
            }
            else {
                // is only a variable name
                // Pattern: varName
                appendXmlIndentedLine(firstT.xmlRepresentation());
            }
        }

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</term>");
    }

    public void compileExpressionList() {
        // Assumption: expressionList only comes before a closing paren,
        //   only way I could think of to check if there is at least one expression

        appendXmlIndentedLine("<expressionList>");
        this.indentLevel++;

        // Pattern: (expression (',' expression)* )?
        // compilation logic
        if (!tokenizer.getCurrent().getRepresentation().equals(")")) {  // <- assumption
            compileExpression();

            while (tokenizer.getCurrent().getRepresentation().equals(",")) {
                Token commaT = eat(",");
                appendXmlIndentedLine(commaT.xmlRepresentation());

                compileExpression();
            }
        }

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</expressionList>");
    }

    private Boolean isNext(String tentative) {
        Token next = tokenizer.lookAhead(1);
        return next.getRepresentation().equals(tentative);
    }

    private Token eat(String expected) {
        Token eaten = tokenizer.getCurrent();
        if (eaten.getRepresentation().equals(expected)) {
            tokenizer.advance();
        } else {
            throw new RuntimeException("SYNTAX ERROR! Expected to eat '" + expected + "', but found: '" + eaten.getRepresentation() + "'.");
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
            String xmlOutputhPath = outputPath.replace(".vm", "-Output.xml");
            File outputXmlFile = new File(xmlOutputhPath);
            if (outputXmlFile.exists()) {
                outputXmlFile.delete();
            }
            FileWriter writer = new FileWriter(outputXmlFile);
            writer.write(xmlBuilder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not write analyzed xml, for output file: " + outputPath);
        }
    }

    
}