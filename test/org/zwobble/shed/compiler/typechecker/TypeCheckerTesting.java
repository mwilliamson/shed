package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.CompilerTesting;

import static java.util.Arrays.asList;

public class TypeCheckerTesting {
    public static Matcher<TypeResult<?>> isFailureWithErrors(CompilerErrorDescription... errorsArray) {
        // TODO: unify with CompilerTesting
        final List<CompilerErrorDescription> errors = asList(errorsArray);
        return new TypeSafeDiagnosingMatcher<TypeResult<?>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("failure with errors: " + errors);
            }

            @Override
            protected boolean matchesSafely(TypeResult<?> item, Description mismatchDescription) {
                List<CompilerErrorDescription> actualErrors = CompilerTesting.errorDescriptions(item);
                if (item.isSuccess()) {
                    mismatchDescription.appendText("was success");
                    return false;
                } else if (actualErrors.equals(errors)) {
                    return true;
                } else {
                    mismatchDescription.appendText("had errors: " + actualErrors);
                    return false;
                }
            }
        };
    }
}
