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
    
}
