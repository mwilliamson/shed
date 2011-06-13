package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.TypeReferences;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;

import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

import static org.zwobble.shed.compiler.parsing.Result.success;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TypeReferencesTest {
    @Test public void
    typeReferenceCanBeIdentifierOfType() {
        assertThat(
            TypeReferences.typeReference().parse(tokens("Integer")),
            is(success((TypeReferenceNode)new TypeIdentifierNode("Integer")))
        );
    }
    
    @Test public void
    typeReferenceCanBeIdentifierOfGenericTypeParameterisedByScalarTypes() {
        assertThat(
            TypeReferences.typeReference().parse(tokens("List[Integer]")),
            is(success((TypeReferenceNode)new TypeApplicationNode(
                new TypeIdentifierNode("List"),
                asList((TypeReferenceNode)new TypeIdentifierNode("Integer"))
            )))
        );
    }
    
}
