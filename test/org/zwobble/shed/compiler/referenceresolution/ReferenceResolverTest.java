package org.zwobble.shed.compiler.referenceresolution;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ReferenceResolverTest {
    private final ReferenceResolver resolver = new ReferenceResolver();
    private static final References EMPTY_REFERENCES = new References(ImmutableMap.<Identity<VariableIdentifierNode>, Identity<DeclarationNode>>of());
    
    @Test public void
    booleanLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.bool(true)), is(EMPTY_REFERENCES));
    }
    
    @Test public void
    stringLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.string("blah")), is(EMPTY_REFERENCES));
    }
    
    @Test public void
    numberLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.number("42")), is(EMPTY_REFERENCES));
    }
    
    @Test public void
    canReferToImmutableReferencesInSameBlock() {
        VariableIdentifierNode reference = Nodes.id("x");
        DeclarationNode declaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(declaration, Nodes.expressionStatement(reference));
        assertThat(resolveReferences(source).findReferent(reference), is(declaration));
    }
    
    private References resolveReferences(SyntaxNode node) {
        return resolver.resolveReferences(node);
    }
}
