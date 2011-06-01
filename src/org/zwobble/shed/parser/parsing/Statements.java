package org.zwobble.shed.parser.parsing;

import java.util.Arrays;

import org.zwobble.shed.parser.parsing.nodes.ExpressionNode;
import org.zwobble.shed.parser.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.parser.tokeniser.Keyword;

import static org.zwobble.shed.parser.parsing.Expressions.expression;
import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.parsing.Rules.guard;
import static org.zwobble.shed.parser.parsing.Rules.keyword;
import static org.zwobble.shed.parser.parsing.Rules.last;
import static org.zwobble.shed.parser.parsing.Rules.optional;
import static org.zwobble.shed.parser.parsing.Rules.sequence;
import static org.zwobble.shed.parser.parsing.Rules.symbol;
import static org.zwobble.shed.parser.parsing.Rules.then;
import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;
import static org.zwobble.shed.parser.parsing.Rules.whitespace;
import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;

public class Statements {
    public static Rule<ImmutableVariableNode> immutableVariable() {
        return variable(Keyword.VAL, new VariableNodeConstructor<ImmutableVariableNode>() {
            @Override
            public ImmutableVariableNode apply(String identifier, ExpressionNode expression) {
                return new ImmutableVariableNode(identifier, expression);
            }
        });
    }

    public static Rule<MutableVariableNode> mutableVariable() {
        return variable(Keyword.VAR, new VariableNodeConstructor<MutableVariableNode>() {
            @Override
            public MutableVariableNode apply(String identifier, ExpressionNode expression) {
                return new MutableVariableNode(identifier, expression);
            }
        });
    }
    
    public static Rule<RuleValues> aStatement(OnError recovery, Rule<?>... rules) {
        Rule<?>[] statementRules = Arrays.copyOf(rules, rules.length + 1);
        statementRules[rules.length] = last(symbol(";"));
        return sequence(recovery, statementRules);
    }

    private static <T> Rule<T> variable(Keyword keyword, final VariableNodeConstructor<T> constructor) {
        final Rule<String> identifier = tokenOfType(IDENTIFIER);
        final Rule<? extends ExpressionNode> expression = expression(); 
        return then(
            aStatement(OnError.FINISH,
                guard(keyword(keyword)), whitespace(),
                identifier, optional(whitespace()),
                symbol("="), optional(whitespace()),
                expression, optional(whitespace())
            ),
            new ParseAction<RuleValues, T>() {
                @Override
                public Result<T> apply(RuleValues result) {
                    return success(constructor.apply(result.get(identifier), result.get(expression)));
                }
            }
        );
    }
    
    private interface VariableNodeConstructor<T> {
        T apply(String identifier, ExpressionNode expression);
    }
}
