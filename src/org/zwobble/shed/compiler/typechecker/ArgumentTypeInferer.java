package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static com.google.common.collect.Lists.transform;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ArgumentTypeInferer {
    public static TypeResult<List<Type>>
    inferArgumentTypesAndAddToContext(List<FormalArgumentNode> formalArguments, NodeLocations nodeLocations, StaticContext context) {
        List<TypeResult<Type>> results = transform(formalArguments, inferArgumentType(nodeLocations, context));
        return TypeResult.combine(results);
    }

    private static Function<FormalArgumentNode, TypeResult<Type>>
    inferArgumentType(final NodeLocations nodeLocations, final StaticContext context) {
        return new Function<FormalArgumentNode, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(FormalArgumentNode argument) {
                TypeResult<Type> lookupTypeReference = lookupTypeReference(argument.getType(), nodeLocations, context);
                if (lookupTypeReference.hasValue()) {
                    context.add(argument, unassignableValue(lookupTypeReference.get()));
                }
                return lookupTypeReference;
            }
        };
    }
}
