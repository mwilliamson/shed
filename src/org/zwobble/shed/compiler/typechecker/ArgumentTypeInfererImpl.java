package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ArgumentTypeInfererImpl implements ArgumentTypeInferer {
    private final TypeLookup typeLookup;
    private final StaticContext context;

    @Inject
    public ArgumentTypeInfererImpl(TypeLookup typeLookup, StaticContext context) {
        this.typeLookup = typeLookup;
        this.context = context;
    }
    
    @Override
    public TypeResult<List<Type>> inferArgumentTypesAndAddToContext(List<FormalArgumentNode> formalArguments) {
        List<TypeResult<Type>> results = Lists.transform(formalArguments, inferArgumentType());
        return TypeResults.combine(results);
    }

    private Function<FormalArgumentNode, TypeResult<Type>> inferArgumentType() {
        return new Function<FormalArgumentNode, TypeResult<Type>>() {
            @Override
            public TypeResult<Type> apply(FormalArgumentNode argument) {
                TypeResultWithValue<Type> lookupTypeReference = typeLookup.lookupTypeReference(argument.getType());
                context.add(argument, ValueInfo.unassignableValue(lookupTypeReference.get()));
                return lookupTypeReference;
            }
        };
    }

}
