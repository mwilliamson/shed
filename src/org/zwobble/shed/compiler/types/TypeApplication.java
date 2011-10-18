package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.naming.FullyQualifiedName;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import static com.google.common.collect.Lists.transform;

@Data
public class TypeApplication implements ScalarType {
    // TODO: store ScalarType directly, and store a map from formal type parameters to actual type parameters
    private final ParameterisedType baseType;
    private final List<Type> typeParameters;
  
    @Override
    public String shortName() {
        Iterable<String> typeParameterNames = transform(typeParameters, toShortName());
        return baseType.shortName() + "[" + Joiner.on(", ").join(typeParameterNames) + "]";
    }
  
    @Override
    public FullyQualifiedName getFullyQualifiedName() {
        return baseType.getBaseType().getFullyQualifiedName().replaceLast(shortName());
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
