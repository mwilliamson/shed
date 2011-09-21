package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;

import org.zwobble.shed.compiler.Option;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class NodeNavigator {
    public static Iterable<? extends SyntaxNode> children(SyntaxNode node) {
        if (node instanceof LiteralNode || node instanceof ImportNode) {
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
        throw new RuntimeException("Cannot find children of: " + node);
    }
}
