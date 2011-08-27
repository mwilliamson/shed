package org.zwobble.shed.compiler.parsing.nodes;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.Option.none;

import java.util.List;

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
    
    public static ImmutableVariableNode immutableVar(String name, ExpressionNode expression) {
        return new ImmutableVariableNode(name, none(TypeReferenceNode.class), expression);
    }

    public static ObjectDeclarationNode object(String name, List<StatementNode> statements) {
        return new ObjectDeclarationNode(name, statements);
    }
}
