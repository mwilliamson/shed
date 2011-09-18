package org.zwobble.shed.compiler.parsing.nodes;

import java.util.List;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;

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

    public static BooleanLiteralNode bool(boolean value) {
        return new BooleanLiteralNode(value);
    }
    
    public static UnitLiteralNode unit() {
        return new UnitLiteralNode();
    }
    
    public static ExpressionStatementNode expressionStatement(ExpressionNode expression) {
        return new ExpressionStatementNode(expression);
    }
    
    public static ImmutableVariableNode immutableVar(String name, ExpressionNode expression) {
        return new ImmutableVariableNode(name, none(ExpressionNode.class), expression);
    }
    
    public static ImmutableVariableNode immutableVar(String name, ExpressionNode type, ExpressionNode expression) {
        return new ImmutableVariableNode(name, some(type), expression);
    }
    
    public static ObjectDeclarationNode object(String name, List<StatementNode> statements) {
        return new ObjectDeclarationNode(name, statements);
    }

    public static PublicDeclarationNode publik(DeclarationNode declaration) {
        return new PublicDeclarationNode(declaration);
    }
    
    public static MemberAccessNode member(ExpressionNode expression, String memberName) {
        return new MemberAccessNode(expression, memberName);
    }

    public static TypeApplicationNode typeApply(ExpressionNode baseType, ExpressionNode... typeParameters) {
        return new TypeApplicationNode(baseType, asList(typeParameters));
    }
    
    public static IfThenElseStatementNode ifThenElse(ExpressionNode condition, List<StatementNode> ifTrue, List<StatementNode> ifFalse) {
        return new IfThenElseStatementNode(condition, ifTrue, ifFalse);
    }

    public static StatementNode returnStatement(ExpressionNode expression) {
        return new ReturnNode(expression);
    }
    
    public static BlockNode block(StatementNode... statements) {
        return new BlockNode(asList(statements));
    }
}
