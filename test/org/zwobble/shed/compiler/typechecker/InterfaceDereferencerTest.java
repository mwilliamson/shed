package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.typechecker.errors.NotAnInterfaceError;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.Interfaces;

import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

public class InterfaceDereferencerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build(); 
    
    @Test public void
    interfacesAreDeferenced() {
        TypeResult<Interfaces> result = dereference(asList((ExpressionNode)fixture.interfaceTypeReference()));
        assertThat(result, isSuccessWithValue(interfaces(fixture.interfaceType())));
    }
    
    @Test public void
    errorIfReferringToVariableNotInScope() {
        TypeResult<Interfaces> result = dereference(asList((ExpressionNode)Nodes.id("Person")));
        assertThat(result, is(isFailureWithErrors(new UntypedReferenceError("Person"))));
    }
    
    @Test public void
    errorIfReferenceInInterfaceListIsNotToAnInterfaceType() {
        TypeResult<Interfaces> result = dereference(asList((ExpressionNode)fixture.implementingClassTypeReference()));
        assertThat(result, is(isFailureWithErrors(new NotAnInterfaceError(fixture.implementingClassType()))));
    }
    
    @Test public void
    validInterfaceReferencesAreDereferencedInThePresenceOfInvalidInterfaceReferences() {
        TypeResult<Interfaces> result = dereference(asList((ExpressionNode)fixture.implementingClassTypeReference(), fixture.interfaceTypeReference()));
        assertThat(result, is(isFailureWithErrors(new NotAnInterfaceError(fixture.implementingClassType()))));
        assertThat(result.getOrThrow(), is(interfaces(fixture.interfaceType())));
    }
    
    private TypeResult<Interfaces> dereference(List<ExpressionNode> interfaces) {
        return fixture.get(InterfaceDereferencer.class).dereferenceInterfaces(interfaces);
    }
}
