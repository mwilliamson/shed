package org.zwobble.shed.compiler.ordering;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.zwobble.shed.compiler.ordering.errors.UnpullableDeclarationError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolver;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolverResult;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.referenceresolution.VariableNotDeclaredYetError;
import org.zwobble.shed.compiler.typechecker.SimpleNodeLocations;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.ordering.OrderTesting.isOrdering;

public class StatementOrdererTest {
    private static final List<FormalArgumentNode> NO_ARGS = Collections.<FormalArgumentNode>emptyList();
    private final NodeLocations nodeLocations = new SimpleNodeLocations();

    @Test public void
    ordinaryStatementsHaveOrderUnaltered() {
        ExpressionStatementNode firstStatement = Nodes.expressionStatement(Nodes.number("42"));
        ExpressionStatementNode secondStatement = Nodes.expressionStatement(Nodes.bool(true));
        assertThat(reorder(Nodes.block(firstStatement, secondStatement)), isOrdering(firstStatement, secondStatement));
    }

    @Test public void
    functionDeclarationIsPulledUpIfCallPrecedesDefinition() {
        ExpressionStatementNode functionCall = Nodes.expressionStatement(Nodes.call(Nodes.id("go")));
        ExpressionStatementNode unrelatedStatement = Nodes.expressionStatement(Nodes.number("42"));
        FunctionDeclarationNode functionDeclaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), Nodes.block());
        assertThat(reorder(
            Nodes.block(functionCall, unrelatedStatement, functionDeclaration)),
            isOrdering(functionDeclaration, functionCall, unrelatedStatement)
        );
    }

    @Test public void
    functionDeclarationIsPushedDownIfItReliesOnVariableDeclaredLater() {
        VariableDeclarationNode variableDeclaration = Nodes.immutableVar("x", Nodes.number("4"));
        BlockNode body = Nodes.block(Nodes.returnStatement(Nodes.id("x")));
        FunctionDeclarationNode functionDeclaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), body);
        assertThat(reorder(Nodes.block(functionDeclaration, variableDeclaration)), isOrdering(variableDeclaration, functionDeclaration));
    }

    @Test public void
    errorIfFunctionAndVariableDependOnEachOther() {
        VariableDeclarationNode variableDeclaration = Nodes.immutableVar("x", Nodes.call(Nodes.id("go")));
        BlockNode body = Nodes.block(Nodes.returnStatement(Nodes.id("x")));
        FunctionDeclarationNode functionDeclaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), body);
        assertThat(
            reorder(Nodes.block(functionDeclaration, variableDeclaration)),
            isFailureWithErrors(new UnpullableDeclarationError(functionDeclaration, variableDeclaration, variableDeclaration))
        );
    }

    @Test public void
    errorIfUsingVariableNotYetDeclared() {
        VariableIdentifierNode variableReference = Nodes.id("x");
        VariableDeclarationNode variableDeclaration = Nodes.immutableVar("x", Nodes.number("4"));
        
        assertThat(
            reorder(Nodes.block(Nodes.expressionStatement(variableReference), variableDeclaration)),
            isFailureWithErrors(new VariableNotDeclaredYetError("x"))
        );
    }

    @Test public void
    functionDeclarationsCanCallEachOther() {
        FunctionDeclarationNode firstFunctionDeclaration = Nodes.func(
            "first", NO_ARGS, Nodes.id("Number"), Nodes.block(Nodes.returnStatement(Nodes.call(Nodes.id("second"))))
        );
        FunctionDeclarationNode secondFunctionDeclaration = Nodes.func(
            "second", NO_ARGS, Nodes.id("Number"), Nodes.block(Nodes.returnStatement(Nodes.call(Nodes.id("first"))))
        );
        assertThat(
            reorder(Nodes.block(firstFunctionDeclaration, secondFunctionDeclaration)),
            isOrdering(firstFunctionDeclaration, secondFunctionDeclaration)
        );
    }

    @Ignore
    @Test public void
    errorIfMutuallyRecursiveFunctionsCannotBeDeclaredTogether() {
        FunctionDeclarationNode firstFunctionDeclaration = Nodes.func(
            "first", NO_ARGS, Nodes.id("Number"), Nodes.block(Nodes.returnStatement(Nodes.call(Nodes.id("second"))))
        );
        VariableDeclarationNode variable = Nodes.immutableVar("x", Nodes.call(Nodes.id("first")));
        FunctionDeclarationNode secondFunctionDeclaration = Nodes.func(
            "second", NO_ARGS, Nodes.id("Number"), Nodes.block(Nodes.expressionStatement(Nodes.id("x")), Nodes.returnStatement(Nodes.call(Nodes.id("first"))))
        );
        assertThat(
            reorder(Nodes.block(firstFunctionDeclaration, variable, secondFunctionDeclaration)),
            isFailureWithErrors(new UnpullableDeclarationError(secondFunctionDeclaration, variable, firstFunctionDeclaration))
        );
    }
    
    private TypeResult<Iterable<StatementNode>> reorder(BlockNode block) {
        return new StatementOrderer().reorder(block, nodeLocations, resolveReferences(block));
    }

    private References resolveReferences(BlockNode block) {
        ReferenceResolver resolver = new ReferenceResolver();
        Map<String, GlobalDeclarationNode> globalDeclarations = ImmutableMap.of("Number", new GlobalDeclarationNode("Number"));
        ReferenceResolverResult result = resolver.resolveReferences(block, nodeLocations, globalDeclarations);
        if (!result.isSuccess()) {
            throw new RuntimeException("Unsuccessful reference resolution: " + result.getErrors());
        }
        return result.getReferences();
    }
}
