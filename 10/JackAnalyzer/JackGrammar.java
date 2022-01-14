import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JackGrammar {

    private static Set<String> allSymbols = Stream.of(
        "{", "}", "(", ")", "[", "]", ".",
        ",", ";", "+", "-", "*", "/", "&",
        "|", "<", ">", "=", "~"
    ).collect(Collectors.toCollection(HashSet::new));;

    private static Map<String, Keywords> allKeywords;

    static {
        allKeywords = new HashMap<String, Keywords>();
        allKeywords.put("class", Keywords.CLASS);
        allKeywords.put("constructor", Keywords.CONSTRUCTOR);
        allKeywords.put("function", Keywords.FUNCTION);
        allKeywords.put("method", Keywords.METHOD);
        allKeywords.put("field", Keywords.FIELD);
        allKeywords.put("static", Keywords.STATIC);
        allKeywords.put("var", Keywords.VAR);
        allKeywords.put("int", Keywords.INT);
        allKeywords.put("char", Keywords.CHAR);
        allKeywords.put("boolean", Keywords.BOOLEAN);
        allKeywords.put("void", Keywords.VOID);
        allKeywords.put("true", Keywords.TRUE);
        allKeywords.put("false", Keywords.FALSE);
        allKeywords.put("null", Keywords.NULL);
        allKeywords.put("this", Keywords.THIS);
        allKeywords.put("let", Keywords.LET);
        allKeywords.put("do", Keywords.DO);
        allKeywords.put("if", Keywords.IF);
        allKeywords.put("else", Keywords.ELSE);
        allKeywords.put("while", Keywords.WHILE);
        allKeywords.put("return", Keywords.RETURN);
    }

    public static Boolean isSymbol(String rawToken) {
        return allSymbols.contains(rawToken);
    }

    public static Boolean isSymbol(Character c) {
        return isSymbol(c.toString());
    }

    public static Boolean isKeyword(String rawToken) {
        return allKeywords.containsKey(rawToken);
    }

    public static Boolean isIntegerConstant(String rawToken) {
        Boolean isInteger = true;

        try {
            Integer.parseInt(rawToken);
        } catch (NumberFormatException ex) {
            isInteger = false;
        }

        return isInteger;
    }

    public static Boolean isStringConstant(String rawToken) {
        Boolean isString = true;
        int last = rawToken.length()-1;

        // Starts with a double quote
        if ( !(rawToken.charAt(0) == '"') ) return false;

        // Sequence of chars do not contain double quote or newline
        for (int i = 1; i < last; i++) if (rawToken.charAt(i) == '"' || rawToken.charAt(i) == '\n') return false;

        // Ends with a double quote
        if ( !(rawToken.charAt(last) == '"') ) return false;

        return isString;
    }

    public static Boolean isIdentifier(String rawToken) {
        Boolean is = true;

        // Does not start with digit
        if (Character.isDigit(rawToken.charAt(0))) return false;

        // Contains only letters, digits, and underscores
        for (int i = 1; i < rawToken.length(); i++) {
            Character cur = rawToken.charAt(i);
            if ( !Character.isAlphabetic(cur)
                 && !Character.isDigit(cur)
                 && (cur != '_') ) {
                     return false;
                 }
        }

        return is;
    }

    public static TokenTypes determineType(String rawToken) {
        TokenTypes type = null;

        if (isSymbol(rawToken)) {
            type = TokenTypes.SYMBOL;
        } else if (isKeyword(rawToken)) {
            type = TokenTypes.KEYWORD;
        } else if (isIntegerConstant(rawToken)) {
            type = TokenTypes.INT_CONST;
        } else if (isStringConstant(rawToken)) {
            type = TokenTypes.STRING_CONST;
        } else if (isIdentifier(rawToken)) {
            type = TokenTypes.IDENTIFIER;
        }

        if (type == null) throw new RuntimeException("Token '" + rawToken + "' could not be parsed into a type.");

        return type;
    }

    public static Boolean startsWord(Character c) {
        return Character.isAlphabetic(c) || c == '_';
    }

    public static Boolean breaksWord(Character c) {
        if (Character.isWhitespace(c)) return true;

        if (isSymbol(c)) return true;

        if ( !Character.isAlphabetic(c) && !Character.isDigit(c) && c != '_' ) return true;

        return false;
    }

    public static Keywords getKeywordEnum(Token token) {
        return allKeywords.get(token.getRepresentation());
    }

    public static Boolean startsClassVarDeclaration(Token token) {
        if (!(token.getType() == TokenTypes.KEYWORD)) return false;

        Keywords tokenKeyword = allKeywords.get(token.getRepresentation());
        return tokenKeyword == Keywords.STATIC || tokenKeyword == Keywords.FIELD;
    }

    public static Boolean startsSubroutineDeclaration(Token token) {
        if (!(token.getType() == TokenTypes.KEYWORD)) return false;

        Keywords tokenKeyword = allKeywords.get(token.getRepresentation());
        return tokenKeyword == Keywords.CONSTRUCTOR 
                || tokenKeyword == Keywords.FUNCTION
                || tokenKeyword == Keywords.METHOD;
    }

    public static Boolean declaresScope(Token token)  {
        return token.getRepresentation().equals("static") 
                || token.getRepresentation().equals("field");
    }

    public static Boolean isPrimitiveType(Token token) {
        return token.getRepresentation().equals("int")
                || token.getRepresentation().equals("char")
                || token.getRepresentation().equals("boolean");
    }

    public static Boolean indicatesType(Token token) {
        return isPrimitiveType(token) || token.getType().equals(TokenTypes.IDENTIFIER);
    }

    public static Boolean indicatesSubroutineReturnType(Token token) {
        return indicatesType(token) || token.getRepresentation().equals("void");
    }

    public static Boolean startsLocalVariableDeclaration(Token token) {
        return token.getRepresentation().equals("var");
    }

    public static Boolean startsStatement(Token token) {
        String rep = token.getRepresentation();

        return rep.equals("let") || rep.equals("if") 
                || rep.equals("while") || rep.equals("do") 
                || rep.equals("return");

    }

    public static Boolean isOp(Token token) {
        String rep = token.getRepresentation();

        return rep.equals("+") || rep.equals("-") || rep.equals("*") || rep.equals("/")
                || rep.equals("&") || rep.equals("|") || rep.equals("<") || rep.equals(">")
                || rep.equals("=");
    }

    public static Boolean isUnaryOp(Token token) {
        String rep = token.getRepresentation();
        return rep.equals("-") || rep.equals("~");
    }

}
