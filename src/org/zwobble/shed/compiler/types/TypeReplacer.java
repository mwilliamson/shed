package org.zwobble.shed.compiler.types;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.google.common.collect.Lists.transform;

public class TypeReplacer {
    public Type replaceTypes(Type type) {
        return replaceTypes(type, ImmutableMap.<FormalTypeParameter, Type>of());
    }
    
    public Type replaceTypes(Type type, Map<FormalTypeParameter, Type> replacements) {
        if (type instanceof FormalTypeParameter) {
            return replaceFormalTypeParameter(type, replacements);
        }
        
        if (type instanceof ClassType) {
            return type;
        }
        
        if (type instanceof InterfaceType) {
            return type;
        }
        
        if (type instanceof TypeApplication) {
            TypeApplication typeApplication = (TypeApplication) type;
            List<Type> transformedTypeParameters = transform(typeApplication.getTypeParameters(), toReplacement(replacements));
            return new TypeApplication((ScalarType)replaceTypes(typeApplication.getReplacedType()), typeApplication.getBaseType(), transformedTypeParameters);
        }
        
        throw new RuntimeException("Don't know how to replace types for " + type);
    }

    private Type replaceFormalTypeParameter(Type type, Map<FormalTypeParameter, Type> replacements) {
        if (replacements.containsKey(type)) {
            return replacements.get(type);
        }
        return type;
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
