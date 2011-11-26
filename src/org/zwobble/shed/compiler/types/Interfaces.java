package org.zwobble.shed.compiler.types;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class Interfaces {
    public static Set<ScalarType> interfaces(ScalarType... interfaces) {
        return newHashSet(interfaces);
    }
}
