package org.zwobble.shed.compiler.types;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.invariantFormalTypeParameter;
import static org.zwobble.shed.compiler.types.FormalTypeParameters.formalTypeParameters;
import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;

public class ParameterisedTypeTest {
    @Test public void
    shortNameIncludesParameters() {
        ParameterisedType typeFunction = parameterisedType(
            new InterfaceType(fullyQualifiedName("shed", "Map")),
            formalTypeParameters(invariantFormalTypeParameter("K"), invariantFormalTypeParameter("V"))
        );
        assertThat(typeFunction.shortName(), is("[K, V] -> Class[Map[K, V]]"));
    }
}
