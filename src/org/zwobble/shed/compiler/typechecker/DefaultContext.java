package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.MembersBuilder;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class DefaultContext {
    public static StaticContext defaultContext() {
        StaticContext staticContext = new StaticContext();
        
        addCore(staticContext, CoreTypes.STRING, new ScalarTypeInfo(interfaces(), members()));
        addCore(staticContext, CoreTypes.BOOLEAN, new ScalarTypeInfo(interfaces(), members()));
        addCore(staticContext, CoreTypes.DOUBLE, numberTypeInfo(CoreTypes.DOUBLE));
        addCore(staticContext, CoreTypes.UNIT, new ScalarTypeInfo(interfaces(), members()));
        
        for (int i = 0; i < 20; i += 1) {
            ParameterisedType functionType = CoreTypes.functionType(i);
            FullyQualifiedName name = functionType.getBaseType().getFullyQualifiedName();
            GlobalDeclaration declaration = globalDeclaration(name);
            staticContext.add(declaration, ValueInfo.unassignableValue(functionType));
            staticContext.addBuiltIn(name.last(), declaration);
        }
        
        return staticContext;
    }

    private static void addCore(StaticContext staticContext, ClassType type, ScalarTypeInfo typeInfo) {
        FullyQualifiedName name = type.getFullyQualifiedName();
        GlobalDeclaration declaration = globalDeclaration(name);
        staticContext.addClass(declaration, type, typeInfo);
        staticContext.addBuiltIn(name.last(), declaration);
    }
    
    private static ScalarTypeInfo numberTypeInfo(Type numberType) {
        MembersBuilder members = Members.builder();
        members.add("equals", unassignableValue(CoreTypes.functionTypeOf(numberType, CoreTypes.BOOLEAN)));
        members.add("add", unassignableValue(CoreTypes.functionTypeOf(numberType, numberType)));
        members.add("subtract", unassignableValue(CoreTypes.functionTypeOf(numberType, numberType)));
        members.add("multiply", unassignableValue(CoreTypes.functionTypeOf(numberType, numberType)));
        members.add("toString", unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING)));
        return new ScalarTypeInfo(interfaces(), members.build());
    }
}
