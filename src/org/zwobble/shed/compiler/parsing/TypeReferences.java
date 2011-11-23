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
            ParseActions.extract(typeReference)
        );
    }
}
