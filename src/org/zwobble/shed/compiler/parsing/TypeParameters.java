package org.zwobble.shed.compiler.parsing;

import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.FormalTypeParameterNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalTypeParametersNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.tokeniser.TokenType;

import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;

import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.zeroOrMoreWithSeparator;
import static org.zwobble.shed.compiler.parsing.Separator.hardSeparator;

public class TypeParameters {
    public static Rule<FormalTypeParametersNode> formalTypeParameters() {
        final Rule<List<FormalTypeParameterNode>> parameters = zeroOrMoreWithSeparator(formalTypeParameter(), hardSeparator(guard(symbol(","))));
        return then(
            sequence(OnError.FINISH,
                guard(symbol("[")),
                parameters,
                symbol("]")
            ),
            new SimpleParseAction<RuleValues, FormalTypeParametersNode>() {
                @Override
                public FormalTypeParametersNode apply(RuleValues result) {
                    return Nodes.formalTypeParameters(result.get(parameters));
                }
            }
        );
    }

    private static Rule<FormalTypeParameterNode> formalTypeParameter() {
        final Rule<String> identifier = tokenOfType(TokenType.IDENTIFIER);
        return then(
            sequence(OnError.FINISH, identifier),
            new SimpleParseAction<RuleValues, FormalTypeParameterNode>() {
                @Override
                public FormalTypeParameterNode apply(RuleValues result) {
                    return new FormalTypeParameterNode(result.get(identifier));
                }
            }
        );
    }
}
