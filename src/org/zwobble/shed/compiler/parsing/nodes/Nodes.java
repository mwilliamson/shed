package org.zwobble.shed.compiler.parsing.nodes;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;

import java.util.Collections;
import java.util.List;

import org.zwobble.shed.compiler.Option;

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
    
    public static VariableDeclarationNode immutableVar(String name, ExpressionNode expression) {
        return VariableDeclarationNode.immutable(name, none(ExpressionNode.class), expression);
    }
    
    public static VariableDeclarationNode immutableVar(String name, ExpressionNode type, ExpressionNode expression) {
        return VariableDeclarationNode.immutable(name, some(type), expression);
    }
    
    public static VariableDeclarationNode mutableVar(String name, ExpressionNode expression) {
        return VariableDeclarationNode.mutable(name, none(ExpressionNode.class), expression);
    }
    
    public static VariableDeclarationNode mutableVar(String name, ExpressionNode type, ExpressionNode expression) {
        return VariableDeclarationNode.mutable(name, some(type), expression);
    }
    
    public static ObjectDeclarationNode object(String name, BlockNode body) {
        return new ObjectDeclarationNode(name, body);
    }

    public static SyntaxNode clazz(String identifier, List<FormalArgumentNode> formalArguments, BlockNode body) {
        return new ClassDeclarationNode(identifier, formalArguments, body);
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

    public static List<FormalArgumentNode> noFormalArguments() {
        return Collections.emptyList();
    }

    public static ImportNode importNode(String... names) {
        return new ImportNode(asList(names));
    }

    public static LongLambdaExpressionNode longLambda(List<FormalArgumentNode> arguments, ExpressionNode type, BlockNode body) {
        return new LongLambdaExpressionNode(arguments, type, body);
    }

    public static PackageDeclarationNode packageDeclaration(String... names) {
        return new PackageDeclarationNode(asList(names));
    }

    public static ShortLambdaExpressionNode shortLambda(List<FormalArgumentNode> arguments, ExpressionNode returnType, ExpressionNode body) {
        return new ShortLambdaExpressionNode(arguments, some(returnType), body);
    }

    public static ShortLambdaExpressionNode shortLambda(List<FormalArgumentNode> arguments, ExpressionNode body) {
        return new ShortLambdaExpressionNode(arguments, Option.<ExpressionNode>none(), body);
    }

    public static SourceNode source(PackageDeclarationNode packageDeclaration, List<ImportNode> imports, List<StatementNode> statements) {
        return new SourceNode(packageDeclaration, imports, statements);
    }

    public static FunctionDeclarationNode func(String identifier, List<FormalArgumentNode> formalArguments, ExpressionNode returnType, BlockNode body) {
        return new FunctionDeclarationNode(identifier, formalArguments, returnType, body);
    }
}
