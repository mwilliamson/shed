package org.zwobble.shed.compiler.types;

import java.util.Collections;

import static java.util.Arrays.asList;

public class CoreTypes {
    public static final Type BOOLEAN = new ScalarType(Collections.<String>emptyList(), "Boolean");
    public static final Type NUMBER = new ScalarType(Collections.<String>emptyList(), "Number");
    public static final Type STRING = new ScalarType(Collections.<String>emptyList(), "String");
    public static final Type UNIT = new ScalarType(Collections.<String>emptyList(), "Unit");
    public static final TypeFunction CLASS = new TypeFunction(Collections.<String>emptyList(), "Class", asList(new FormalTypeParameter("C")));
    
    public static TypeFunction functionType() {
        return new TypeFunction(Collections.<String>emptyList(), "Function", Collections.<FormalTypeParameter>emptyList());
    }
}
