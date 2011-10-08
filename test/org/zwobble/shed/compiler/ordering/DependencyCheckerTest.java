package org.zwobble.shed.compiler.ordering;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.zwobble.shed.compiler.ordering.errors.UndeclaredDependenciesError;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.typechecker.SimpleNodeLocations;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;

public class DependencyCheckerTest {
    private static final List<FormalArgumentNode> NO_ARGS = Collections.<FormalArgumentNode>emptyList();
    private final DependencyChecker checker = new DependencyChecker();
    
    @Test public void
    validIfThereAreNoDependencies() {
        StatementNode first = Nodes.expressionStatement(Nodes.id("go"));
        StatementNode second = Nodes.returnStatement(Nodes.number("42"));
        DependencyGraph graph = new DependencyGraph();
        
        assertThat(check(asList(first, second), graph), isSuccess());
    }
    
    @Test public void
    functionDeclarationIsAllowedAfterUsageIfItHasNoDependencies() {
        DeclarationNode declaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), Nodes.block());
        StatementNode reference = Nodes.expressionStatement(Nodes.call(Nodes.id("go")));
        DependencyGraph graph = new DependencyGraph();
        graph.addStrictLogicalDependency(declaration, reference);
        
        assertThat(check(asList(reference, declaration), graph), isSuccess());
    }
    
    @Test public void
    errorIfCycleInLogicalDependencies() {
        VariableDeclarationNode variableDeclaration = Nodes.immutableVar("x", Nodes.call(Nodes.id("go")));
        BlockNode body = Nodes.block(Nodes.returnStatement(Nodes.id("x")));
        FunctionDeclarationNode functionDeclaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), body);
        
        DependencyGraph graph = new DependencyGraph();
        graph.addStrictLogicalDependency(variableDeclaration, functionDeclaration);
        graph.addStrictLogicalDependency(functionDeclaration, variableDeclaration);
        
        assertThat(
            check(asList(variableDeclaration, functionDeclaration), graph),
            isFailureWithErrors(new UndeclaredDependenciesError(asList("go", "x")))
        );
    }
    
    @Test public void
    listOfVariablesBeingDeclaredIsClearedOfOldVariables() {
        ExpressionStatementNode first = Nodes.expressionStatement(Nodes.id("y"));
        VariableDeclarationNode variableDeclaration = Nodes.immutableVar("x", Nodes.call(Nodes.id("go")));
        BlockNode body = Nodes.block(Nodes.returnStatement(Nodes.id("x")));
        FunctionDeclarationNode functionDeclaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), body);
        
        DependencyGraph graph = new DependencyGraph();
        graph.addStrictLogicalDependency(variableDeclaration, functionDeclaration);
        graph.addStrictLogicalDependency(functionDeclaration, variableDeclaration);
        
        assertThat(
            check(asList(first, variableDeclaration, functionDeclaration), graph),
            isFailureWithErrors(new UndeclaredDependenciesError(asList("go", "x")))
        );
    }

    @Test public void
    errorIfVariableIsUsedBeforeItIsDeclaredAndDeclarationCannotBeMoved() {
        StatementNode variableReference = Nodes.expressionStatement(Nodes.id("x"));
        VariableDeclarationNode variableDeclaration = Nodes.immutableVar("x", Nodes.number("4"));

        DependencyGraph graph = new DependencyGraph();
        graph.addStrictLogicalDependency(variableDeclaration, variableReference);
        
        assertThat(
            check(asList(variableReference, variableDeclaration), graph),
            isFailureWithErrors(new UndeclaredDependenciesError(asList("x")))
        );
    }

    @Test public void
    errorIfFunctionCannotBeUsedSinceDependencyIsNotDeclaredYet() {
        StatementNode call = Nodes.expressionStatement(Nodes.id("go()"));
        VariableDeclarationNode variableDeclaration = Nodes.immutableVar("x", Nodes.number("4"));
        BlockNode functionBody = Nodes.block(Nodes.returnStatement(Nodes.id("x")));
        FunctionDeclarationNode functionDeclaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), functionBody);

        DependencyGraph graph = new DependencyGraph();
        graph.addStrictLogicalDependency(functionDeclaration, call);
        graph.addStrictLogicalDependency(variableDeclaration, functionDeclaration);
        
        assertThat(
            check(asList(call, variableDeclaration), graph),
            isFailureWithErrors(new UndeclaredDependenciesError(asList("go", "x")))
        );
    }
    
    private TypeResult<Void> check(List<? extends StatementNode> statements, DependencyGraph graph) {
        return checker.check(statements, graph, new SimpleNodeLocations());
    }
}
