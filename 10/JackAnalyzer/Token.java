public class Token {

    private String representation;
    private TokenTypes type;

    public Token(String raw) {
        this.representation = raw;
    }

    public String getRepresentation() {
        return this.representation;
    }
    
}
