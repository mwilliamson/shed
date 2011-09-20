package org.zwobble.shed.compiler.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

import lombok.Data;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static com.google.common.collect.Lists.transform;

@Data
public class TypeApplication implements ScalarType {
    public static Type applyTypes(TypeFunction typeFunction, List<Type> typeParameters) {
        TypeReplacer typeReplacer = new TypeReplacer();
        Builder<FormalTypeParameter, Type> replacementsBuilder = ImmutableMap.builder();
        
        for (int i = 0; i < typeFunction.getTypeParameters().size(); i++) {
            replacementsBuilder.put(typeFunction.getTypeParameters().get(i), typeParameters.get(i));
        }
        if (typeFunction instanceof ParameterisedFunctionType) {
            return typeReplacer.replaceTypes(((ParameterisedFunctionType) typeFunction).getBaseFunctionType(), replacementsBuilder.build());
        } else {
            ScalarType baseType = ((ParameterisedType)typeFunction).getBaseType();
            return new TypeApplication(
                (ScalarType)typeReplacer.replaceTypes(baseType, replacementsBuilder.build()),
                baseType,
                typeParameters
            );
        }
    }

    private final ScalarType replacedType;
    private final ScalarType baseType;
    private final List<Type> typeParameters;
    
    @Override
    public String shortName() {
        Iterable<String> typeParameterNames = transform(typeParameters, toShortName());
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
    @Override
    public Set<InterfaceType> superTypes() {
        return replacedType.superTypes();
    }
    
    @Override
    public Map<String, ValueInfo> getMembers() {
        return replacedType.getMembers();
    }
}
