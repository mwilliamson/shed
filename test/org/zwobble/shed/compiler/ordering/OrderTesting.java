package org.zwobble.shed.compiler.ordering;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;

public class OrderTesting {
    public static Matcher<TypeResult<Iterable<StatementNode>>> isOrdering(final StatementNode... statements) {
        return allOf(isSuccess(), new TypeSafeDiagnosingMatcher<TypeResult<Iterable<StatementNode>>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Ordering: " + asList(statements));
            }

            @Override
            protected boolean matchesSafely(TypeResult<Iterable<StatementNode>> item, Description mismatchDescription) {
                Matcher<Iterable<? extends StatementNode>> orderMatcher = contains(statements);
                if (orderMatcher.matches(item.get())) {
                    return true;
                } else {
                    orderMatcher.describeMismatch(item.get(), mismatchDescription);
                    return false;
                }
            }
        });
    }
}
