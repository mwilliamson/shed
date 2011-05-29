package org.zwobble.shed.parser.parsing;

import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.StringLiteralNode;
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

    public static Rule<StringLiteralNode> stringLiteral() {
        return Rules.then(tokenOfType(TokenType.STRING), new ParseAction<String, StringLiteralNode>() {
            @Override
            public Result<StringLiteralNode> apply(String result) {
                return success(new StringLiteralNode(result));
            }
        });
    }
}
