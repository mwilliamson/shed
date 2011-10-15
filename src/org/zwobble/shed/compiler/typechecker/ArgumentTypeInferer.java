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

    @Inject
    public ArgumentTypeInferer(TypeLookup typeLookup) {
        this.typeLookup = typeLookup;
    }
    
    public TypeResult<List<Type>>
    inferArgumentTypesAndAddToContext(List<FormalArgumentNode> formalArguments, StaticContext context) {
        List<TypeResult<Type>> results = transform(formalArguments, inferArgumentType(context));
        return TypeResult.combine(results);
    }

    private Function<FormalArgumentNode, TypeResult<Type>>
    inferArgumentType(final StaticContext context) {
        return new Function<FormalArgumentNode, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(FormalArgumentNode argument) {
                TypeResult<Type> lookupTypeReference = typeLookup.lookupTypeReference(argument.getType(), context);
                if (lookupTypeReference.hasValue()) {
                    context.add(argument, unassignableValue(lookupTypeReference.get()));
                }
                return lookupTypeReference;
            }
        };
    }
}
