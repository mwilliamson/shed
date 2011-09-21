package org.zwobble.shed.compiler.types;

import org.junit.Test;
import org.zwobble.shed.compiler.typechecker.ValueInfo;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

public class ParameterisedTypeTest {
    @Test public void
    shortNameIncludesParameters() {
        ParameterisedType typeFunction = new ParameterisedType(
            new InterfaceType(fullyQualifiedName("shed", "Map"), ImmutableMap.<String, ValueInfo>of()),
            asList(new FormalTypeParameter("K"), new FormalTypeParameter("V"))
        );
        assertThat(typeFunction.shortName(), is("[K, V] -> Class[Map[K, V]]"));
    }
}
