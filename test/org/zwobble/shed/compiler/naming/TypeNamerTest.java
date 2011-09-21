package org.zwobble.shed.compiler.naming;

import org.junit.Ignore;
import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;

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
    
    @Ignore
    @Test public void
    nestedTypesIncludeNameOfParent() {
        ObjectDeclarationNode nestedObjectDeclaration = Nodes.object("road", Nodes.block());
        ObjectDeclarationNode objectDeclaration = Nodes.object("neverEnding", Nodes.block(nestedObjectDeclaration));
        FullyQualifiedNames names = generateNames(objectDeclaration);
        assertThat(names.fullyQualifiedNameOf(nestedObjectDeclaration), is(fullyQualifiedName("neverEnding", "road")));
    }
    
    private FullyQualifiedNames generateNames(TypeDeclarationNode node) {
        return namer.generateFullyQualifiedNames(node);
    }
}
