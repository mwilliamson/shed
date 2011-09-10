package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.UnitLiteralNode;
import org.zwobble.shed.compiler.tokeniser.Keyword;
import org.zwobble.shed.compiler.tokeniser.TokenType;

import static org.zwobble.shed.compiler.parsing.Rules.firstOf;
import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.keyword;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;


public class Literals {
    public static Rule<NumberLiteralNode> numberLiteral() {
        return then(tokenOfType(TokenType.NUMBER), new SimpleParseAction<String, NumberLiteralNode>() {
            @Override
            public NumberLiteralNode apply(String result) {
                return new NumberLiteralNode(result);
            }
        });
    }

    public static Rule<StringLiteralNode> stringLiteral() {
        return then(tokenOfType(TokenType.STRING), new SimpleParseAction<String, StringLiteralNode>() {
            @Override
            public StringLiteralNode apply(String result) {
                return new StringLiteralNode(result);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public static Rule<BooleanLiteralNode> booleanLiteral() {
        return then(
            firstOf("Boolean literal", keyword(Keyword.TRUE), keyword(Keyword.FALSE)),
            new SimpleParseAction<Keyword, BooleanLiteralNode>() {
                @Override
                public BooleanLiteralNode apply(Keyword result) {
                    return new BooleanLiteralNode(result == Keyword.TRUE);
                }
            }
        );
    }

    public static Rule<UnitLiteralNode> unitLiteral() {
        return then(
            sequence(OnError.FINISH,
                guard(symbol("(")),
                guard(symbol(")"))
            ),
            new SimpleParseAction<RuleValues, UnitLiteralNode>() {
                @Override
                public UnitLiteralNode apply(RuleValues result) {
                    return Nodes.unit();
                }
            }
        );
    }
}
