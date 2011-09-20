package org.zwobble.shed.compiler;

import java.util.Collections;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static java.util.Arrays.asList;

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
        return hasErrors("success", Collections.<CompilerErrorDescription>emptyList());
    }
    
    public static TypeSafeDiagnosingMatcher<HasErrors> isFailureWithErrors(CompilerErrorDescription... errorsArray) {
        List<CompilerErrorDescription> errors = asList(errorsArray);
        final HasErrorsMatcher errorMatcher = hasErrors("failure with errors: " + errors, errors);
        
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
    
    private static HasErrorsMatcher hasErrors(final String matcherDescription, final List<CompilerErrorDescription> errors) {
        return new HasErrorsMatcher(matcherDescription, errors);
    }
    
    private static class HasErrorsMatcher extends TypeSafeDiagnosingMatcher<HasErrors> {
        private final String matcherDescription;
        private final List<CompilerErrorDescription> errors;

        public HasErrorsMatcher(String matcherDescription, List<CompilerErrorDescription> errors) {
            this.matcherDescription = matcherDescription;
            this.errors = errors;
        }
        
        @Override
        public void describeTo(Description description) {
            description.appendText(matcherDescription);
        }

        @Override
        protected boolean matchesSafely(HasErrors item, Description mismatchDescription) {
            List<CompilerErrorDescription> actualErrors = errorDescriptions(item);
            if (actualErrors.equals(errors)) {
                return true;
            } else {
                mismatchDescription.appendText("had errors: " + actualErrors);
                return false;
            }
        }
    }
}
