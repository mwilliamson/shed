package org.zwobble.shed.compiler.typebinding;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typegeneration.TypeStore;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.util.Pair;

import static org.zwobble.shed.compiler.typechecker.ShedTypeValue.shedTypeValue;

public class TypeBinder {
    private final MetaClasses metaClasses;

    public TypeBinder(MetaClasses metaClasses) {
        this.metaClasses = metaClasses;
    }
    
    public void bindTypes(TypeStore generatedTypes, StaticContext context) {
        for (Pair<TypeDeclarationNode, Type> generatedType : generatedTypes) {
            bindGeneratedType(context, generatedType);
        }
    }

    private void bindGeneratedType(StaticContext context, Pair<TypeDeclarationNode, Type> generatedType) {
        TypeDeclarationNode declaration = generatedType.getFirst();
        ScalarType type = (ScalarType) generatedType.getSecond();
        if (isSingletonType(declaration)) {
            context.add(declaration, ValueInfo.unassignableValue(type));
        } else {
            Type metaClass = metaClasses.metaClassOf(type);
            context.add(declaration, ValueInfo.unassignableValue(metaClass, shedTypeValue(type)));
        }
    }

    private boolean isSingletonType(TypeDeclarationNode declaration) {
        return declaration instanceof ObjectDeclarationNode;
    }
}
