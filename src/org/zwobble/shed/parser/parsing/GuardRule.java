package org.zwobble.shed.parser.parsing;


public class GuardRule<T> implements Rule<T> {
    private final Rule<T> rule;

    public GuardRule(Rule<T> rule) {
        this.rule = rule;
    }
    
    @Override
    public Result<T> parse(TokenIterator tokens) {
        return rule.parse(tokens);
    }
}