package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.types.Interfaces;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

public class InterfaceDereferencerTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build(); 
    
    @Test public void
    interfacesAreDeferenced() {
        TypeResult<Interfaces> result = dereference(asList((ExpressionNode)fixture.interfaceTypeReference()));
        assertThat(result, is(success(interfaces(fixture.interfaceType()))));
    }
    
    private TypeResult<Interfaces> dereference(List<ExpressionNode> interfaces) {
        return fixture.get(InterfaceDereferencer.class).dereferenceInterfaces(interfaces);
    }
}
