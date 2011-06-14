package org.zwobble.shed.compiler.types;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.Result;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.parsing.Result.success;
import static org.zwobble.shed.compiler.types.TypeLookup.lookupTypeReference;

public class TypeLookupTest {
    @Test public void
    canLookupTypesFromContext() {
        StaticContext context = new StaticContext();
        context.add("String", new TypeApplication(CoreTypes.CLASS, asList(CoreTypes.STRING)));

        Result<Type> result = lookupTypeReference(new TypeIdentifierNode("String"), context);
        assertThat(result, is(success(CoreTypes.STRING)));
    }
    
    @Test public void
    errorIfVariableDoesNotReferenceAFunctionTypeInCurrentContext() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.NUMBER);
        Result<Type> result = lookupTypeReference(new TypeIdentifierNode("String"), context);
        assertThat(errorStrings(result), is(asList("\"String\" is not a type but an instance of \"Number\"")));
    }
    
    @Test public void
    errorIfVariableDoesNotReferenceTypeInCurrentContext() {
        StaticContext context = new StaticContext();
        TypeFunction listType = new TypeFunction(Collections.<String>emptyList(), "List", asList(new FormalTypeParameter("E")));
        TypeApplication type = new TypeApplication(listType, asList(CoreTypes.NUMBER));
        context.add("String", type);
        Result<Type> result = lookupTypeReference(new TypeIdentifierNode("String"), context);
        assertThat(errorStrings(result), is(asList("\"String\" is not a type but an instance of \"List[Number]\"")));
    }
}
