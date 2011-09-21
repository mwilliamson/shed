package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;

import org.zwobble.shed.compiler.Option;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class NodeNavigator {
    public static Iterable<? extends SyntaxNode> children(SyntaxNode node) {
        if (isNodeWithNoChildren(node)) {
            return Collections.emptyList();
        }
        if (node instanceof AssignmentExpressionNode) {
            AssignmentExpressionNode assignment = (AssignmentExpressionNode) node;
            return asList(assignment.getTarget(), assignment.getValue());
        }
        if (node instanceof BlockNode) {
            return ((BlockNode) node).getStatements();
        }
        if (node instanceof CallNode) {
            CallNode call = (CallNode) node;
            return concat(singletonList((call.getFunction())), call.getArguments());
        }
        if (node instanceof ExpressionStatementNode) {
            return singletonList(((ExpressionStatementNode) node).getExpression());
        }
        if (node instanceof FormalArgumentNode) {
            return singletonList(((FormalArgumentNode) node).getType());
        }
        if (node instanceof IfThenElseStatementNode) {
            IfThenElseStatementNode ifStatement = (IfThenElseStatementNode) node;
            return asList(ifStatement.getCondition(), ifStatement.getIfTrue(), ifStatement.getIfFalse());
        }
        if (node instanceof VariableDeclarationNode) {
            VariableDeclarationNode variableDeclaration = (ImmutableVariableNode) node;
            Option<? extends ExpressionNode> type = variableDeclaration.getTypeReference();
            if (type.hasValue()) {
                return asList(variableDeclaration.getValue(), type.get());
            } else {
                return asList(variableDeclaration.getValue());   
            }
        }
        if (node instanceof LongLambdaExpressionNode) {
            LongLambdaExpressionNode lambda = (LongLambdaExpressionNode) node;
            return concat(lambda.getFormalArguments(), asList(lambda.getReturnType(), lambda.getBody()));
        }
        if (node instanceof MemberAccessNode) {
            return singletonList(((MemberAccessNode) node).getExpression());
        }
        if (node instanceof ObjectDeclarationNode) {
            return singletonList(((ObjectDeclarationNode) node).getStatements());
        }
        if (node instanceof PublicDeclarationNode) {
            return singletonList(((PublicDeclarationNode) node).getDeclaration());
        }
        if (node instanceof ReturnNode) {
            return singletonList(((ReturnNode) node).getExpression());
        }
        if (node instanceof ShortLambdaExpressionNode) {
            ShortLambdaExpressionNode lambda = (ShortLambdaExpressionNode) node;
            Option<? extends ExpressionNode> returnType = lambda.getReturnType();
            Iterable<? extends ExpressionNode> typeNodes = returnType.hasValue() 
                ? singletonList(returnType.get()) 
                : Collections.<ExpressionNode>emptyList();
            return concat(lambda.getFormalArguments(), typeNodes, singletonList(lambda.getBody()));
        }
        if (node instanceof SourceNode) {
            SourceNode source = (SourceNode) node;
            return concat(singletonList(source.getPackageDeclaration()), source.getImports(), source.getStatements());
        }
        if (node instanceof TypeApplicationNode) {
            TypeApplicationNode typeApplication = (TypeApplicationNode) node;
            return concat(singletonList(typeApplication.getBaseValue()), typeApplication.getParameters());
        }
        if (node instanceof WhileStatementNode) {
            WhileStatementNode whileStatement = (WhileStatementNode) node;
            return asList(whileStatement.getCondition(), whileStatement.getBody());
        }
        throw new RuntimeException("Cannot find children of: " + node);
    }

    private static boolean isNodeWithNoChildren(SyntaxNode node) {
        return 
            node instanceof LiteralNode || 
            node instanceof ImportNode || 
            node instanceof PackageDeclarationNode || 
            node instanceof VariableIdentifierNode;
    }
}
