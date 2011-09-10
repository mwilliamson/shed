package org.zwobble.shed.compiler.parsing;

public interface ParseAction<F, T> {
    ParseResult<T> apply(ParseResult<F> result);
}
