package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collections;

import com.google.common.collect.Iterables;

import static java.util.Collections.singletonList;

import static java.util.Arrays.asList;

public class NodeNavigator {
    public static Iterable<? extends SyntaxNode> children(SyntaxNode node) {
        if (node instanceof LiteralNode) {
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
            return Iterables.concat(singletonList((call.getFunction())), call.getArguments());
        }
        if (node instanceof ExpressionStatementNode) {
            return singletonList(((ExpressionStatementNode) node).getExpression());
        }
        throw new RuntimeException("Cannot find children of: " + node);
    }
}
