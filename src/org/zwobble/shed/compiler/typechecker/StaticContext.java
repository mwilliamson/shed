package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;

public class StaticContext {
    public static StaticContext defaultContext(References references) {
        StaticContext staticContext = new StaticContext(references);
        for (Entry<String, Type> value : CoreModule.VALUES.entrySet()) {
            addCore(staticContext, value.getKey(), value.getValue());
        }
        return staticContext;
    }

    private static void addCore(StaticContext staticContext, String identifier, Type type) {
        staticContext.addGlobal(asList("shed", "core", identifier), type);
        staticContext.add(CoreModule.GLOBAL_DECLARATIONS.get(identifier), type);
    }
    
    private final Map<List<String>, Type> global = new HashMap<List<String>, Type>();
    private final Map<Identity<DeclarationNode>, Type> types = new HashMap<Identity<DeclarationNode>, Type>();
    private final References references;
    
    public StaticContext(References references) {
        this.references = references;
    }
    
    public void add(DeclarationNode declaration, Type type) {
        types.put(new Identity<DeclarationNode>(declaration), type);
    }

    public VariableLookupResult get(VariableIdentifierNode reference) {
        return get(references.findReferent(reference));
    }

    public VariableLookupResult get(DeclarationNode declaration) {
        Identity<DeclarationNode> key = new Identity<DeclarationNode>(declaration);
        if (types.containsKey(key)) {
            return VariableLookupResult.success(types.get(key));
        } else {
            return VariableLookupResult.notDeclared();            
        }
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
}
