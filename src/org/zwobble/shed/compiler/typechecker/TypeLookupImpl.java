package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class TypeLookupImpl implements TypeLookup {
    private final TypeInferer typeInferer;

    @Inject
    public TypeLookupImpl(TypeInferer typeInferer) {
        this.typeInferer = typeInferer;
    }
    
    public TypeResult<Type>
    lookupTypeReference(ExpressionNode typeReference) {
        return typeInferer.inferType(typeReference).ifValueThen(extractType(typeReference));
    }

    private static Function<Type, TypeResult<Type>> extractType(final ExpressionNode expression) {
        return new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type variableType) {
                if (!isType(variableType)) {
                    return failure(error(expression, ("Not a type but an instance of \"" + variableType.shortName() + "\"")));
                }
                
                Type type = ((TypeApplication)variableType).getTypeParameters().get(0);
                return success(type);
            }
        };
    }
    
    private static boolean isType(Type type) {
        return type instanceof TypeApplication && ((TypeApplication)type).getBaseType().equals(CoreTypes.CLASS);
    }

}
