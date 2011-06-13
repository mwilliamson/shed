package org.zwobble.shed.compiler.parsing;


public class Separator<T> implements Rule<T> {
    public static <T> Separator<T> softSeparator(Rule<T> rule) {
        return new Separator<T>(rule, Type.SOFT);
    }
    
    public static <T> Separator<T> hardSeparator(Rule<T> rule) {
        return new Separator<T>(rule, Type.HARD);
    }
    
    private final Rule<T> rule;
    private final Type type;
    
    private Separator(Rule<T> rule, Type type) {
        this.rule = rule;
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
    
    public static enum Type {
        SOFT,
        HARD
    }

    @Override
    public Result<T> parse(TokenIterator tokens) {
        return rule.parse(tokens);
    }
}
