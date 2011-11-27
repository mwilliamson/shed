package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;

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
        return TypeResult.success(interfaces(transform(interfaces, lookupType())));
    }

    private Function<ExpressionNode, ScalarType> lookupType() {
        return new Function<ExpressionNode, ScalarType>() {
            @Override
            public ScalarType apply(ExpressionNode input) {
                TypeResult<Type> lookupResult = typeLookup.lookupTypeReference(input);
                if (!lookupResult.isSuccess()) {
                    // TODO:
                    throw new RuntimeException("Failed type lookup " + lookupResult.getErrors());
                }
                // TODO: handle non-scalar types
                return (ScalarType)lookupResult.get();
            }
        };
    }
}
