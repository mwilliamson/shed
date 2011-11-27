package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration.globalDeclaration;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class TypeLookupTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    
    @Test public void
    canLookupTypesFromContext() {
        TypeResult<Type> result = lookupTypeReference(fixture.stringTypeReference(), standardContext());
        assertThat(result, isSuccessWithValue((Type)CoreTypes.STRING));
    }
    
    @Test public void
    errorIfVariableDoesNotReferenceAFunctionTypeInCurrentContext() {
        VariableIdentifierNode reference = Nodes.id("length");
        GlobalDeclaration declaration = globalDeclaration("length");
        fixture.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        context.add(declaration, unassignableValue(CoreTypes.DOUBLE));
        
        TypeResult<Type> result = lookupTypeReference(reference, context);
        assertThat(errorStrings(result), is(asList("Not a type but an instance of \"Double\"")));
    }
    
    @Test public void
    errorIncludesNodeLocation() {
        VariableIdentifierNode reference = Nodes.id("length");
        GlobalDeclaration declaration = globalDeclaration("length");
        fixture.addReference(reference, declaration);
        
        StaticContext context = standardContext();
        context.add(declaration, unassignableValue(CoreTypes.DOUBLE));
        
        TypeResult<Type> result = lookupTypeReference(reference, context);
        assertThat(
            result.getErrors(),
            is((Object)asList(error(reference, "Not a type but an instance of \"Double\"")))
        );
    }
    
    private TypeResultWithValue<Type> lookupTypeReference(ExpressionNode typeReference, StaticContext context) {
        TypeLookup typeLookup = fixture.get(TypeLookup.class);
        return typeLookup.lookupTypeReference(typeReference);
    }
    
    private StaticContext standardContext() {
        return fixture.context();
    }
}
