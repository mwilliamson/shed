package org.zwobble.shed.compiler.parsing;

public interface Rule<T> {
    Result<T> parse(TokenIterator tokens);
}
