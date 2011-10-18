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
    
//    public static Type applyTypes(TypeFunction typeFunction, List<? extends Type> typeParameters) {
//        TypeReplacer typeReplacer = new TypeReplacer();
//        Builder<FormalTypeParameter, Type> replacementsBuilder = ImmutableMap.builder();
//        
//        for (int i = 0; i < typeFunction.getTypeParameters().size(); i++) {
//            replacementsBuilder.put(typeFunction.getTypeParameters().get(i), typeParameters.get(i));
//        }
//        if (typeFunction instanceof ParameterisedFunctionType) {
//            return typeReplacer.replaceTypes(((ParameterisedFunctionType) typeFunction).getBaseFunctionType(), replacementsBuilder.build());
//        } else {
//            ScalarType baseType = ((ParameterisedType)typeFunction).getBaseType();
//            return new TypeApplication(
//                baseType,
//                typeParameters
//            );
//        }
//    }

//    private final ScalarType baseType;
//    private final List<? extends Type> typeParameters;
//    
//    @Override
//    public String shortName() {
//        Iterable<String> typeParameterNames = transform(typeParameters, toShortName());
//        return baseType.shortName() + "[" + Joiner.on(", ").join(typeParameterNames) + "]";
//    }
//    
//    @Override
//    public FullyQualifiedName getFullyQualifiedName() {
//        return baseType.getFullyQualifiedName().replaceLast(shortName());
//    }
//    
//    private Function<Type, String> toShortName() {
//        return new Function<Type, String>() {
//            @Override
//            public String apply(Type input) {
//                return input.shortName();
//            }
//        };
//    }
}
