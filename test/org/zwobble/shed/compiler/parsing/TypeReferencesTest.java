package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.parsing.ParserTesting.isSuccessWithNode;
import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

public class TypeReferencesTest {
    @Test public void
    typeReferenceCanBeIdentifierOfType() {
        assertThat(
            TypeReferences.typeReference().parse(tokens("Integer")),
            isSuccessWithNode(new TypeIdentifierNode("Integer"))
        );
    }
    
    @Test public void
    typeReferenceCanBeIdentifierOfGenericTypeParameterisedByScalarTypes() {
        assertThat(
            TypeReferences.typeReference().parse(tokens("List[Integer]")),
            isSuccessWithNode(new TypeApplicationNode(
                new TypeIdentifierNode("List"),
                asList((TypeReferenceNode)new TypeIdentifierNode("Integer"))
            ))
        );
    }
    
}
