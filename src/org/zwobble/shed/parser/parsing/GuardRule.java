package org.zwobble.shed.parser.parsing;

import org.zwobble.shed.parser.tokeniser.TokenPosition;

import com.google.common.collect.PeekingIterator;

public class GuardRule<T> implements Rule<T> {
    private final Rule<T> rule;

    public GuardRule(Rule<T> rule) {
        this.rule = rule;
    }
    
    @Override
    public Result<T> parse(PeekingIterator<TokenPosition> tokens) {
        return rule.parse(tokens);
    }
}
