package org.zwobble.shed.compiler.types;

import lombok.Data;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import static com.google.common.collect.Iterables.transform;

@Data(staticConstructor="parameterisedType")
public class ParameterisedType implements TypeFunction {
    private final ScalarType baseType;
    private final FormalTypeParameters formalTypeParameters;
    
    @Override
    public String shortName() {
        String parameterString = "[" + Joiner.on(", ").join(transform(formalTypeParameters, toName())) + "]"; 
        return parameterString + " -> Class[" + baseType.shortName() + parameterString + "]";
    }
    
    private Function<FormalTypeParameter, String> toName() {
        return new Function<FormalTypeParameter, String>() {
            @Override
            public String apply(FormalTypeParameter input) {
                return input.shortName();
            }
        };
    }
}
