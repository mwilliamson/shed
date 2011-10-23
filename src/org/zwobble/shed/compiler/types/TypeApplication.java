package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import static com.google.common.collect.Lists.transform;

@Data(staticConstructor="applyTypes")
public class TypeApplication implements ScalarType {
    private final ParameterisedType parameterisedType;
    private final List<Type> typeParameters;
  
    @Override
    public String shortName() {
        Iterable<String> typeParameterNames = transform(typeParameters, toShortName());
        return parameterisedType.shortName() + "[" + Joiner.on(", ").join(typeParameterNames) + "]";
    }
  
    @Override
    public FullyQualifiedName getFullyQualifiedName() {
        return parameterisedType.getBaseType().getFullyQualifiedName().replaceLast(shortName());
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
