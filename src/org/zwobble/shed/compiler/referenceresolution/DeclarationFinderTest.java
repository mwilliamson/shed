package org.zwobble.shed.compiler.referenceresolution;

import java.util.Set;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class DeclarationFinderTest {
    private final DeclarationFinder finder = new DeclarationFinder();
    
    @Test public void
    findsVariableDeclarations() {
        Set<String> declarations = finder.findDeclarations(Nodes.immutableVar("x", Nodes.string("go")));
        assertThat(declarations, containsInAnyOrder("x"));
    }
    
    @Test public void
    findsPublicVariableDeclarations() {
        Set<String> declarations = finder.findDeclarations(Nodes.publik(Nodes.immutableVar("x", Nodes.string("go"))));
        assertThat(declarations, containsInAnyOrder("x"));
    }
}
