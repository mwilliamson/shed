package org.zwobble.shed.compiler.types;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class Interfaces {
    public static Set<Type> interfaces(Type... interfaces) {
        return newHashSet(interfaces);
    }
}
