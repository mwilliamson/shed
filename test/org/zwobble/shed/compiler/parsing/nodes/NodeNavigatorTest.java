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
        ExpressionNode expression = Nodes.id("Number");
        assertThat(children(Nodes.formalArgument("age", expression)), isNodes(expression));
    }
    
    @Test public void
    ifThenElseChildrenAreConditionAndBothBranches() {
        ExpressionNode condition = Nodes.bool(true);
        BlockNode ifTrue = Nodes.block(Nodes.returnStatement(Nodes.number("59")));
        BlockNode ifFalse = Nodes.block(Nodes.returnStatement(Nodes.number("60")));
        assertThat(children(Nodes.ifThenElse(condition, ifTrue, ifFalse)), isNodes(condition, ifTrue, ifFalse));
    }
    
    @Test public void
    variableDeclarationChildrenIsValue() {
        ExpressionNode value = Nodes.bool(true);
        assertThat(children(Nodes.immutableVar("isPlaying", value)), isNodes(value));
    }
    
    @Test public void
    variableDeclarationChildrenIncludesTypeSpecifierIfPresent() {
        ExpressionNode value = Nodes.bool(true);
        VariableIdentifierNode typeSpecifier = Nodes.id("Boolean");
        assertThat(children(Nodes.immutableVar("isPlaying", typeSpecifier, value)), isNodes(typeSpecifier, value));
    }
    
    @Test public void
    importNodeHasNoChildren() {
        assertThat(children(Nodes.importNode("shed", "example")), isNodes());
    }
    
    @Test public void
    longLambdaChildrenAreArgumentsAndTypeSpecifierAndBody() {
        FormalArgumentNode formalArgument = Nodes.formalArgument("x", Nodes.id("Number"));
        VariableIdentifierNode typeSpecifier = Nodes.id("Boolean");
        BlockNode body = Nodes.block();
        assertThat(children(Nodes.longLambda(asList(formalArgument), typeSpecifier, body)), isNodes(formalArgument, typeSpecifier, body));
    }
    
    @Test public void
    memberAccessChildrenIsBaseValue() {
        VariableIdentifierNode baseValue = Nodes.id("bob");
        assertThat(children(Nodes.member(baseValue, "name")), isNodes(baseValue));
    }
    
    @Test public void
    objectDeclarationChildrenIsBody() {
        BlockNode body = Nodes.block();
        assertThat(children(Nodes.object("bob", body)), isNodes(body));
    }
    
    @Test public void
    packageDeclarationHasNoChildren() {
        assertThat(children(Nodes.packageDeclaration("shed", "example")), isNodes());
    }
    
    @Test public void
    publicDeclarationChildrenIsUnderlyingDeclaration() {
        ObjectDeclarationNode declaration = Nodes.object("bob", Nodes.block());
        assertThat(children(Nodes.publik(declaration)), isNodes(declaration));
    }
    
    @Test public void
    returnStatementChildrenIsReturnedValue() {
        NumberLiteralNode returnedValue = Nodes.number("42");
        assertThat(children(Nodes.returnStatement(returnedValue)), isNodes(returnedValue));
    }
    
    @Test public void
    shortLambdaChildrenAreArgumentsAndBody() {
        FormalArgumentNode formalArgument = Nodes.formalArgument("x", Nodes.id("Number"));
        BooleanLiteralNode body = Nodes.bool(true);
        assertThat(children(Nodes.shortLambda(asList(formalArgument), body)), isNodes(formalArgument, body));
    }
    
    @Test public void
    shortLambdaChildrenAreArgumentsAndTypeSpecifierWhenPresentAndBody() {
        FormalArgumentNode formalArgument = Nodes.formalArgument("x", Nodes.id("Number"));
        VariableIdentifierNode typeSpecifier = Nodes.id("Boolean");
        BooleanLiteralNode body = Nodes.bool(true);
        assertThat(children(Nodes.shortLambda(asList(formalArgument), typeSpecifier, body)), isNodes(formalArgument, typeSpecifier, body));
    }
    
    @Test public void
    sourceNodeChildrenArePackageDeclarationAndImportsAndBody() {
        PackageDeclarationNode packageDeclaration = Nodes.packageDeclaration("shed", "example");
        ImportNode importNode = Nodes.importNode("shed", "util", "List");
        StatementNode body = Nodes.returnStatement(Nodes.number("42"));
        SourceNode source = Nodes.source(packageDeclaration, asList(importNode), asList(body));
        assertThat(children(source), isNodes(packageDeclaration, importNode, body));
    }
    
    @Test public void
    typeApplicationChildrenAreBaseTypeAndTypeParameters() {
        VariableIdentifierNode baseType = Nodes.id("List");
        VariableIdentifierNode parameter = Nodes.id("Number");
        assertThat(children(Nodes.typeApply(baseType, parameter)), isNodes(baseType, parameter));
    }
    
    @Test public void
    variableIdentifierHasNoChildren() {
        assertThat(children(Nodes.id("bob")), isNodes());
    }
    
    @Test public void
    whileChildrenAreConditionAndBody() {
        ExpressionNode condition = Nodes.bool(true);
        BlockNode body = Nodes.block(Nodes.returnStatement(Nodes.number("59")));
        assertThat(children(Nodes.whileLoop(condition, body)), isNodes(condition, body));
    }
    
    @Test public void
    functionDeclarationChildrenAreArgumentsAndReturnTypeAndBody() {
        FormalArgumentNode formalArgument = Nodes.formalArgument("delay", Nodes.id("TimeSpan"));
        ExpressionNode returnType = Nodes.id("Unit");
        BlockNode body = Nodes.block();
        assertThat(children(Nodes.func("go", asList(formalArgument), returnType, body)), isNodes(formalArgument, returnType, body));
    }
    
    @Test public void
    canFindAllDescendantsOfNode() {
        NumberLiteralNode value = Nodes.number("4");
        VariableIdentifierNode reference = Nodes.id("x");
        AssignmentExpressionNode assignment = Nodes.assign(reference, value);
        ExpressionStatementNode statement = Nodes.expressionStatement(assignment);
        BlockNode body = Nodes.block(statement);
        assertThat(descendents(body), isNodes(body, statement, assignment, reference, value));
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

    private Iterable<SyntaxNode> descendents(SyntaxNode node) {
        return NodeNavigator.descendents(node);
    }
}
