package org.zwobble.shed.compiler.util;

public interface Predicate3<T, U, V> {
    boolean apply(T first, U second, V third);
}
