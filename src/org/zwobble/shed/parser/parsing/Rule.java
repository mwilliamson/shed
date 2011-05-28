package org.zwobble.shed.parser.parsing;

import org.zwobble.shed.parser.tokeniser.TokenPosition;

import com.google.common.collect.PeekingIterator;

public interface Rule<T> {
    Result<T> parse(PeekingIterator<TokenPosition> tokens);
}
