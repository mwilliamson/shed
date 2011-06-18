package org.zwobble.shed.compiler.parsing;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.tokeniser.Tokeniser;

public class ParserTesting {
    public static TokenIterator tokens(String input) {
        return new TokenIterator(new Tokeniser().tokenise(input));
    }
    
    public static Matcher<Result<?>> isSuccessWithNode(final SyntaxNode node) {
        return new TypeSafeDiagnosingMatcher<Result<?>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Success with node " + node);
            }

            @Override
            protected boolean matchesSafely(Result<?> item, Description mismatchDescription) {
                boolean result = item.isSuccess() && node.equals(item.get());
                if (!result) {
                    mismatchDescription.appendText("got " + item);
                }
                return result;
            }
        };
    }
}
