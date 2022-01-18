import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private String outputPath;
    private String className;
    private String currentSubroutineName;
    private Map<UUID, Integer> nargsToSubroutine = new HashMap<>();
    private Integer uniqueLabelNumber = 0;

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

        // VM:
        this.className = classNameT.getRepresentation();

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

        // VM:
        String name = varNameT.getRepresentation();
        String type = typeT.getRepresentation();
        VariableKind kind = kindFromRaw(scopeT.getRepresentation());
        symbolTable.define(name, type, kind);

        // Parsing (',' varName)*
        Token nextCommaT, nextVarNameT;
        while (tokenizer.getCurrent().getRepresentation().equals(",")) {
            nextCommaT = eat(",");
            appendXmlIndentedLine(nextCommaT.xmlRepresentation());

            nextVarNameT = eatIdentifier();
            appendXmlIndentedLine(nextVarNameT.xmlRepresentation());

            // VM:
            name = nextVarNameT.getRepresentation();
            symbolTable.define(name, type, kind);
        }

        Token semicolonT = eat(";");
        appendXmlIndentedLine(semicolonT.xmlRepresentation());
        
        this.indentLevel--;

        appendXmlIndentedLine("</classVarDec>");
    }

    public void compileSubroutine() {
        appendXmlIndentedLine("<subroutineDec>");
        this.indentLevel++;

        // VM:
        symbolTable.startSubroutine();

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

        // VM:
        this.currentSubroutineName = subroutineNameT.getRepresentation();

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

            // VM:
            Integer numberLocalVariables = symbolTable.varCount(VariableKind.VAR);
            vmWriter.writeFunction(currentSubroutineFullName(), numberLocalVariables);
            if (subroutineTypeT.getRepresentation().equals("constructor")) {
                // Allocate memory to object and set the address of the allocated block on "this"
                vmWriter.writePush(Segment.CONST, numberLocalVariables);
                vmWriter.writeCall("Memory.alloc", 1);
                vmWriter.writePop(Segment.POINTER, 0);
            } else if (subroutineTypeT.getRepresentation().equals("method")) {
                // Sets the "this" reference through the 'pointer' virtual segment
                vmWriter.writePush(Segment.ARG, 0);
                vmWriter.writePop(Segment.POINTER, 0);
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

            // VM:
            String name = varNameT.getRepresentation();
            String type = typeT.getRepresentation();
            VariableKind kind = VariableKind.ARG;
            symbolTable.define(name, type, kind);
    
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

                // VM:
                name = nextVarNameT.getRepresentation();
                type = nextTypeT.getRepresentation();
                symbolTable.define(name, type, kind);
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

        // VM:
        String name = varNameT.getRepresentation();
        String type = typeT.getRepresentation();
        VariableKind kind = VariableKind.VAR;
        symbolTable.define(name, type, kind);

        // Parsing (',' varName)*
        Token commaT, nextVarNameT;
        while (tokenizer.getCurrent().getRepresentation().equals(",")) {
            commaT = eat(",");
            appendXmlIndentedLine(commaT.xmlRepresentation());

            nextVarNameT = eatIdentifier();
            appendXmlIndentedLine(nextVarNameT.xmlRepresentation());

            // VM:
            name = nextVarNameT.getRepresentation();
            symbolTable.define(name, type, kind);
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

        // For VM:
        UUID id = UUID.randomUUID();
        nargsToSubroutine.put(id, 0);

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
            compileExpressionList(id);

            // VM:
            vmWriter.writeCall(this.className + "." + subroutineNameT.getRepresentation(), nargsToSubroutine.get(id));

            // Parsing ')'
            Token closeParenT = eat(")");
            appendXmlIndentedLine(closeParenT.xmlRepresentation());


        } else {
            // Parsing the pattern: (className | varName) '.' subroutineName '(' expressionList ')'

            // Parsing (className | varName)
            Token nameT = eatIdentifier();
            appendXmlIndentedLine(nameT.xmlRepresentation());
            String identifierRep = nameT.getRepresentation();

            // For VM:
            if(identifierIsRecognized(identifierRep)) {
                // calling a method on the variable, so must push the object to stack first
                pushVariableOnStack(identifierRep);
                incrementNargsToSubroutine(id);
            }

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
            compileExpressionList(id);

            // VM:
            String calleeName = nameT.getRepresentation() + "." + subroutineNameT.getRepresentation();
            Integer nArgs = nargsToSubroutine.get(id);
            vmWriter.writeCall(calleeName, nArgs);

            // Parsing ')'
            Token closeParenT = eat(")");
            appendXmlIndentedLine(closeParenT.xmlRepresentation());

        }

        // Parsing ';'
        Token semicolonT = eat(";");
        appendXmlIndentedLine(semicolonT.xmlRepresentation());

        // Cleaning up:
        nargsToSubroutine.remove(id);
        vmWriter.writePop(Segment.TEMP, 0);

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</doStatement>");
    }

    public void compileLet() {
        appendXmlIndentedLine("<letStatement>");
        this.indentLevel++;
        Segment targetSegment;
        Integer targetIndex;
        Boolean isIndexingArray = false;

        // compilation logic

        // Parsing 'let'
        Token letT = eat("let");
        appendXmlIndentedLine(letT.xmlRepresentation());

        // Parsing varName
        Token varNameT = eatIdentifier();
        appendXmlIndentedLine(varNameT.xmlRepresentation());

        // For VM:
        targetSegment = kindToSegment(symbolTable.kindOf(varNameT.getRepresentation()));
        targetIndex = symbolTable.indexOf(varNameT.getRepresentation());

        // Parsing ('[' expression ']')?
        if (tokenizer.getCurrent().getRepresentation().equals("[")) {
            isIndexingArray = true;
            // VM:
            vmWriter.writePush(targetSegment, targetIndex);

            // Parsing '['
            Token openSquareT = eat("[");
            appendXmlIndentedLine(openSquareT.xmlRepresentation());

            // Parsing expression
            compileExpression();

            // Parsing ']'
            Token closeSquareT = eat("]");
            appendXmlIndentedLine(closeSquareT.xmlRepresentation());

            // VM:
            vmWriter.writeArithmetic(VMCommand.ADD);  // leaves arr[exp] on top of stack
        }

        // Parsing '='
        Token equalsT = eat("=");
        appendXmlIndentedLine(equalsT.xmlRepresentation());

        // Parsing expression
        compileExpression();

        // Parsing ';'
        Token semicolonT = eat(";");
        appendXmlIndentedLine(semicolonT.xmlRepresentation());

        // VM:
        if (isIndexingArray) {
            // "General solution for generating array access code"
            // As shown in video lecture on how to handle arrays
            vmWriter.writePop(Segment.TEMP, 0);  // save the value of result to be stored
            vmWriter.writePop(Segment.POINTER, 1); // retrieves arr[exp] from top of stacl onto "THAT"
            vmWriter.writePush(Segment.TEMP, 0);  // put the result to be stored on top of stack

            // finally store the result onto position arr[exp]
            targetSegment = Segment.THAT;
            targetIndex = 0;
        }
        vmWriter.writePop(targetSegment, targetIndex);

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</letStatement>");
    }

    public void compileWhile() {
        appendXmlIndentedLine("<whileStatement>");
        this.indentLevel++;
        String l1 = generateUniqueLabel("L1_WHILE");
        String l2 = generateUniqueLabel("L2_WHILE");


        // Pattern: 'while' '(' expression ')' '{' statements '}'
        // compilation logic

        // VM:
        vmWriter.writeLabel(l1);

        // Parsing 'while'
        Token whileT = eat("while");
        appendXmlIndentedLine(whileT.xmlRepresentation());

        // Parsing '('
        Token openParenT = eat("(");
        appendXmlIndentedLine(openParenT.xmlRepresentation());

        // Parsing expression
        compileExpression();

        // VM:
        vmWriter.writeArithmetic(VMCommand.NEG);
        vmWriter.writeIf(l2);

        // Parsing ')'
        Token closeParenT = eat(")");
        appendXmlIndentedLine(closeParenT.xmlRepresentation());

        // Parsing '{'
        Token openCurlyT = eat("{");
        appendXmlIndentedLine(openCurlyT.xmlRepresentation());

        // Parsing statements
        compileStatements();

        // VM:
        vmWriter.writeGoto(l1);

        // Parsing '}'
        Token closeCurlyT = eat("}");
        appendXmlIndentedLine(closeCurlyT.xmlRepresentation());

        // VM:
        vmWriter.writeLabel(l2);

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
        if (tokenizer.getCurrent().getRepresentation().equals(";")) {
            // If the next token is a semicolon, put "0" on top of stack before returning
            // VM:
            vmWriter.writePush(Segment.CONST, 0);
        } else {
            // If not, compile the expression to return
            compileExpression();
        }

        // Parsing ';'
        Token semicolonT = eat(";");
        appendXmlIndentedLine(semicolonT.xmlRepresentation());

        // VM:
        vmWriter.writeReturn();

        // end of compilation logic

        this.indentLevel--;
        appendXmlIndentedLine("</returnStatement>");
    }

    public void compileIf() {
        appendXmlIndentedLine("<ifStatement>");
        this.indentLevel++;
        String l1 = generateUniqueLabel("L1_IF");
        String l2 = generateUniqueLabel("L2_IF");

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

        // VM:
        vmWriter.writeArithmetic(VMCommand.NEG);
        vmWriter.writeIf(l1);

        // Parsing ')'
        Token closeParenT = eat(")");
        appendXmlIndentedLine(closeParenT.xmlRepresentation());

        // Parsing '{'
        Token openCurlyT = eat("{");
        appendXmlIndentedLine(openCurlyT.xmlRepresentation());

        // Parsing statements
        compileStatements();

        // VM:
        vmWriter.writeGoto(l2);

        // Parsing '}'
        Token closeCurlyT = eat("}");
        appendXmlIndentedLine(closeCurlyT.xmlRepresentation());

        // VM:
        vmWriter.writeLabel(l1);

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

        vmWriter.writeLabel(l2);

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
                UUID id = UUID.randomUUID();
                nargsToSubroutine.put(id, 0);
                // Parsing subroutineCall
                if (nextRep.equals("(")) {
                    // Parsing the pattern: subroutineName '(' expressionList ')'

                    // Parsing subroutineName
                    appendXmlIndentedLine(firstT.xmlRepresentation());

                    // Parsing '('
                    Token openParenT = eat("(");
                    appendXmlIndentedLine(openParenT.xmlRepresentation());

                    // Parsing expressionList
                    compileExpressionList(id);

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
                    compileExpressionList(id);

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

    public void compileExpressionList(UUID callerId) {
        // Assumption: expressionList only comes before a closing paren,
        //   only way I could think of to check if there is at least one expression

        appendXmlIndentedLine("<expressionList>");
        this.indentLevel++;

        // Pattern: (expression (',' expression)* )?
        // compilation logic
        if (!tokenizer.getCurrent().getRepresentation().equals(")")) {  // <- assumption
            incrementNargsToSubroutine(callerId);
            compileExpression();

            while (tokenizer.getCurrent().getRepresentation().equals(",")) {
                Token commaT = eat(",");
                appendXmlIndentedLine(commaT.xmlRepresentation());

                incrementNargsToSubroutine(callerId);
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

    private VariableKind kindFromRaw(String raw) {
        VariableKind kind = null;

        switch(raw) {
            case "static":
                kind = VariableKind.STATIC;
                break;
            case "field":
                kind = VariableKind.FIELD;
                break;
            default:
                throw new RuntimeException("Variable kind not recognized: " + raw);
        }

        return kind;
    }

    private String currentSubroutineFullName() {
        return this.className + "." + this.currentSubroutineName;
    }

    private void incrementNargsToSubroutine(UUID id) {
        nargsToSubroutine.put(id, nargsToSubroutine.get(id) + 1);
    }

    private Boolean identifierIsRecognized(String name) {
        return symbolTable.kindOf(name) != VariableKind.NONE;
    }

    private Segment kindToSegment(VariableKind kind) {
        Segment seg = null;

        switch(kind) {
            case STATIC:
                seg = Segment.STATIC;
                break;
            case FIELD:
                seg = Segment.THIS;
                break;
            case ARG:
                seg = Segment.ARG;
                break;
            case VAR:
                seg = Segment.LOCAL;
                break;
            default:
                throw new RuntimeException("Impossible to get segment for variable kind: '" + kind.toString() + "'");
        }

        return seg;
    }

    private void pushVariableOnStack(String variable) {
        Integer index = symbolTable.indexOf(variable);
        VariableKind kind = symbolTable.kindOf(variable);
        Segment seg = kindToSegment(kind);
        vmWriter.writePush(seg, index);
    }

    private String generateUniqueLabel(String description) {
        String label = this.className + "_" + uniqueLabelNumber.toString() + "_" + description;
        uniqueLabelNumber++;
        return label.toUpperCase();
    }

    
}