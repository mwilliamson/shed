package org.zwobble.shed.compiler.naming;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

public class TypeNamerTest {
    private final TypeNamer namer = new TypeNamer();
    
    @Test public void
    typesAreNamedByIdentifier() {
        ObjectDeclarationNode objectDeclaration = Nodes.object("bob", Nodes.block());
        FullyQualifiedNames names = generateNames(objectDeclaration);
        assertThat(names.fullyQualifiedNameOf(objectDeclaration), is(fullyQualifiedName("bob")));
    }
    
    @Test public void
    nestedTypesIncludeNameOfParent() {
        ObjectDeclarationNode nestedObjectDeclaration = Nodes.object("road", Nodes.block());
        ObjectDeclarationNode objectDeclaration = Nodes.object("neverEnding", Nodes.block(nestedObjectDeclaration));
        FullyQualifiedNames names = generateNames(objectDeclaration);
        assertThat(names.fullyQualifiedNameOf(nestedObjectDeclaration), is(fullyQualifiedName("neverEnding", "road")));
    }
    
    @Test public void
    sourceFileAddsPackageDeclarationToName() {
        ObjectDeclarationNode objectDeclaration = Nodes.object("bob", Nodes.block());
        List<StatementNode> body = Arrays.<StatementNode>asList(objectDeclaration);
        SourceNode source = Nodes.source(Nodes.packageDeclaration("shed", "example"), Collections.<ImportNode>emptyList(), body);
        FullyQualifiedNames names = generateNames(source);
        assertThat(names.fullyQualifiedNameOf(objectDeclaration), is(fullyQualifiedName("shed", "example", "bob")));
    }
    
    private FullyQualifiedNames generateNames(SyntaxNode node) {
        return namer.generateFullyQualifiedNames(node);
    }
}
