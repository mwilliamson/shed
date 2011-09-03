package org.zwobble.shed.compiler.types;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import static com.google.common.collect.Iterables.transform;

import lombok.Data;

@Data
public class ParameterisedFunctionType implements TypeFunction {
    private final Type baseFunctionType;
    private final List<FormalTypeParameter> typeParameters;
    
    @Override
    public String shortName() {
        String parameterString = "[" + Joiner.on(", ").join(transform(typeParameters, toName())) + "]";
        return parameterString + " -> " + baseFunctionType.shortName();
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
