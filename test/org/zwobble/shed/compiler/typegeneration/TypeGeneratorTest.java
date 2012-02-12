package org.zwobble.shed.compiler.typegeneration;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNamesBuilder;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalTypeParameterNode;
import org.zwobble.shed.compiler.parsing.nodes.InterfaceDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeDeclarationNode;
import org.zwobble.shed.compiler.types.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.TypeMatchers.classTypeWithName;
import static org.zwobble.shed.compiler.types.TypeMatchers.interfaceTypeWithName;
import static org.zwobble.shed.compiler.types.TypeMatchers.invariantFormalTypeParameterWithName;

public class TypeGeneratorTest {
    private final FullyQualifiedNamesBuilder names = new FullyQualifiedNamesBuilder();
    
    @Test public void
    typesAreGeneratedForClassDeclarations() {
        ClassDeclarationNode declarationNode = Nodes.clazz("Song", Nodes.formalArguments(), Nodes.block());
        FullyQualifiedName name = fullyQualifiedName("shed", "music", "Song");
        names.addFullyQualifiedName(declarationNode, name);
        assertThat(generate(declarationNode), isTypeStoreWith(declarationNode, classTypeWithName(name)));
    }
    
    @Test public void
    typesAreGeneratedForInterfaceDeclarations() {
        InterfaceDeclarationNode declarationNode = Nodes.interfaceDeclaration("Song", Nodes.interfaceBody());
        FullyQualifiedName name = fullyQualifiedName("shed", "music", "Song");
        names.addFullyQualifiedName(declarationNode, name);
        assertThat(generate(declarationNode), isTypeStoreWith(declarationNode, interfaceTypeWithName(name)));
    }
    
    @Test public void
    typesAreGeneratedForObjectDeclarations() {
        ObjectDeclarationNode declarationNode = Nodes.object("song", Nodes.block());
        FullyQualifiedName name = fullyQualifiedName("shed", "music", "song");
        names.addFullyQualifiedName(declarationNode, name);
        assertThat(generate(declarationNode), isTypeStoreWith(declarationNode, interfaceTypeWithName(name)));
    }
    
    @Test public void
    typesAreGeneratedForInvariantFormalTypeParameters() {
        FormalTypeParameterNode formalTypeParameter = Nodes.formalTypeParameter("T"); 
        assertThat(generate(formalTypeParameter), isTypeStoreWith(formalTypeParameter, invariantFormalTypeParameterWithName("T")));
    }
    
    @Test public void
    typesAreGeneratedForChildrenOfNodes() {
        InterfaceDeclarationNode declarationNode = Nodes.interfaceDeclaration("Song", Nodes.interfaceBody());
        FullyQualifiedName name = fullyQualifiedName("shed", "music", "Song");
        names.addFullyQualifiedName(declarationNode, name);
        assertThat(generate(Nodes.block(declarationNode)), isTypeStoreWith(declarationNode, interfaceTypeWithName(name)));
    }
    
    private Matcher<TypeStore> isTypeStoreWith(final TypeDeclarationNode node, final Matcher<? extends Type> type) {
        return new TypeSafeDiagnosingMatcher<TypeStore>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("type store mapping " + node + " to " + type);
            }

            @Override
            protected boolean matchesSafely(TypeStore typeStore, Description mismatchDescription) {
                Type actualType = typeStore.typeDeclaredBy(node);
                if (!type.matches(actualType)) {
                    mismatchDescription.appendText("was mapped to " + actualType);
                    return false;
                }
                return true;
            }
        };
    }

    private TypeStore generate(SyntaxNode node) {
        return new TypeGenerator(names.build()).generateTypes(node);
    }
}
