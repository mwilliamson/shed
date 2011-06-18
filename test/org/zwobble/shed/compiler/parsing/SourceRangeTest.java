package org.zwobble.shed.compiler.parsing;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SourceRangeTest {
    @Test public void
    equalSourceRangesContainEachOther() {
        assertThat(range(pos(1, 2), pos(3, 4)).contains(range(pos(1, 2), pos(3, 4))), is(true));
    }
    
    @Test public void
    completelyDistinctRangesDontIncludeEachOther() {
        assertThat(range(pos(1, 2), pos(3, 4)).contains(range(pos(5, 6), pos(7, 8))), is(false));
    }
    
    @Test public void
    rangesDontIncludeEachIfTheyOverlap() {
        assertThat(range(pos(1, 2), pos(3, 4)).contains(range(pos(2, 6), pos(7, 8))), is(false));
        assertThat(range(pos(2, 6), pos(7, 8)).contains(range(pos(1, 2), pos(3, 4))), is(false));
    }
    
    @Test public void
    rangeContainsAnotherIfItIsEntirelyContained() {
        assertThat(range(pos(1, 2), pos(3, 4)).contains(range(pos(1, 3), pos(3, 3))), is(true));
    }
    
    private SourceRange range(SourcePosition start, SourcePosition end) {
        return new SourceRange(start, end);
    }
    
    private SourcePosition pos(int lineNumber, int characterNumber) {
        return new SourcePosition(lineNumber, characterNumber);
    }
}
