package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.Data;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import static com.google.common.collect.Iterables.transform;

@Data
public class TypeFunction implements Type {
    private final Type baseType;
    private final List<FormalTypeParameter> typeParameters;
    
    public String shortName() {
        String parameterString = "[" + Joiner.on(", ").join(transform(typeParameters, toName())) + "]"; 
        return parameterString + " -> Class[" + baseType.shortName() + parameterString + "]";
    }
    
    private Function<FormalTypeParameter, String> toName() {
        return new Function<FormalTypeParameter, String>() {
            @Override
            public String apply(FormalTypeParameter input) {
                return input.getName();
            }
        };
    }
}
