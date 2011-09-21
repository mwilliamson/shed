package org.zwobble.shed.compiler.parsing.nodes;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;

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
    
    public static ObjectDeclarationNode object(String name, BlockNode body) {
        return new ObjectDeclarationNode(name, body);
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
    
    public static IfThenElseStatementNode ifThenElse(ExpressionNode condition, BlockNode ifTrue, BlockNode ifFalse) {
        return new IfThenElseStatementNode(condition, ifTrue, ifFalse);
    }

    public static StatementNode returnStatement(ExpressionNode expression) {
        return new ReturnNode(expression);
    }
    
    public static BlockNode block(StatementNode... statements) {
        return new BlockNode(asList(statements));
    }

    public static WhileStatementNode whileLoop(ExpressionNode condition, BlockNode body) {
        return new WhileStatementNode(condition, body);
    }

    public static AssignmentExpressionNode assign(ExpressionNode target, ExpressionNode value) {
        return new AssignmentExpressionNode(target, value);
    }

    public static FormalArgumentNode formalArgument(String identifier, ExpressionNode type) {
        return new FormalArgumentNode(identifier, type);
    }

    public static ImportNode importNode(String... names) {
        return new ImportNode(asList(names));
    }

    public static LongLambdaExpressionNode longLambda(List<FormalArgumentNode> arguments, ExpressionNode type, BlockNode body) {
        return new LongLambdaExpressionNode(arguments, type, body);
    }
}
