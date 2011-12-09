package org.zwobble.shed.compiler.types;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;

import static org.zwobble.shed.compiler.types.TypeApplication.applyTypes;

import static com.google.common.collect.Lists.transform;

public class TypeReplacer {
    public Type replaceTypes(Type type, Map<FormalTypeParameter, Type> replacements) {
        if (type instanceof ScalarFormalTypeParameter) {
            return replaceFormalTypeParameter(type, replacements);
        }
        
        if (type instanceof TypeApplication) {
            TypeApplication typeApplication = (TypeApplication) type;
            List<Type> transformedTypeParameters = transform(typeApplication.getTypeParameters(), toReplacement(replacements));
            return applyTypes((ParameterisedType)replaceTypes(typeApplication.getParameterisedType(), replacements), transformedTypeParameters);
        }
        return type;
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
