public class Token {

    private String representation;
    private TokenTypes type;

    public Token(String raw) {
        this.type = JackGrammar.determineType(raw);

        if (this.type == TokenTypes.STRING_CONST) {
            this.representation = raw.substring(1, raw.length()-1);
        } else {
            this.representation = raw;
        }
    }

    public String getRepresentation() {
        return this.representation;
    }

    public TokenTypes getType() {
        return this.type;
    }

    public String xmlRepresentation() {
        String typeRep = tokenTypeStringRepresentation();
        return "<" + typeRep + "> " + canonicalXmlTokenRepresentation() + " </" + typeRep + ">";
    }

    /**
     * Used only in the context of generating the tokens xml, to test Project 10
     */
    private String tokenTypeStringRepresentation() {
        String rep = "";

        switch(type) {
            case KEYWORD:
                rep = "keyword";
                break;
            case SYMBOL:
                rep = "symbol";
                break;
            case IDENTIFIER:
                rep = "identifier";
                break;
            case INT_CONST:
                rep = "integerConstant";
                break;
            case STRING_CONST:
                rep = "stringConstant";
                break;
        }

        return rep;
    }

    /**
     * Used only in the context of generating the tokens xml, to test Project 10
     */
    private String canonicalXmlTokenRepresentation() {
        String rep = representation;

        if (rep.equals("<")) rep = "&lt;";
        else if (rep.equals(">")) rep = "&gt;";
        else if (rep.equals("\"")) rep = "&quot";
        else if (rep.equals("&")) rep = "&amp;";

        return rep;
    }

    
}
