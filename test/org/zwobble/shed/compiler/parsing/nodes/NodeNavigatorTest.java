package org.zwobble.shed.compiler.parsing.nodes;

import java.util.Collection;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

public class NodeNavigatorTest {
    @Test public void
    literalsHaveNoChildren() {
        assertThat(children(Nodes.bool(true)), isNodes());
    }
    
    @Test public void
    assignmentExpressionChildrenAreBothSidesOfAssignment() {
        ExpressionNode target = Nodes.id("x");
        ExpressionNode value = Nodes.bool(true);
        assertThat(children(Nodes.assign(target, value)), isNodes(target, value));
    }
    
    @Test public void
    blockNodeChildrenAreStatements() {
        StatementNode first = Nodes.expressionStatement(Nodes.id("x"));
        StatementNode second = Nodes.immutableVar("age", Nodes.number("42"));
        assertThat(children(Nodes.block(first, second)), isNodes(first, second));
    }
    
    @Test public void
    callNodeChildrenAreFunctionAndArguments() {
        ExpressionNode function = Nodes.id("go");
        ExpressionNode argument = Nodes.number("42");
        assertThat(children(Nodes.call(function, argument)), isNodes(function, argument));
    }
    
    @Test public void
    expressionStatementChildrenIsExpression() {
        ExpressionNode expression = Nodes.number("42");
        assertThat(children(Nodes.expressionStatement(expression)), isNodes(expression));
    }
    
    @Test public void
    formalArgumentChildrenIsTypeExpression() {
        ExpressionNode expression = Nodes.number("42");
        assertThat(children(Nodes.expressionStatement(expression)), isNodes(expression));
    }
    
    private Matcher<Iterable<? extends SyntaxNode>> isNodes(SyntaxNode... nodes) {
        Collection<Matcher<? super SyntaxNode>> matchers = newArrayList(transform(asList(nodes), toSameMatcher()));
        return new IsIterableContainingInAnyOrder<SyntaxNode>(matchers);
    }
    
    private Function<SyntaxNode, Matcher<? super SyntaxNode>> toSameMatcher() {
        return new Function<SyntaxNode, Matcher<? super SyntaxNode>>() {
            @Override
            public Matcher<SyntaxNode> apply(SyntaxNode input) {
                return Matchers.sameInstance(input);
            }
        };
    }

    private Iterable<? extends SyntaxNode> children(SyntaxNode node) {
        return NodeNavigator.children(node);
    }
}
