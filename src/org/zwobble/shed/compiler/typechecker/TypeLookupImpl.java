package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.Types;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.errors.CompilerErrors.error;

import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

public class TypeLookupImpl implements TypeLookup {
    private final TypeInferer typeInferer;
    private final MetaClasses metaClasses;

    @Inject
    public TypeLookupImpl(TypeInferer typeInferer, MetaClasses metaClasses) {
        this.typeInferer = typeInferer;
        this.metaClasses = metaClasses;
    }
    
    public TypeResultWithValue<Type> lookupTypeReference(ExpressionNode typeReference) {
        return typeInferer.inferType(typeReference).ifValueThen(extractType(typeReference)).orElse(Types.newUnknown());
    }

    private Function<Type, TypeResult<Type>> extractType(final ExpressionNode expression) {
        return new Function<Type, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(Type variableType) {
                if (!metaClasses.isMetaClass(variableType)) {
                    return failure(error(expression, ("Not a type but an instance of \"" + variableType.shortName() + "\"")));
                }
                
                return success(metaClasses.getTypeFromMetaClass(variableType));
            }
        };
    }
}
