package org.zwobble.shed.compiler.ordering;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.zwobble.shed.compiler.ordering.errors.CircularDependencyError;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
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
import static org.zwobble.shed.compiler.ordering.OrderTesting.isOrdering;

public class DependencyGraphLineariserTest {
    private static final List<FormalArgumentNode> NO_ARGS = Collections.<FormalArgumentNode>emptyList();
    private final DependencyGraphLineariser lineariser = new DependencyGraphLineariser();
    
    @Test public void
    orderIsPreservedIfThereAreNoDependencies() {
        StatementNode first = Nodes.expressionStatement(Nodes.id("go"));
        StatementNode second = Nodes.returnStatement(Nodes.number("42"));
        DependencyGraph graph = new DependencyGraph(asList(first, second));
        
        assertThat(linearise(graph), isOrdering(first, second));
    }
    
    @Test public void
    statementsAreReorderedAccordingToLexicalDependencies() {
        StatementNode first = Nodes.expressionStatement(Nodes.id("go"));
        StatementNode second = Nodes.returnStatement(Nodes.number("42"));
        DependencyGraph graph = new DependencyGraph(asList(second, first));
        graph.addLexicalDependency(first, second);
        
        assertThat(linearise(graph), isOrdering(first, second));
    }
    
    @Test public void
    statementsAreReorderedAccordingToStrictLogicalDependencyDependencies() {
        DeclarationNode declaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), Nodes.block());
        StatementNode reference = Nodes.expressionStatement(Nodes.call(Nodes.id("go")));
        DependencyGraph graph = new DependencyGraph(asList(reference, declaration));
        graph.addStrictLogicalDependency(declaration, reference);
        
        assertThat(linearise(graph), isOrdering(declaration, reference));
    }
    
    @Test public void
    errorIfCycleInLogicalDependencies() {
        VariableDeclarationNode variableDeclaration = Nodes.immutableVar("x", Nodes.call(Nodes.id("go")));
        BlockNode body = Nodes.block(Nodes.returnStatement(Nodes.id("x")));
        FunctionDeclarationNode functionDeclaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), body);
        DependencyGraph graph = new DependencyGraph(asList(variableDeclaration, functionDeclaration));
        graph.addStrictLogicalDependency(variableDeclaration, functionDeclaration);
        graph.addStrictLogicalDependency(functionDeclaration, variableDeclaration);
        assertThat(
            linearise(graph),
            isFailureWithErrors(new CircularDependencyError(asList(variableDeclaration, functionDeclaration)))
        );
    }
    
    private TypeResult<Iterable<StatementNode>> linearise(DependencyGraph graph) {
        return lineariser.linearise(graph, new SimpleNodeLocations());
    }
}
