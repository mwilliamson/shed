package org.zwobble.shed.compiler.referenceresolution;

import java.util.Collections;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.CompilerTesting;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.CoreModule;
import org.zwobble.shed.compiler.typechecker.SimpleNodeLocations;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
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
        assertThat(resolveReferences(reference), isFailureWithErrors(new VariableNotInScopeError("height")));
    }

    @Test public void
    declaringVariableWithSameNameInSameScopeAddsError() {
        DeclarationNode firstDeclaration = Nodes.immutableVar("x", Nodes.number("42"));
        DeclarationNode secondDeclaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(firstDeclaration, secondDeclaration);
        assertThat(resolveReferences(source), isFailureWithErrors(new DuplicateIdentifierError("x")));
    }

    @Test public void
    shortLambdaExpressionAddsArgumentsToScope() {
        FormalArgumentNode firstArgument = new FormalArgumentNode("first", Nodes.id("String"));
        VariableIdentifierNode reference = Nodes.id("first");
        SyntaxNode source = new ShortLambdaExpressionNode(
            asList(firstArgument, new FormalArgumentNode("second", Nodes.id("Number"))),
            Option.<ExpressionNode>none(),
            reference
        );
        assertThat(resolveReferences(source), hasReference(reference, firstArgument));
    }

    @Test public void
    longLambdaExpressionAddsArgumentsToScope() {
        FormalArgumentNode firstArgument = new FormalArgumentNode("first", Nodes.id("String"));
        VariableIdentifierNode reference = Nodes.id("first");
        SyntaxNode source = new LongLambdaExpressionNode(
            asList(firstArgument, new FormalArgumentNode("second", Nodes.id("Number"))),
            Nodes.id("String"),
            Nodes.block(Nodes.returnStatement(reference))
        );
        assertThat(resolveReferences(source), hasReference(reference, firstArgument));
    }

    @Test public void
    canReferToVariableInParentScope() {
        VariableIdentifierNode reference = Nodes.id("theNight");
        ImmutableVariableNode declaration = Nodes.immutableVar("theNight", Nodes.string("feelsMySoul"));
        SyntaxNode source = Nodes.block(
            declaration,
            Nodes.expressionStatement(new ShortLambdaExpressionNode(
                asList(new FormalArgumentNode("first", Nodes.id("String")), new FormalArgumentNode("second", Nodes.id("Number"))),
                Option.<ExpressionNode>none(),
                reference
            ))
        );
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }

    @Test public void
    canOverrideDeclarationOfVariableInSubScope() {
        SyntaxNode source = Nodes.block(
            Nodes.immutableVar("wind", Nodes.string("decide")),
            Nodes.expressionStatement(new LongLambdaExpressionNode(
                Collections.<FormalArgumentNode>emptyList(),
                Nodes.id("String"),
                Nodes.block(
                    Nodes.immutableVar("wind", Nodes.bool(false))
                )
            ))
        );
        assertThat(resolveReferences(source), isSuccess());
    }

    @Test public void
    cannotReferToVariableInCurrentScopeAndParentScopeNotYetDefinedInCurrentScope() {
        SyntaxNode source = Nodes.block(
            Nodes.immutableVar("wind", Nodes.string("decide")),
            Nodes.expressionStatement(new LongLambdaExpressionNode(
                Collections.<FormalArgumentNode>emptyList(),
                Nodes.id("String"),
                Nodes.block(
                    Nodes.expressionStatement(Nodes.id("wind")),
                    Nodes.immutableVar("wind", Nodes.bool(false))
                )
            ))
        );
        assertThat(resolveReferences(source), isFailureWithErrors(new VariableNotDeclaredYetError("wind")));
    }

    @Test public void
    typeExpressionsOfFormalArgumentsAreLookedUp() {
        VariableIdentifierNode typeReference = Nodes.id("String");
        SyntaxNode source = new ShortLambdaExpressionNode(
            asList(new FormalArgumentNode("first", typeReference), new FormalArgumentNode("second", Nodes.id("Number"))),
            Option.<ExpressionNode>none(),
            Nodes.id("first")
        );
        assertThat(resolveReferences(source), hasReference(typeReference, CoreModule.GLOBAL_DECLARATIONS.get("String")));
    }

    @Test public void
    typeExpressionsOfShortLambdaReturnTypeAreLookedUp() {
        VariableIdentifierNode typeReference = Nodes.id("String");
        SyntaxNode source = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            Option.<ExpressionNode>some(typeReference),
            Nodes.string("Falling from your mouth")
        );
        assertThat(resolveReferences(source), hasReference(typeReference, CoreModule.GLOBAL_DECLARATIONS.get("String")));
    }

    @Test public void
    typeExpressionsOfLongLambdaReturnTypeAreLookedUp() {
        VariableIdentifierNode typeReference = Nodes.id("String");
        SyntaxNode source = new LongLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            typeReference,
            Nodes.block(Nodes.returnStatement(Nodes.string("Falling from your mouth")))
        );
        assertThat(resolveReferences(source), hasReference(typeReference, CoreModule.GLOBAL_DECLARATIONS.get("String")));
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
                        mismatchDescription.appendText("referent was " + actualReferent);
                        return false;
                    }
                } else {
                    mismatchDescription.appendText("result was failure: " + CompilerTesting.errorDescriptions(result));
                    return false;
                }
            }
        };
    }
    
    private Matcher<ReferenceResolverResult> isFailureWithErrors(CompilerErrorDescription... errorsArray) {
        List<CompilerErrorDescription> errors = asList(errorsArray);
        return hasErrors("failure with errors: " + errors, errors);
    }

    private Matcher<ReferenceResolverResult> isSuccess() {
        return hasErrors("success", Collections.<CompilerErrorDescription>emptyList());
    }
    
    private Matcher<ReferenceResolverResult> hasErrors(final String matcherDescription, final List<CompilerErrorDescription> errors) {
        return new TypeSafeDiagnosingMatcher<ReferenceResolverResult>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(matcherDescription);
            }

            @Override
            protected boolean matchesSafely(ReferenceResolverResult item, Description mismatchDescription) {
                List<CompilerErrorDescription> actualErrors = CompilerTesting.errorDescriptions(item);
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
