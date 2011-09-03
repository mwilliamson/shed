package org.zwobble.shed.compiler.typechecker;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.FormalTypeParameter;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.types.ParameterisedType;

import com.google.common.collect.ImmutableMap;

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
        context.add("String", TypeApplication.applyTypes(CoreTypes.CLASS, asList(CoreTypes.STRING)));

        TypeResult<Type> result = lookupTypeReference(new VariableIdentifierNode("String"), context);
        assertThat(result, is(success(CoreTypes.STRING)));
    }
    
    @Test public void
    errorIfVariableDoesNotReferenceAFunctionTypeInCurrentContext() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.NUMBER);
        TypeResult<Type> result = lookupTypeReference(new VariableIdentifierNode("String"), context);
        assertThat(errorStrings(result), is(asList("Not a type but an instance of \"Number\"")));
    }
    
    @Test public void
    errorIncludesNodeLocation() {
        StaticContext context = new StaticContext();
        context.add("String", CoreTypes.NUMBER);
        VariableIdentifierNode node = new VariableIdentifierNode("String");
        nodeLocations.put(node, range(position(3, 5), position(7, 4)));
        TypeResult<Type> result = lookupTypeReference(node, context);
        assertThat(
            result.getErrors(),
            is(asList(new CompilerError(range(position(3, 5), position(7, 4)), "Not a type but an instance of \"Number\"")))
        );
    }
    
    @Test public void
    errorIfVariableDoesNotReferenceTypeInCurrentContext() {
        StaticContext context = new StaticContext();
        ParameterisedType listType = new ParameterisedType(
            new ClassType(Collections.<String>emptyList(), "List", Collections.<InterfaceType>emptySet(), ImmutableMap.<String, Type>of()),
            asList(new FormalTypeParameter("E"))
        );
        Type type = TypeApplication.applyTypes(listType, asList(CoreTypes.NUMBER));
        context.add("String", type);
        TypeResult<Type> result = lookupTypeReference(new VariableIdentifierNode("String"), context);
        assertThat(errorStrings(result), is(asList("Not a type but an instance of \"List[Number]\"")));
    }
    
    private TypeResult<Type> lookupTypeReference(ExpressionNode typeReference, StaticContext context) {
        return TypeLookup.lookupTypeReference(typeReference, nodeLocations, context);
    }
}
