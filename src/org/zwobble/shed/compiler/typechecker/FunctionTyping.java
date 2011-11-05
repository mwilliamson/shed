package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.types.CoreTypes.isFunctionType;

public class FunctionTyping {
    private final StaticContext context;
    
    @Inject
    public FunctionTyping(StaticContext context) {
        this.context = context;
    }
    
    public boolean isFunction(ScalarType type) {
        return extractFunctionTypeParameters(type).hasValue();
    }
    
    public Option<List<Type>> extractFunctionTypeParameters(ScalarType type) {
        if (isFunctionType(type)) {
            return some(((TypeApplication)type).getTypeParameters());
        } else {
            Iterable<ScalarType> superTypes = filter(context.getInfo(type).getSuperTypes(), ScalarType.class);
            return getFirst(filter(transform(superTypes, toFunctionTypeParameters()), hasValue()), Option.<List<Type>>none());
        }
    }
    
    private Predicate<Option<?>> hasValue() {
        return new Predicate<Option<?>>() {
            @Override
            public boolean apply(Option<?> input) {
                return input.hasValue();
            }
        };
    }
    private Function<ScalarType, Option<List<Type>>> toFunctionTypeParameters() {
        return new Function<ScalarType, Option<List<Type>>>() {
            @Override
            public Option<List<Type>> apply(ScalarType input) {
                return extractFunctionTypeParameters(input);
            }
        };
    }
}
