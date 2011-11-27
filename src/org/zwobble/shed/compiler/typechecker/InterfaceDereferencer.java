package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerErrorWithSyntaxNode;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.Options;
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
        Iterable<TypeResult<ScalarType>> results = transform(interfaces, lookupType());
        List<ScalarType> dereferencedInterfaces = Options.flatten(transform(results, toOption()));
        return TypeResult.success(interfaces(dereferencedInterfaces)).withErrorsFrom(TypeResult.combine(results));
    }

    private Function<TypeResult<ScalarType>, Option<ScalarType>> toOption() {
        return new Function<TypeResult<ScalarType>, Option<ScalarType>>() {
            @Override
            public Option<ScalarType> apply(TypeResult<ScalarType> input) {
                return input.asOption();
            }
        };
    }

    private Function<ExpressionNode, TypeResult<ScalarType>> lookupType() {
        return new Function<ExpressionNode, TypeResult<ScalarType>>() {
            @Override
            public TypeResult<ScalarType> apply(ExpressionNode input) {
                TypeResult<Type> lookupResult = typeLookup.lookupTypeReference(input);
                // We should always get a result -- if we fail, we get unknown type
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
