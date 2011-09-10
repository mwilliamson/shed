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
    canGetPositionOfEndOfLastToken() {
        TokenNavigator navigator = navigator("1 + 2");
        navigator.next();
        assertThat(navigator.lastPosition(), is(position(1, 2)));
        navigator.next();
        assertThat(navigator.lastPosition(), is(position(1, 4)));
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
    
    @Test public void
    seekingToEndOfStatementSeeksToNextSemiColon() {
        TokenNavigator navigator = navigator("1+2;3;");
        navigator.next();
        navigator.seekToEndOfStatement();
        assertThat(navigator.peek().getStartPosition(), is(position(1, 5)));
    }
    
    @Test public void
    seekingToEndOfStatementWhenThereAreNoMoreSemiColonsSeeksToEndOfIterator() {
        TokenNavigator navigator = navigator("1+2");
        navigator.seekToEndOfStatement();
        assertThat(navigator.peek().getToken(), is(Token.end()));
    }
    
    @Test public void
    seekingToEndOfStatementSeeksToJustBeforeEndOfBlockIfNoSemiColonsAreFoundBeforehand() {
        TokenNavigator navigator = navigator("{1+2}3;");
        navigator.next();
        navigator.seekToEndOfStatement();
        assertThat(navigator.peek().getStartPosition(), is(position(1, 5)));
    }
    
    @Test public void
    seekingToEndOfStatementSeeksToJustBeforeEndOfCurrentBlockIfNoSemiColonsAreFoundBeforehand() {
        TokenNavigator navigator = navigator("{1+2{ }}3;");
        navigator.next();
        navigator.seekToEndOfStatement();
        assertThat(navigator.peek().getStartPosition(), is(position(1, 8)));
    }
    
    @Test public void
    seekingToEndOfBlockSeeksToEndOfCurrentBlock() {
        TokenNavigator navigator = navigator("{1+2{ }}3;");
        navigator.next();
        navigator.seekToEndOfBlock();
        assertThat(navigator.peek().getStartPosition(), is(position(1, 9)));
    }

    @Test public void
    seekingToEndOfBlockSeeksToEndIfCurrentBlockIsNotClosed() {
        TokenNavigator navigator = navigator("{1+2{ }3;");
        navigator.next();
        navigator.seekToEndOfBlock();
        assertThat(navigator.peek().getToken(), is(Token.end()));
    }
    
    private TokenNavigator navigator(String input) {
        return ParserTesting.tokens(input);
    }
}
