package org.zwobble.shed.compiler.types;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import static com.google.common.collect.Iterables.transform;

@Getter
@EqualsAndHashCode
@ToString
public class TypeApplication implements Type {
    public static Type applyTypes(TypeFunction typeFunction, List<Type> typeParameters) {
        return new TypeApplication(typeFunction, typeParameters);
    }
    
    private TypeApplication(TypeFunction typeFunction, List<Type> typeParameters) {
        this.typeFunction = typeFunction;
        this.typeParameters = typeParameters;
    }
    
    private final TypeFunction typeFunction;
    private final List<Type> typeParameters;
    
    @Override
    public String shortName() {
        Iterable<String> typeParameterNames = transform(typeParameters, toShortName());
        Type baseType = typeFunction instanceof ParameterisedType ? ((ParameterisedType)typeFunction).getBaseType() : typeFunction;
        return baseType.shortName() + "[" + Joiner.on(", ").join(typeParameterNames) + "]";
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
