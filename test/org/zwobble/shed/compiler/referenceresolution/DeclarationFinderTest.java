package org.zwobble.shed.compiler.referenceresolution;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

import static java.util.Arrays.asList;
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
    
    @Test public void
    findsImportDeclarations() {
        SyntaxNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            asList(new ImportNode(asList("shed", "collections", "List"))),
            Collections.<StatementNode>emptyList()
        );
        Set<String> declarations = finder.findDeclarations(source);
        assertThat(declarations, containsInAnyOrder("List"));
    }
    
    @Test public void
    findsDeclarationsInSourceBody() {
        SyntaxNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            Collections.<ImportNode>emptyList(),
            Arrays.<StatementNode>asList(Nodes.immutableVar("go", Nodes.bool(true)))
        );
        Set<String> declarations = finder.findDeclarations(source);
        assertThat(declarations, containsInAnyOrder("go"));
    }
}
