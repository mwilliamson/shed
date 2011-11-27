package org.zwobble.shed.compiler;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static org.hamcrest.Matchers.containsInAnyOrder;

public class CompilerTesting {
    public static List<String> errorStrings(HasErrors result) {
        return Lists.transform(result.getErrors(), toErrorString());
    }
    
    private static Function<CompilerError, String> toErrorString() {
        return new Function<CompilerError, String>() {
            @Override
            public String apply(CompilerError input) {
                return input.describe();
            }
        };
    }

    public static List<CompilerErrorDescription> errorDescriptions(HasErrors result) {
        return Lists.transform(result.getErrors(), toDescription());
    }
    
    private static Function<CompilerError, CompilerErrorDescription> toDescription() {
        return new Function<CompilerError, CompilerErrorDescription>() {
            @Override
            public CompilerErrorDescription apply(CompilerError input) {
                return input.getDescription();
            }
        };
    }

    public static Matcher<HasErrors> isSuccess() {
        return hasErrors("success", Matchers.<CompilerErrorDescription>emptyIterable());
    }
    
    public static TypeSafeDiagnosingMatcher<HasErrors> isFailureWithErrors(Matcher<Iterable<? extends CompilerErrorDescription>> errorsMatcher) {
        final HasErrorsMatcher errorMatcher = hasErrors("failure with errors: " + description(errorsMatcher), errorsMatcher);
        
        return new TypeSafeDiagnosingMatcher<HasErrors>() {
            @Override
            public void describeTo(Description description) {
                errorMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(HasErrors item, Description mismatchDescription) {
                if (item.isSuccess()) {
                    mismatchDescription.appendText("was success");
                    return false;
                } else {
                    return errorMatcher.matchesSafely(item, mismatchDescription);
                }
            }
        };
    }
    
    public static TypeSafeDiagnosingMatcher<HasErrors> isFailureWithErrors(CompilerErrorDescription... errorsArray) {
        return isFailureWithErrors(containsInAnyOrder(errorsArray));
    }
    
    private static HasErrorsMatcher hasErrors(final String matcherDescription, final Matcher<? extends Iterable<? extends CompilerErrorDescription>> errorsMatcher) {
        return new HasErrorsMatcher(matcherDescription, errorsMatcher);
    }
    
    private static class HasErrorsMatcher extends TypeSafeDiagnosingMatcher<HasErrors> {
        private final String matcherDescription;
        private final Matcher<? extends Iterable<? extends CompilerErrorDescription>> errorsMatcher;

        public HasErrorsMatcher(String matcherDescription, Matcher<? extends Iterable<? extends CompilerErrorDescription>> errorsMatcher) {
            this.matcherDescription = matcherDescription;
            this.errorsMatcher = errorsMatcher;
        }
        
        @Override
        public void describeTo(Description description) {
            description.appendText(matcherDescription);
        }

        @Override
        protected boolean matchesSafely(HasErrors item, Description mismatchDescription) {
            List<CompilerErrorDescription> actualErrors = errorDescriptions(item);
            if (errorsMatcher.matches(actualErrors)) {
                return true;
            } else {
                errorsMatcher.describeMismatch(actualErrors, mismatchDescription);
                return false;
            }
        }
    }
    
    private static String description(Matcher<?> matcher) {
        Description description = new StringDescription();
        matcher.describeTo(description);
        return description.toString();
    }
}
