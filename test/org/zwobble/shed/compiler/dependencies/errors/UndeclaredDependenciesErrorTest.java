package org.zwobble.shed.compiler.dependencies.errors;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UndeclaredDependenciesErrorTest {
    @Test public void
    messageForSingleUndeclaredDependencyDisplaysThatIdentifer() {
        assertThat(new UndeclaredDependenciesError(asList("x")).describe(), is("Reference to undeclared variable x"));
    }
    
    @Test public void
    messageForChainLeadingToUndeclaredDependencyDisplaysAllIdentifiers() {
        assertThat(
            new UndeclaredDependenciesError(asList("x", "y", "z")).describe(),
            is("Reference to undeclared variable z, which is required to declare y, which is required to declare x")
        );
    }
}
