package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import com.google.common.base.Function;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class TypeLookup {
    public static TypeResult<Type>
    lookupTypeReference(ExpressionNode typeReference, NodeLocations nodeLocations, StaticContext context) {
        SourceRange nodeLocation = nodeLocations.locate(typeReference);
        return TypeInferer.inferType(typeReference, nodeLocations, context).ifValueThen(extractType(nodeLocation));
    }

    private static Function<Type, TypeResult<Type>> extractType(final SourceRange nodeLocation) {
        return new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type variableType) {
                if (!isType(variableType)) {
                    return failure(asList(CompilerError.error(
                        nodeLocation,
                        "Not a type but an instance of \"" + variableType.shortName() + "\""
                    )));
                }
                
                return success(((TypeApplication)variableType).getTypeParameters().get(0));
            }
        };
    }
    
    private static boolean isType(Type type) {
        return type instanceof TypeApplication && ((TypeApplication)type).getBaseType().equals(CoreTypes.CLASS.getBaseType());
    }
}
