package org.zwobble.shed.compiler.types;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import static com.google.common.collect.Iterables.transform;

import lombok.Data;

@Data
public class ParameterisedFunctionType implements TypeFunction {
    private final List<Type> functionTypeParameters;
    private final List<FormalTypeParameter> formalTypeParameters;
    
    @Override
    public String shortName() {
        return buildTypeList(formalTypeParameters) + " -> Function" + buildTypeList(functionTypeParameters);
    }
    
    private String buildTypeList(List<? extends Type> types) {
        return "[" + Joiner.on(", ").join(transform(types, toName())) + "]";
    }
    
    private Function<Type, String> toName() {
        return new Function<Type, String>() {
            @Override
            public String apply(Type input) {
                return input.shortName();
            }
        };
    }
}
