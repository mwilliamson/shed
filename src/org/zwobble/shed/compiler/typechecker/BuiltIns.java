package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;

public class BuiltIns {
    public static BuiltIns builtIns(String name1, GlobalDeclaration declaration1, String name2, GlobalDeclaration declaration2) {
        BuiltIns builtIns = new BuiltIns();
        builtIns.add(name1, declaration1);
        builtIns.add(name2, declaration2);
        return builtIns;
    }
    
    private final Map<String, GlobalDeclaration> builtIns = new HashMap<String, GlobalDeclaration>();
    
    public void add(String name, GlobalDeclaration declaration) {
        builtIns.put(name, declaration);
    }
    
    public Option<GlobalDeclaration> get(String name) {
        if (builtIns.containsKey(name)) {
            return Option.some(builtIns.get(name));
        } else {
            return Option.none();
        }
    }

    public Iterable<String> allNames() {
        return builtIns.keySet();
    }
}
