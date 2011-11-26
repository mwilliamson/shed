package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class TypeLookupImpl implements TypeLookup {
    private final TypeInferer typeInferer;
    private final StaticContext context;

    @Inject
    public TypeLookupImpl(TypeInferer typeInferer, StaticContext context) {
        this.typeInferer = typeInferer;
        this.context = context;
    }
    
    public TypeResult<Type> lookupTypeReference(ExpressionNode typeReference) {
        return typeInferer.inferType(typeReference).ifValueThen(extractType(typeReference));
    }

    private Function<Type, TypeResult<Type>> extractType(final ExpressionNode expression) {
        return new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type variableType) {
                if (!context.isMetaClass(variableType)) {
                    return failure(error(expression, ("Not a type but an instance of \"" + variableType.shortName() + "\"")));
                }
                
                return success(context.getTypeFromMetaClass(variableType));
            }
        };
    }
}
