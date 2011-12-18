package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.util.ShedMaps;

public class GlobalTypes {
    private final Map<FullyQualifiedName, Type> global = new HashMap<FullyQualifiedName, Type>();


    public void addGlobal(FullyQualifiedName name, Type type) {
        global.put(name, type);
    }
    
    public Option<Type> lookupGlobal(FullyQualifiedName name) {
        return ShedMaps.getOrNone(global, name);
    }
}
