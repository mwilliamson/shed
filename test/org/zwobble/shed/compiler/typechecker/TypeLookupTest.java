package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.types.TypeFunction;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;
import static org.zwobble.shed.compiler.parsing.SourceRange.range;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class TypeLookupTest {
    private final SimpleNodeLocations nodeLocations = new SimpleNodeLocations();
    
    @Test public void
    canLookupTypesFromContext() {
        StaticContext context = new StaticContext();
        context.add("String", new TypeApplication(CoreTypes.CLASS, asList(CoreTypes.STRING)));

        TypeResult<Type> result = lookupTypeReference(new TypeIdentifierNode("String"), context);
        assertThat(result, is(success(CoreTypes.STRING)));
    }
    
    @Test public void
    errorIfVariableDoesNotReferenceAFunctionTypeInCurrentContext() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.NUMBER);
        TypeResult<Type> result = lookupTypeReference(new TypeIdentifierNode("String"), context);
        assertThat(errorStrings(result), is(asList("\"String\" is not a type but an instance of \"Number\"")));
    }
    
    @Test public void
    errorIncludesNodeLocation() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.NUMBER);
        TypeIdentifierNode node = new TypeIdentifierNode("String");
        nodeLocations.put(node, range(position(3, 5), position(7, 4)));
        TypeResult<Type> result = lookupTypeReference(node, context);
        assertThat(
            result.getErrors(),
            is(asList(new CompilerError(range(position(3, 5), position(7, 4)), "\"String\" is not a type but an instance of \"Number\"")))
        );
    }
    
    @Test public void
    errorIfVariableDoesNotReferenceTypeInCurrentContext() {
        StaticContext context = new StaticContext();
        TypeFunction listType = new TypeFunction(new ScalarType(Collections.<String>emptyList(), "List"), asList(new FormalTypeParameter("E")));
        TypeApplication type = new TypeApplication(listType, asList(CoreTypes.NUMBER));
        context.add("String", type);
        TypeResult<Type> result = lookupTypeReference(new TypeIdentifierNode("String"), context);
        assertThat(errorStrings(result), is(asList("\"String\" is not a type but an instance of \"List[Number]\"")));
    }
    
    private TypeResult<Type> lookupTypeReference(TypeReferenceNode typeReference, StaticContext context) {
        return TypeLookup.lookupTypeReference(typeReference, nodeLocations, context);
    }
}
