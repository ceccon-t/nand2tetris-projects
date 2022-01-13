public class Token {

    private String representation;
    private TokenTypes type;

    public Token(String raw) {
        this.representation = raw;
        this.type = JackGrammar.determineType(raw);
    }

    public String getRepresentation() {
        return this.representation;
    }

    public TokenTypes getType() {
        return this.type;
    }
    
}
