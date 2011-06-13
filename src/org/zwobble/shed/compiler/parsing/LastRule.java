package org.zwobble.shed.compiler.parsing;


public class LastRule<T> implements Rule<T> {
    private final Rule<T> rule;

    public LastRule(Rule<T> rule) {
        this.rule = rule;
    }
    
    @Override
    public Result<T> parse(TokenIterator tokens) {
        return rule.parse(tokens);
    }
}
