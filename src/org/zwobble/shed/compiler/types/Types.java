package org.zwobble.shed.compiler.types;

import java.util.List;

import static java.util.Arrays.asList;

public class Types {
    public static List<Type> typeParameters(Type... types) {
        return asList(types);
    }

    public static Type newUnknown() {
        return new UnknownType();
    }
}
