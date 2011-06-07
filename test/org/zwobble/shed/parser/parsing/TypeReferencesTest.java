package org.zwobble.shed.parser.parsing;

import org.junit.Test;
import org.zwobble.shed.parser.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.parser.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.parser.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.parser.tokeniser.Tokeniser;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.parser.parsing.Result.success;

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
    
    private TokenIterator tokens(String input) {
        return new TokenIterator(new Tokeniser().tokenise(input));
    }
    
}
