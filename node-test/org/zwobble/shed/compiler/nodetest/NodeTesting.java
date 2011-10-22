package org.zwobble.shed.compiler.nodetest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class NodeTesting {
    public static Matcher<NodeExecutionResult> isSuccessWithOutput(final String expectedOutput) {
        return new TypeSafeDiagnosingMatcher<NodeExecutionResult>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("success with output: " + expectedOutput);
            }

            @Override
            protected boolean matchesSafely(NodeExecutionResult item, Description mismatchDescription) {
                if (item.getReturnCode() != 0) {
                    mismatchDescription.appendText("had return code " + item.getReturnCode() + " (err: " + item.getErrorOutput() + ")");
                    return false;
                }
                if (!item.getOutput().equals(expectedOutput)) {
                    mismatchDescription.appendText("had output: " + item.getOutput());
                    return false;
                }
                return true;
            }
        };
    }
}
