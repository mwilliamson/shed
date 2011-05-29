package org.zwobble.shed.parser.parsing;

import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.parser.tokeniser.TokenType;

import static org.zwobble.shed.parser.parsing.Result.success;

import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;

public class Literals {
    public static Rule<NumberLiteralNode> numberLiteral() {
        return Rules.then(tokenOfType(TokenType.NUMBER), new ParseAction<String, NumberLiteralNode>() {
            @Override
            public Result<NumberLiteralNode> apply(String result) {
                return success(new NumberLiteralNode(result));
            }
        });
    }
}
