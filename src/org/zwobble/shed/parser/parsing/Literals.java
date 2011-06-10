package org.zwobble.shed.parser.parsing;

import org.zwobble.shed.parser.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.parser.tokeniser.Keyword;
import org.zwobble.shed.parser.tokeniser.TokenType;

import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.parsing.Rules.firstOf;
import static org.zwobble.shed.parser.parsing.Rules.keyword;
import static org.zwobble.shed.parser.parsing.Rules.then;
import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;

public class Literals {
    public static Rule<NumberLiteralNode> numberLiteral() {
        return then(tokenOfType(TokenType.NUMBER), new ParseAction<String, NumberLiteralNode>() {
            @Override
            public Result<NumberLiteralNode> apply(String result) {
                return success(new NumberLiteralNode(result));
            }
        });
    }

    public static Rule<StringLiteralNode> stringLiteral() {
        return then(tokenOfType(TokenType.STRING), new ParseAction<String, StringLiteralNode>() {
            @Override
            public Result<StringLiteralNode> apply(String result) {
                return success(new StringLiteralNode(result));
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public static Rule<BooleanLiteralNode> booleanLiteral() {
        return then(
            firstOf("Boolean literal", keyword(Keyword.TRUE), keyword(Keyword.FALSE)),
            new ParseAction<Keyword, BooleanLiteralNode>() {
                @Override
                public Result<BooleanLiteralNode> apply(Keyword result) {
                    return success(new BooleanLiteralNode(result == Keyword.TRUE));
                }
            }
        );
    }
}
