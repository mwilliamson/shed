package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.Option.none;

import static org.zwobble.shed.compiler.Option.some;

public class StaticContext {
    public static StaticContext defaultContext() {
        StaticContext staticContext = new StaticContext();
        staticContext.add("String", CoreTypes.classOf(CoreTypes.STRING));
        staticContext.add("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        staticContext.add("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        staticContext.add("Class", CoreTypes.classOf(CoreTypes.CLASS));
        return staticContext;
    }
    
    private final List<StaticScope> scopes = new ArrayList<StaticScope>();
    private final Map<List<String>, Type> global = new HashMap<List<String>, Type>();
    
    public StaticContext() {
        scopes.add(new StaticScope());
    }
    
    public void add(String identifier, Type type) {
        currentScope().add(identifier, type);
    }
    
    public Option<Type> get(String identifier) {
        for (StaticScope scope : scopes) {
            if (scope.isDeclared(identifier)) {
                return some(scope.get(identifier));
            }   
        }
        return none();
    }
    
    public boolean isDeclaredInCurrentScope(String identifier) {
        return currentScope().isDeclared(identifier);
    }

    public void addGlobal(List<String> identifiers, Type type) {
        global.put(identifiers, type);
    }
    
    public Option<Type> lookupGlobal(List<String> identifiers) {
        if (global.containsKey(identifiers)) {
            return some(global.get(identifiers));
        } else {
            return none();
        }
    }

    public void newScope() {
        scopes.add(new StaticScope());
    }

    private StaticScope currentScope() {
        return scopes.get(scopes.size() - 1);
    }
}
