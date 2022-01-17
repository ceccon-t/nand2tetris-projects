public class Variable {
    private String name;
    private String type;
    private VariableKind kind;
    private Integer index;

    public Variable(String name, String type, VariableKind kind, Integer index) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public VariableKind getKind() {
        return this.kind;
    }

    public Integer getIndex() {
        return this.index;
    }


}
