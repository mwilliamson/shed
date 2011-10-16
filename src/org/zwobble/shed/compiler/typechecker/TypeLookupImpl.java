package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

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

public class TypeLookupImpl implements TypeLookup {
    private final TypeInferer typeInferer;
    private final NodeLocations nodeLocations;

    @Inject
    public TypeLookupImpl(TypeInferer typeInferer, NodeLocations nodeLocations) {
        this.typeInferer = typeInferer;
        this.nodeLocations = nodeLocations;
    }
    
    public TypeResult<Type>
    lookupTypeReference(ExpressionNode typeReference, StaticContext context) {
        SourceRange nodeLocation = nodeLocations.locate(typeReference);
        return typeInferer.inferType(typeReference, context).ifValueThen(extractType(nodeLocation));
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
                
                Type type = ((TypeApplication)variableType).getTypeParameters().get(0);
                return success(type);
            }
        };
    }
    
    private static boolean isType(Type type) {
        return type instanceof TypeApplication && ((TypeApplication)type).getBaseType().equals(CoreTypes.CLASS.getBaseType());
    }

}
