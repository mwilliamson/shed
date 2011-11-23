package org.zwobble.shed.compiler.parsing;

public class ParseActions {
    public static <T> SimpleParseAction<RuleValues, T> extract(final Rule<T> rule) {
        return new SimpleParseAction<RuleValues, T>() {
            @Override
            public T apply(RuleValues result) {
                return result.get(rule);
            }
        };
    }
}
