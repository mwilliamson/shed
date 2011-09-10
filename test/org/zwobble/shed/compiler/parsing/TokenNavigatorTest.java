package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.tokeniser.Token;

import static org.zwobble.shed.compiler.parsing.SourcePosition.position;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TokenNavigatorTest {
    @Test public void
    canPeekAtNextTokenWithoutAdvancingIterator() {
        TokenNavigator navigator = navigator("1+2");
        assertThat(navigator.peek().getToken(), is(Token.number("1")));
        assertThat(navigator.peek().getToken(), is(Token.number("1")));
    }
    
    @Test public void
    canPeekAtAbritraryNextTokenWithoutAdvancingIterator() {
        TokenNavigator navigator = navigator("1+2");
        assertThat(navigator.peek(1).getToken(), is(Token.symbol("+")));
        assertThat(navigator.peek(1).getToken(), is(Token.symbol("+")));
    }
    
    @Test public void
    nextReturnsNextTokenAndAdvancesIterator() {
        TokenNavigator navigator = navigator("1+2");
        assertThat(navigator.next().getToken(), is(Token.number("1")));
        assertThat(navigator.next().getToken(), is(Token.symbol("+")));
    }
    
    @Test public void
    canGetStartingPositionOfNavigator() {
        TokenNavigator navigator = navigator("1+2");
        assertThat(navigator.currentPosition(), is(position(1, 1)));
    }
    
    @Test public void
    canGetPositionOfNavigatorAfterAdvancement() {
        TokenNavigator navigator = navigator("1+2");
        navigator.next();
        navigator.next();
        assertThat(navigator.currentPosition(), is(position(1, 3)));
    }
    
    @Test public void
    canGetPositionOfNavigatorHavingReachedTheEndOfTokens() {
        TokenNavigator navigator = navigator("1+2");
        navigator.next();
        navigator.next();
        navigator.next();
        navigator.next();
        assertThat(navigator.currentPosition(), is(position(1, 4)));
    }
    
    @Test public void
    canResetNavigator() {
        TokenNavigator navigator = navigator("1+2");
        TokenNavigator start = navigator.currentState();
        navigator.next();
        navigator.next();
        navigator.reset(start);
        assertThat(navigator.peek().getToken(), is(Token.number("1")));
    }
    
    private TokenNavigator navigator(String input) {
        return ParserTesting.tokens(input);
    }
}
