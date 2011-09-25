package org.zwobble.shed.compiler.ordering;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolver;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolverResult;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.SimpleNodeLocations;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;

public class StatementOrdererTest {
    private static final List<FormalArgumentNode> NO_ARGS = Collections.<FormalArgumentNode>emptyList();
    private final ReferencesBuilder references = new ReferencesBuilder();

    @Test public void
    ordinaryStatementsHaveOrderUnaltered() {
        ExpressionStatementNode firstStatement = Nodes.expressionStatement(Nodes.number("42"));
        ExpressionStatementNode secondStatement = Nodes.expressionStatement(Nodes.bool(true));
        assertThat(reorder(Nodes.block(firstStatement, secondStatement)), isOrdering(firstStatement, secondStatement));
    }

    @Test public void
    functionDeclarationIsPulledUpIfCallPrecedesDefinition() {
        ExpressionStatementNode functionCall = Nodes.expressionStatement(Nodes.call(Nodes.id("go")));
        FunctionDeclarationNode functionDeclaration = Nodes.func("go", NO_ARGS, Nodes.id("Number"), Nodes.block());
        assertThat(reorder(Nodes.block(functionCall, functionDeclaration)), isOrdering(functionDeclaration, functionCall));
    }
    
    private Matcher<TypeResult<Iterable<StatementNode>>> isOrdering(final StatementNode... statements) {
        return allOf(isSuccess(), new TypeSafeDiagnosingMatcher<TypeResult<Iterable<StatementNode>>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Ordering: " + asList(statements));
            }

            @Override
            protected boolean matchesSafely(TypeResult<Iterable<StatementNode>> item, Description mismatchDescription) {
                Matcher<Iterable<? extends StatementNode>> orderMatcher = contains(statements);
                if (orderMatcher.matches(item.get())) {
                    return true;
                } else {
                    orderMatcher.describeMismatch(item.get(), mismatchDescription);
                    return false;
                }
            }
        });
    }
    
    private TypeResult<Iterable<StatementNode>> reorder(BlockNode block) {
        resolveReferences(block);
        return new StatementOrderer().reorder(block, references.build());
    }

    private References resolveReferences(BlockNode block) {
        ReferenceResolver resolver = new ReferenceResolver();
        SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
        Map<String, GlobalDeclarationNode> globalDeclarations = ImmutableMap.of("Number", new GlobalDeclarationNode("Number"));
        ReferenceResolverResult result = resolver.resolveReferences(block, nodeLocations, globalDeclarations);
        if (!result.isSuccess()) {
            throw new RuntimeException("Unsuccessful reference resolution: " + result.getErrors());
        }
        return result.getReferences();
    }
}
