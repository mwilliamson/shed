package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.MembersBuilder;
import org.zwobble.shed.compiler.types.ParameterisedType;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class DefaultContextInitialiser implements StaticContextInitialiser {
    @Override
    public void initialise(StaticContext staticContext, BuiltIns builtIns, MetaClasses metaClasses) {
        addCore(staticContext, builtIns, CoreTypes.STRING, new ScalarTypeInfo(interfaces(), members()));
        addCore(staticContext, builtIns, CoreTypes.BOOLEAN, new ScalarTypeInfo(interfaces(), members()));
        addCore(staticContext, builtIns, CoreTypes.DOUBLE, numberTypeInfo(CoreTypes.DOUBLE));
        addCore(staticContext, builtIns, CoreTypes.UNIT, new ScalarTypeInfo(interfaces(), members()));
        addCore(staticContext, builtIns, CoreTypes.CLASS, new ScalarTypeInfo(interfaces(), members()));
        
        addCoreParameterisedType(staticContext, builtIns, CoreTypes.FUNCTION);
        addCoreParameterisedType(staticContext, builtIns, CoreTypes.TUPLE);
    }

    private static void addCore(StaticContext staticContext, BuiltIns builtIns, ScalarType type, ScalarTypeInfo typeInfo) {
        FullyQualifiedName name = type.getFullyQualifiedName();
        GlobalDeclaration declaration = globalDeclaration(name);
        if (type instanceof ClassType) {
            staticContext.addClass(declaration, (ClassType)type, typeInfo);
        } else {
            staticContext.addInterface(declaration, (InterfaceType)type, typeInfo);
        }
        builtIns.add(name.last(), declaration);
    }

    private void addCoreParameterisedType(StaticContext staticContext, BuiltIns builtIns, ParameterisedType type) {
        FullyQualifiedName name = type.getBaseType().getFullyQualifiedName();
        GlobalDeclaration declaration = globalDeclaration(name);
        staticContext.add(declaration, ValueInfo.unassignableValue(type));
        builtIns.add(name.last(), declaration);
        staticContext.addInfo(type.getBaseType(), ScalarTypeInfo.EMPTY);
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
