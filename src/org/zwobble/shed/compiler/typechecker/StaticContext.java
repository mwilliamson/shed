package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.Lists;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;

public class StaticContext {
    public static StaticContext defaultContext() {
        StaticContext staticContext = new StaticContext();
        addCore(staticContext, "String", CoreTypes.classOf(CoreTypes.STRING));
        addCore(staticContext, "Number", CoreTypes.classOf(CoreTypes.NUMBER));
        addCore(staticContext, "Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        addCore(staticContext, "Unit", CoreTypes.classOf(CoreTypes.UNIT));
//        addCore(staticContext, "Class", CoreTypes.classOf(CoreTypes.CLASS));
        
        for (int i = 0; i < 20; i += 1) {
            ParameterisedType functionType = CoreTypes.functionType(i);
            // TODO: remove assumption that the base type is a ClassType
            addCore(staticContext, ((ClassType)functionType.getBaseType()).getName(), functionType);
        }
        
        return staticContext;
    }
    
    private static void addCore(StaticContext context, String name, Type type) {
        context.add(name, type);
        context.addGlobal(asList("shed", "core", name), type);
    }
    
    private final List<StaticScope> scopes = new ArrayList<StaticScope>();
    private final Map<List<String>, Type> global = new HashMap<List<String>, Type>();
    
    public StaticContext() {
        scopes.add(new StaticScope(none(Type.class)));
    }
    
    public void add(String identifier, Type type) {
        currentScope().add(identifier, type);
    }

    public void declaredSoon(String identifier) {
        currentScope().declaredSoon(identifier);
    }
    
    public VariableLookupResult get(String identifier) {
        for (StaticScope scope : Lists.reverse(scopes)) {
            VariableLookupResult result = scope.get(identifier);
            if (result.getStatus() != Status.NOT_DECLARED) {
                return result;
            }
        }
        return new VariableLookupResult(Status.NOT_DECLARED, null);
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

    public void enterNewScope(Option<Type> type) {
        scopes.add(new StaticScope(type));
    }
    
    public void exitScope() {
        scopes.remove(scopes.size() - 1);
    }

    public StaticScope currentScope() {
        return scopes.get(scopes.size() - 1);
    }
}
