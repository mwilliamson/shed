package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerErrorWithSyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.typechecker.errors.NotAnInterfaceError;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.Types;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

public class InterfaceDereferencer {
    private final TypeLookup typeLookup;

    @Inject
    public InterfaceDereferencer(TypeLookup typeLookup) {
        this.typeLookup = typeLookup;
    }
    
    public TypeResult<Interfaces> dereferenceInterfaces(List<ExpressionNode> interfaces) {
        TypeResult<List<ScalarType>> result = TypeResult.combine(transform(interfaces, lookupType())); 
        return result.ifValueThen(toInterfaces());
    }

    private Function<List<ScalarType>, TypeResult<Interfaces>> toInterfaces() {
        return new Function<List<ScalarType>, TypeResult<Interfaces>>() {
            @Override
            public TypeResult<Interfaces> apply(List<ScalarType> input) {
                return TypeResult.success(interfaces(input));
            }
        };
    }

    private Function<ExpressionNode, TypeResult<ScalarType>> lookupType() {
        return new Function<ExpressionNode, TypeResult<ScalarType>>() {
            @Override
            public TypeResult<ScalarType> apply(ExpressionNode input) {
                TypeResult<Type> lookupResult = typeLookup.lookupTypeReference(input);
                // We should always get a result -- if we fail, we get unknown type
                // TODO: handle non-scalar types
                Type type = lookupResult.get();
                if (Types.isInterface(type)) {
                    return TypeResult.success((ScalarType)type);
                } else if (lookupResult.isSuccess()) {
                    return TypeResult.failure(new CompilerErrorWithSyntaxNode(input, new NotAnInterfaceError(type)));
                } else {
                    return TypeResult.failure(lookupResult.getErrors());
                }
            }
        };
    }
}
