package org.zwobble.shed.compiler.typechecker.expressions;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.TypeCheckerTestFixture;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.CoreTypes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.typechecker.TypeResultMatchers.isSuccessWithValue;
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
    looksUpValueInfoUsingStaticContext() {
        fixture.addReference(reference, declaration);
        fixture.context().add(declaration, unassignableValue(CoreTypes.STRING));
        assertThat(lookup(reference), isSuccessWithValue(unassignableValue(CoreTypes.STRING)));
    }
    
    private TypeResult<ValueInfo> lookup(VariableIdentifierNode reference) {
        return fixture.get(VariableLookup.class).inferValueInfo(reference);
    }
}
