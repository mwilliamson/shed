package org.zwobble.shed.compiler.parsing;

public interface ParseAction<F, T> {
    Result<T> apply(F result);
}
