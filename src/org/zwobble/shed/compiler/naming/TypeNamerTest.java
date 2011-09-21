package org.zwobble.shed.compiler.naming;

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
        assertThat(nameOf(objectDeclaration), is(fullyQualifiedName("bob")));
    }
    
    private FullyQualifiedName nameOf(TypeDeclarationNode node) {
        return namer.generateFullyQualifiedNames(node).fullyQualifiedNameOf(node);
    }
}
