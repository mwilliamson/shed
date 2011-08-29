package org.zwobble.shed.compiler.types;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static com.google.common.collect.Lists.transform;

public class TypeReplacer {
    public Type replaceTypes(Type type) {
        return replaceTypes(type, ImmutableMap.<FormalTypeParameter, Type>of());
    }
    
    public Type replaceTypes(Type type, Map<FormalTypeParameter, Type> replacements) {
        if (type instanceof FormalTypeParameter) {
            return replaceFormalTypeParameter(type, replacements);
        }
        
        if (type instanceof TypeApplication) {
            return replaceTypeApplication((TypeApplication)type, replacements);
        }
        
        throw new RuntimeException("Don't know how to replace types for " + type);
    }

    private Type replaceFormalTypeParameter(Type type, Map<FormalTypeParameter, Type> replacements) {
        if (replacements.containsKey(type)) {
            return replacements.get(type);
        }
        return type;
    }

    private Type replaceTypeApplication(TypeApplication typeApplication, Map<FormalTypeParameter, Type> replacements) {
        if (typeApplication.getTypeFunction() instanceof ParameterisedFunctionType) {
            ParameterisedFunctionType typeFunction = (ParameterisedFunctionType)typeApplication.getTypeFunction();
            
            Builder<FormalTypeParameter, Type> replacementsBuilder = ImmutableMap.builder();
            replacementsBuilder.putAll(replacements);
            
            for (int i = 0; i < typeFunction.getTypeParameters().size(); i++) {
                replacementsBuilder.put(typeFunction.getTypeParameters().get(i), typeApplication.getTypeParameters().get(i));
            }
            
            return replaceTypeApplication(typeFunction.getBaseFunctionType(), replacementsBuilder.build());
        } else {
            return new TypeApplication(typeApplication.getTypeFunction(), transform(typeApplication.getTypeParameters(), toReplacement(replacements)));
        }
    }

    private Function<Type, Type> toReplacement(final Map<FormalTypeParameter, Type> replacements) {
        return new Function<Type, Type>() {
            @Override
            public Type apply(Type input) {
                return replaceTypes(input, replacements);
            }
        };
    }
}
