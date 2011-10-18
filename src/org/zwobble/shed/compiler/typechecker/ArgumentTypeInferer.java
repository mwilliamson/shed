package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static com.google.common.collect.Lists.transform;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ArgumentTypeInferer {
    private final TypeLookup typeLookup;
    private final StaticContext context;

    @Inject
    public ArgumentTypeInferer(TypeLookup typeLookup, StaticContext context) {
        this.typeLookup = typeLookup;
        this.context = context;
    }
    
    public TypeResult<List<Type>>
    inferArgumentTypesAndAddToContext(List<FormalArgumentNode> formalArguments) {
        List<TypeResult<Type>> results = transform(formalArguments, inferArgumentType());
        return TypeResult.combine(results);
    }

    private Function<FormalArgumentNode, TypeResult<Type>> inferArgumentType() {
        return new Function<FormalArgumentNode, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(FormalArgumentNode argument) {
                TypeResult<Type> lookupTypeReference = typeLookup.lookupTypeReference(argument.getType());
                if (lookupTypeReference.hasValue()) {
                    context.add(argument, unassignableValue(lookupTypeReference.get()));
                }
                return lookupTypeReference;
            }
        };
    }
}
