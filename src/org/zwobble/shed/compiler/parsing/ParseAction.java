package org.zwobble.shed.compiler.parsing;

public interface ParseAction<F, T> {
    T apply(F result);
}
