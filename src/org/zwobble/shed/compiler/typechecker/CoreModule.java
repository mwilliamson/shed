package org.zwobble.shed.compiler.typechecker;

import java.util.Map;

import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class CoreModule {
    public static final Map<String, Type> VALUES;
    public static final Map<String, GlobalDeclarationNode> GLOBAL_DECLARATIONS;
    
    static {
        Builder<String, Type> valuesBuilder = ImmutableMap.builder();
        valuesBuilder.put("String", CoreTypes.classOf(CoreTypes.STRING));
        valuesBuilder.put("Number", CoreTypes.classOf(CoreTypes.NUMBER));
        valuesBuilder.put("Boolean", CoreTypes.classOf(CoreTypes.BOOLEAN));
        valuesBuilder.put("Unit", CoreTypes.classOf(CoreTypes.UNIT));
        
        for (int i = 0; i < 20; i += 1) {
            ParameterisedType functionType = CoreTypes.functionType(i);
            valuesBuilder.put((functionType.getBaseType()).getFullyQualifiedName().last(), functionType);
        }
        VALUES = valuesBuilder.build();

        Builder<String, GlobalDeclarationNode> declarationsBuilder = ImmutableMap.builder();
        for (String identifier : VALUES.keySet()) {
            declarationsBuilder.put(identifier, new GlobalDeclarationNode(identifier));
        }
        GLOBAL_DECLARATIONS = declarationsBuilder.build();
    }
}
