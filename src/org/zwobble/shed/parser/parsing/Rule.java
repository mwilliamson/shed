package org.zwobble.shed.parser.parsing;

public interface Rule<T> {
    Result<T> parse(TokenIterator tokens);
}
