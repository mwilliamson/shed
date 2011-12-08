package org.zwobble.shed.compiler.dependencies;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.zwobble.shed.compiler.dependencies.errors.UndeclaredDependenciesError;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolver;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolverResult;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.BuiltIns;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;

public class DependencyCheckerTest {
    private static final List<ImportNode> NO_IMPORTS = Collections.emptyList();
    private final DependencyChecker checker = new DependencyChecker();
    
    @Test public void
    bodyOfSourceNodeHasDependenciesChecked() {
        List<StatementNode> body = asList(
            Nodes.expressionStatement(Nodes.id("x")),
            Nodes.immutableVar("x", Nodes.number("42"))
        );
        assertThat(
            check(Nodes.source(Nodes.packageDeclaration("shed", "example"), NO_IMPORTS, body)),
            isFailureWithErrors(new UndeclaredDependenciesError(asList("x")))
        );
    }
    
    @Test public void
    blockHasDependenciesChecked() {
        BlockNode block = Nodes.block(
            Nodes.expressionStatement(Nodes.id("x")),
            Nodes.immutableVar("x", Nodes.number("42"))
        );
        assertThat(
            check(block),
            isFailureWithErrors(new UndeclaredDependenciesError(asList("x")))
        );
    }
    
    @Test public void
    blockWithinSourceNodeHasDependenciesChecked() {
        BlockNode objectBody = Nodes.block(
            Nodes.expressionStatement(Nodes.id("x")),
            Nodes.immutableVar("x", Nodes.number("42"))
        );
        SourceNode source = Nodes.source(
            Nodes.packageDeclaration("shed", "example"),
            NO_IMPORTS,
            Arrays.<StatementNode>asList(Nodes.object("bob", objectBody))
        );
        assertThat(
            check(source),
            isFailureWithErrors(new UndeclaredDependenciesError(asList("x")))
        );
    }
    
    @Test public void
    canReferToVariablesOutsideOfScope() {
        BlockNode objectBody = Nodes.block(Nodes.expressionStatement(Nodes.id("x")));
        SourceNode source = Nodes.source(
            Nodes.packageDeclaration("shed", "example"),
            NO_IMPORTS,
            Arrays.<StatementNode>asList(
                Nodes.immutableVar("x", Nodes.number("42")),
                Nodes.object("bob", objectBody)
            )
        );
        assertThat(
            check(source),
            isSuccess()
        );
    }

    private TypeResult<?> check(SyntaxNode node) {
        return checker.check(node, resolveReferences(node));
    }

    private References resolveReferences(SyntaxNode node) {
        ReferenceResolver resolver = new ReferenceResolver();
        ReferenceResolverResult result = resolver.resolveReferences(node, new BuiltIns());
        if (!result.isSuccess()) {
            throw new RuntimeException("Unsuccessful reference resolution: " + result.getErrors());
        }
        return result.getReferences();
    }
}
