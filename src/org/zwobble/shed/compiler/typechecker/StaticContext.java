package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

public class StaticContext {
    public static StaticContext defaultContext() {
        StaticContext staticContext = new StaticContext();
        staticContext.add("String", CoreTypes.classOf(CoreTypes.STRING));
        staticContext.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        staticContext.add("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        staticContext.add("Class", CoreTypes.classOf(CoreTypes.CLASS));
        return staticContext;
    }
    
    private final Map<String, Type> values = new HashMap<String, Type>();
    
    public void add(String identifier, Type type) {
        values.put(identifier, type);
    }
    
    public Option<Type> get(String identifier) {
        if (values.containsKey(identifier)) {
            return Option.some(values.get(identifier));
        } else {
            return Option.none();
        }
    }
}
