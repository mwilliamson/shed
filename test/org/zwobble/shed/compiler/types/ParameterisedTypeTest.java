package org.zwobble.shed.compiler.types;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ParameterisedTypeTest {
    @Test public void
    shortNameIncludesParameters() {
        ParameterisedType typeFunction = new ParameterisedType(
            new InterfaceType(asList("shed"), "Map", ImmutableMap.<String, Type>of()),
            asList(new FormalTypeParameter("K"), new FormalTypeParameter("V"))
        );
        assertThat(typeFunction.shortName(), is("[K, V] -> Class[Map[K, V]]"));
    }
}
