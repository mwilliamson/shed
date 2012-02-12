package org.zwobble.shed.compiler.types;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.types.ScalarFormalTypeParameter.Variance;

public class TypeMatchers {
    public static Matcher<ClassType> classTypeWithName(final FullyQualifiedName name) {
        return scalarTypeWithName(name);
    }
    
    public static Matcher<InterfaceType> interfaceTypeWithName(final FullyQualifiedName name) {
        return scalarTypeWithName(name);
    }
    
    public static Matcher<ScalarFormalTypeParameter> invariantFormalTypeParameterWithName(final String name) {
        return new TypeSafeDiagnosingMatcher<ScalarFormalTypeParameter>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Invariant formal type parameter " + name);
            }

            @Override
            protected boolean matchesSafely(ScalarFormalTypeParameter type, Description mismatchDescription) {
                if (!type.getVariance().equals(Variance.INVARIANT)) {
                    mismatchDescription.appendText("had variance " + type.getVariance());
                    return false;
                }
                if (!type.getName().equals(name)) {
                    mismatchDescription.appendText("had name " + type.getName());
                    return false;
                }
                return true;
            }
        };
    }
    
    private static <T extends ScalarType> Matcher<T> scalarTypeWithName(final FullyQualifiedName name) {
        return new TypeSafeDiagnosingMatcher<T>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Type with name " + name);
            }

            @Override
            protected boolean matchesSafely(T type, Description mismatchDescription) {
                if (!type.getFullyQualifiedName().equals(name)) {
                    mismatchDescription.appendText("had name " + type.getFullyQualifiedName());
                    return false;
                }
                return true;
            }
        };
    }
}
