package org.zwobble.shed.compiler.parsing;

public interface Rule<T> {
    ParseResult<T> parse(TokenIterator tokens);
}
