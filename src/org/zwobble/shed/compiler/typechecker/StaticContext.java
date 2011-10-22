package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.MembersBuilder;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class StaticContext {
    public static StaticContext defaultContext() {
        StaticContext staticContext = new StaticContext();
        for (Entry<String, Type> value : CoreModule.VALUES.entrySet()) {
            addCore(staticContext, value.getKey(), value.getValue());
        }
        return staticContext;
    }

    private static void addCore(StaticContext staticContext, String identifier, Type type) {
        staticContext.addGlobal(asList("shed", "core", identifier), type);
        staticContext.add(CoreModule.GLOBAL_DECLARATIONS.get(identifier), unassignableValue(type));
    }
    
    private final Map<List<String>, Type> global = new HashMap<List<String>, Type>();
    private final Map<Identity<DeclarationNode>, ValueInfo> types = new HashMap<Identity<DeclarationNode>, ValueInfo>();
    private final Map<ScalarType, ScalarTypeInfo> scalarTypeInfo = new HashMap<ScalarType, ScalarTypeInfo>();
    
    public StaticContext() {
        addInfo(CoreTypes.STRING, new ScalarTypeInfo(interfaces(), members()));
        addInfo(CoreTypes.BOOLEAN, new ScalarTypeInfo(interfaces(), members()));
        addInfo(CoreTypes.DOUBLE, numberTypeInfo(CoreTypes.DOUBLE));
        addInfo(CoreTypes.UNIT, new ScalarTypeInfo(interfaces(), members()));
    }
    
    public void add(DeclarationNode declaration, ValueInfo type) {
        types.put(new Identity<DeclarationNode>(declaration), type);
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
    
    public void addInfo(ScalarType type, ScalarTypeInfo typeInfo) {
        scalarTypeInfo.put(type, typeInfo);
    }
    
    public ScalarTypeInfo getInfo(ScalarType scalarType) {
        return scalarTypeInfo.get(scalarType);
    }
    
    private ScalarTypeInfo numberTypeInfo(Type numberType) {
        MembersBuilder members = Members.builder();
        members.add("equals", unassignableValue(CoreTypes.functionTypeOf(numberType, CoreTypes.BOOLEAN)));
        members.add("add", unassignableValue(CoreTypes.functionTypeOf(numberType, numberType)));
        members.add("subtract", unassignableValue(CoreTypes.functionTypeOf(numberType, numberType)));
        members.add("multiply", unassignableValue(CoreTypes.functionTypeOf(numberType, numberType)));
        members.add("toString", unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING)));
        return new ScalarTypeInfo(interfaces(), members.build());
    }
}
