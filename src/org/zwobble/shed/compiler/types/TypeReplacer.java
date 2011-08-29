package org.zwobble.shed.compiler.types;

import java.util.Map;

public class TypeReplacer {
    public Type replaceTypes(Type type, Map<FormalTypeParameter, Type> replacements) {
        if (replacements.containsKey(type)) {
            return replacements.get(type);
        }
        return type;
    }
}
