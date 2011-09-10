package org.zwobble.shed.compiler.parsing;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.tokeniser.Tokeniser;

public class ParserTesting {
    public static TokenNavigator tokens(String input) {
        return new TokenNavigator(new Tokeniser().tokenise(input));
    }
    
    public static Matcher<ParseResult<?>> isSuccessWithNode(final SyntaxNode node) {
        return new TypeSafeDiagnosingMatcher<ParseResult<?>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Success with node " + node);
            }

            @Override
            protected boolean matchesSafely(ParseResult<?> item, Description mismatchDescription) {
                boolean result = item.isSuccess() && node.equals(item.get());
                if (!result) {
                    mismatchDescription.appendText("got " + item);
                }
                return result;
            }
        };
    }
}
