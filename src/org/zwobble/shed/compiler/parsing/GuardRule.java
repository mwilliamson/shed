package org.zwobble.shed.compiler.parsing;


public class GuardRule<T> implements Rule<T> {
    private final Rule<T> rule;

    public GuardRule(Rule<T> rule) {
        this.rule = rule;
    }
    
    @Override
    public ParseResult<T> parse(TokenNavigator tokens) {
        return rule.parse(tokens);
    }
}
