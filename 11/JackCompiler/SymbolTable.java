import java.util.Map; 
import java.util.HashMap;

public class SymbolTable {

    private Map<String, Variable> classTable;
    private Map<String, Variable> subroutineTable;

    public SymbolTable() {
        this.classTable = new HashMap<String, Variable>();
    }

    public void startSubroutine() {
        this.subroutineTable = new HashMap<String, Variable>();
    }

    public void define(String name, String type, VariableKind kind) {
        Integer nextIndex = varCount(kind);

        Variable newVariable = new Variable(name, type, kind, nextIndex);
        
        if (hasClassScope(kind)) {
            classTable.put(name, newVariable);
        } else {
            subroutineTable.put(name, newVariable);
        }

    }

    public Integer varCount(VariableKind kind) {
        Integer total = 0;

        Map<String, Variable> relevantTable = hasClassScope(kind) ? classTable : subroutineTable;

        for (Variable cur : relevantTable.values()) {
            if (cur.getKind() == kind) {
                total++;
            }
        }
        
        return total;
    }

    public VariableKind kindOf(String name) {
        VariableKind kind = VariableKind.NONE;

        if (classTable.containsKey(name)) {
            kind = classTable.get(name).getKind();
        } else if (subroutineTable.containsKey(name)) {
            kind = subroutineTable.get(name).getKind();
        }

        return kind;
    }

    public String typeOf(String name) {
        String type = "";

        if (classTable.containsKey(name)) {
            type = classTable.get(name).getType();
        } else if (subroutineTable.containsKey(name)) {
            type = subroutineTable.get(name).getType();
        } else {
            throw new RuntimeException("Variable '" + name + "' could not be found when trying to get type of.");
        }

        return type;
    }

    public Integer indexOf(String name) {
        Integer index = 0;

        if (classTable.containsKey(name)) {
            index = classTable.get(name).getIndex();
        } else if (subroutineTable.containsKey(name)) {
            index = subroutineTable.get(name).getIndex();
        } else {
            throw new RuntimeException("Variable '" + name + "' could not be found when trying to get index of.");
        }

        return index;
    }

    private Boolean hasClassScope(VariableKind kind) {
        return kind == VariableKind.STATIC || kind == VariableKind.FIELD;
    }
    
}
