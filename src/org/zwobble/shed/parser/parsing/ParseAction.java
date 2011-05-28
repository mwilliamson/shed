package org.zwobble.shed.parser.parsing;

public interface ParseAction<F, T> {
    Result<T> apply(Result<F> result);
}
