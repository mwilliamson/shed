package org.zwobble.shed.compiler.typechecker;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.zwobble.shed.compiler.Results;

public class TypeResultMatchers {
    public static <T> Matcher<TypeResult<T>> isSuccessWithValue(final T value) {
        return new TypeSafeDiagnosingMatcher<TypeResult<T>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Success with value " + value);
            }

            @Override
            protected boolean matchesSafely(TypeResult<T> item, Description mismatchDescription) {
                if (!Results.isSuccess(item)) {
                    mismatchDescription.appendText("unsuccessful result, errors were " + item.getErrors());
                    return false;
                }
                if (!item.hasValue()) {
                    mismatchDescription.appendText("result had no value");
                    return false;
                }
                if (!item.getOrThrow().equals(value)) {
                    mismatchDescription.appendText("value was " + item.getOrThrow());
                    return false;
                }
                return true;
            }
        };
    }
}
