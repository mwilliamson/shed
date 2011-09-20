package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;

import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;


public class TypeReferences {
    public static Rule<ExpressionNode> typeSpecifier() {
        final Rule<ExpressionNode> typeReference = Expressions.typeExpression();
        return then(
            sequence(OnError.FINISH,
                guard(symbol(":")),
                typeReference
            ),
            new SimpleParseAction<RuleValues, ExpressionNode>() {
                @Override
                public ExpressionNode apply(RuleValues result) {
                    return result.get(typeReference);
                }
            }
        );
    }
}
