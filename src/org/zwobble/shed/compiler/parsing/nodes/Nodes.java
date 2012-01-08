package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;
import java.util.List;

import org.zwobble.shed.compiler.Option;

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
        return new ObjectDeclarationNode(name, Collections.<ExpressionNode>emptyList(), body);
    }
    
    public static ObjectDeclarationNode object(String name, List<ExpressionNode> superTypes, BlockNode body) {
        return new ObjectDeclarationNode(name, superTypes, body);
    }

    public static ClassDeclarationNode clazz(String identifier, List<FormalArgumentNode> formalArguments, BlockNode body) {
        return new ClassDeclarationNode(identifier, formalArguments, Collections.<ExpressionNode>emptyList(), body);
    }

    public static ClassDeclarationNode clazz(String identifier, List<FormalArgumentNode> formalArguments, List<ExpressionNode> superTypes, BlockNode body) {
        return new ClassDeclarationNode(identifier, formalArguments, superTypes, body);
    }

    public static InterfaceDeclarationNode interfaceDeclaration(String identifier, InterfaceBodyNode body) {
        return new InterfaceDeclarationNode(identifier, body);
    }

    public static InterfaceBodyNode interfaceBody(FunctionSignatureDeclarationNode... functionDeclarations) {
        return new InterfaceBodyNode(asList(functionDeclarations));
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

    public static IfThenElseStatementNode ifThen(ExpressionNode condition, BlockNode ifTrue) {
        return new IfThenElseStatementNode(condition, ifTrue, block());
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

    public static FormalTypeParameterNode formalTypeParameter(String identifier) {
        return new FormalTypeParameterNode(identifier);
    }

    public static FormalTypeParametersNode formalTypeParameters(List<FormalTypeParameterNode> formalTypeParameters) {
        return new FormalTypeParametersNode(formalTypeParameters);
    }

    public static FormalTypeParametersNode formalTypeParameters(FormalTypeParameterNode... formalTypeParameters) {
        return new FormalTypeParametersNode(asList(formalTypeParameters));
    }

    public static FormalArgumentNode formalArgument(String identifier, ExpressionNode type) {
        return new FormalArgumentNode(identifier, type);
    }

    public static List<FormalArgumentNode> formalArguments(FormalArgumentNode... formalArguments) {
        return asList(formalArguments);
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
    
    public static EntireSourceNode sources(SourceNode... sources) {
        return new EntireSourceNode(asList(sources));
    }

    public static FunctionDeclarationNode func(String identifier, List<FormalArgumentNode> formalArguments, ExpressionNode returnType, BlockNode body) {
        return new FunctionDeclarationNode(identifier, Option.<FormalTypeParametersNode>none(), formalArguments, returnType, body);
    }

    public static FunctionDeclarationNode func(String identifier, FormalTypeParametersNode formalTypeParameters, List<FormalArgumentNode> formalArguments, ExpressionNode returnType, BlockNode body) {
        return new FunctionDeclarationNode(identifier, some(formalTypeParameters), formalArguments, returnType, body);
    }

    public static FunctionSignatureDeclarationNode funcSignature(String identifier, List<FormalArgumentNode> formalArguments, ExpressionNode returnType) {
        return new FunctionSignatureDeclarationNode(identifier, formalArguments, returnType);
    }

    public static List<ExpressionNode> expressions(ExpressionNode... expressions) {
        return asList(expressions);
    }
}
