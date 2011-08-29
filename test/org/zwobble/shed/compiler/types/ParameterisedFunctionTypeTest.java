package org.zwobble.shed.compiler.types;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ParameterisedFunctionTypeTest {
    @Test public void
    shortNameIncludesParameters() {
        FormalTypeParameter typeParameter = new FormalTypeParameter("T");
        ParameterisedFunctionType function = new ParameterisedFunctionType(
            CoreTypes.functionTypeOf(typeParameter, typeParameter),
            asList(typeParameter)
        );
        
        assertThat(function.shortName(), is("[T] -> Function1[T, T]"));
    }
}
