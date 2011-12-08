package org.zwobble.shed.compiler.util;

public interface Function2<T1, T2, R> {
    R apply(T1 first, T2 second);
}
