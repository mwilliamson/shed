package org.zwobble.shed.compiler.referenceresolution;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.CompilerTesting;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.SimpleNodeLocations;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ReferenceResolverTest {
    private static final References EMPTY_REFERENCES = new References(ImmutableMap.<Identity<VariableIdentifierNode>, Identity<DeclarationNode>>of());
    private static final ReferenceResolverResult EMPTY_RESULT = ReferenceResolverResult.build(EMPTY_REFERENCES, Collections.<CompilerError>emptyList());
    private final ReferenceResolver resolver = new ReferenceResolver();
    private final NodeLocations nodeLocations = new SimpleNodeLocations();
    
    @Test public void
    booleanLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.bool(true)), is(EMPTY_RESULT));
    }
    
    @Test public void
    stringLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.string("blah")), is(EMPTY_RESULT));
    }
    
    @Test public void
    numberLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.number("42")), is(EMPTY_RESULT));
    }
    
    @Test public void
    canReferToImmutableReferencesInSameBlock() {
        VariableIdentifierNode reference = Nodes.id("x");
        DeclarationNode declaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(declaration, Nodes.expressionStatement(reference));
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }

    @Test public void
    referringToVariablesNotInScopeAddsError() {
        VariableIdentifierNode reference = Nodes.id("height");
        assertThat(resolveReferences(reference), isFailureWithErrors("No variable \"height\" in scope"));
    }

    private ReferenceResolverResult resolveReferences(SyntaxNode node) {
        return resolver.resolveReferences(node, nodeLocations);
    }
    
    private Matcher<ReferenceResolverResult> hasReference(final VariableIdentifierNode reference, final DeclarationNode declaration) {
        return new TypeSafeDiagnosingMatcher<ReferenceResolverResult>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(reference.getIdentifier() + " should refer to " + declaration);
            }

            @Override
            protected boolean matchesSafely(ReferenceResolverResult result, Description mismatchDescription) {
                if (result.isSuccess()) {
                    References references = result.getReferences();
                    DeclarationNode actualReferent = references.findReferent(reference);
                    if (actualReferent == declaration) {
                        return true;
                    } else {
                        mismatchDescription.appendText("referent was " + result.getReferences());
                        return false;
                    }
                } else {
                    mismatchDescription.appendText("result was failure");
                    return false;
                }
            }
        };
    }
    
    private Matcher<ReferenceResolverResult> isFailureWithErrors(final String... errorsArray) {
        final List<String> errors = Arrays.asList(errorsArray);
        return new TypeSafeDiagnosingMatcher<ReferenceResolverResult>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("failure with errors: " + errors);
            }

            @Override
            protected boolean matchesSafely(ReferenceResolverResult item, Description mismatchDescription) {
                List<String> actualErrors = CompilerTesting.errorStrings(item);
                if (actualErrors.equals(errors)) {
                    return true;
                } else {
                    mismatchDescription.appendText("had errors: " + actualErrors);
                    return false;
                }
            }
        };
    }
}
