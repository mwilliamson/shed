package org.zwobble.shed.compiler.types;

import java.util.List;
import java.util.Map;

import org.zwobble.shed.compiler.typechecker.ValueInfo;

import com.google.common.base.Function;

import static com.google.common.collect.Lists.transform;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.assignableValue;

public class TypeReplacer {
    public Type replaceTypes(Type type, Map<FormalTypeParameter, Type> replacements) {
        if (type instanceof FormalTypeParameter) {
            return replaceFormalTypeParameter(type, replacements);
        }
        
        if (type instanceof TypeApplication) {
            TypeApplication typeApplication = (TypeApplication) type;
            List<Type> transformedTypeParameters = transform(typeApplication.getTypeParameters(), toReplacement(replacements));
            return new TypeApplication((ParameterisedType)replaceTypes(typeApplication.getBaseType(), replacements), transformedTypeParameters);
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

    private Function<ValueInfo, ValueInfo> replaceType(final Map<FormalTypeParameter, Type> replacements) {
        return new Function<ValueInfo, ValueInfo>() {
            @Override
            public ValueInfo apply(ValueInfo input) {
                Type replacedType = replaceTypes(input.getType(), replacements);
                if (input.isAssignable()) {
                    return assignableValue(replacedType);
                } else {
                    return ValueInfo.unassignableValue(replacedType);
                }
            }
        };
    }
}
