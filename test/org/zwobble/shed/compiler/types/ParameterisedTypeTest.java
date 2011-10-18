package org.zwobble.shed.compiler.types;

import org.junit.Test;

import static org.zwobble.shed.compiler.types.ParameterisedType.parameterisedType;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

public class ParameterisedTypeTest {
    @Test public void
    shortNameIncludesParameters() {
        ParameterisedType typeFunction = parameterisedType(
            new InterfaceType(fullyQualifiedName("shed", "Map")),
            asList(new FormalTypeParameter("K"), new FormalTypeParameter("V"))
        );
        assertThat(typeFunction.shortName(), is("[K, V] -> Class[Map[K, V]]"));
    }
}
