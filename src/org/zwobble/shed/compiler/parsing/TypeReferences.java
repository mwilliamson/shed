package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;

import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.optional;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.whitespace;


public class TypeReferences {
    public static Rule<ExpressionNode> typeSpecifier() {
        final Rule<ExpressionNode> typeReference = Expressions.expression();
        return then(
            sequence(OnError.FINISH,
                guard(symbol(":")),
                optional(whitespace()),
                typeReference
            ),
            new ParseAction<RuleValues, ExpressionNode>() {
                @Override
                public ExpressionNode apply(RuleValues result) {
                    return result.get(typeReference);
                }
            }
        );
    }
}
