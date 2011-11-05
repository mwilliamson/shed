package org.zwobble.shed.compiler.typechecker;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.CoreTypes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class VariableLookupTest {
    private final TypeCheckerTestFixture fixture = TypeCheckerTestFixture.build();
    private final VariableIdentifierNode reference = Nodes.id("name");
    private final VariableDeclarationNode declaration = Nodes.immutableVar("name", Nodes.string("Bob"));
    
    @Test public void
    failsToLookupVariableIfNotInContext() {
        assertThat(lookup(reference), isFailureWithErrors(new UntypedReferenceError("name")));
    }
    
    @Test public void
    failsToLookupVariableIfTypeOfValueIsUnknown() {
        fixture.addReference(reference, declaration);
        fixture.context().add(declaration, ValueInfo.unknown());
        assertThat(lookup(reference), isFailureWithErrors(new UntypedReferenceError("name")));
    }
    
    @Test public void
    looksUpValueInfoUsingStaticContext() {
        fixture.addReference(reference, declaration);
        fixture.context().add(declaration, unassignableValue(CoreTypes.STRING));
        assertThat(lookup(reference), is(success(unassignableValue(CoreTypes.STRING))));
    }
    
    private TypeResult<ValueInfo> lookup(VariableIdentifierNode reference) {
        return fixture.get(VariableLookup.class).lookupVariableReference(reference);
    }
}
