package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.Data;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import static com.google.common.collect.Iterables.transform;

@Data
public class TypeApplication implements Type {
    private final ParameterisedType typeFunction;
    private final List<Type> typeParameters;
    
    @Override
    public String shortName() {
        Iterable<String> typeParameterNames = transform(typeParameters, toShortName());
        return typeFunction.getBaseType().shortName() + "[" + Joiner.on(", ").join(typeParameterNames) + "]";
    }
    
    private Function<Type, String> toShortName() {
        return new Function<Type, String>() {
            @Override
            public String apply(Type input) {
                return input.shortName();
            }
        };
    }
}
