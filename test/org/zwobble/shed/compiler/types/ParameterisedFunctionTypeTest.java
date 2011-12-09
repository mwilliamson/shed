package org.zwobble.shed.compiler.types;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.types.FormalTypeParameters.formalTypeParameters;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.invariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.Types.typeParameters;

public class ParameterisedFunctionTypeTest {
    @Test public void
    shortNameIncludesParameters() {
        FormalTypeParameter typeParameter = invariantFormalTypeParameter("T");
        ParameterisedFunctionType function = new ParameterisedFunctionType(
            typeParameters(typeParameter, typeParameter),
            formalTypeParameters(typeParameter)
        );
        
        assertThat(function.shortName(), is("[T] -> Function[T, T]"));
    }
}
