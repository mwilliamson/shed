package org.zwobble.shed.compiler.parsing.nodes;

import static java.util.Arrays.asList;

public class Nodes {
    public static VariableIdentifierNode id(String identifier) {
        return new VariableIdentifierNode(identifier);
    }

    public static CallNode call(ExpressionNode function, ExpressionNode... arguments) {
        return new CallNode(function, asList(arguments));
    }

    public static NumberLiteralNode number(String value) {
        return new NumberLiteralNode(value);
    }

    public static StringLiteralNode string(String value) {
        return new StringLiteralNode(value);
    }
    
    public static ExpressionStatementNode expressionStatement(ExpressionNode expression) {
        return new ExpressionStatementNode(expression);
    }
}
