package org.zwobble.shed.compiler.parsing;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class SourcePositionTest {
    @Test public void
    equalSourcePositionsCompareEqual() {
        assertThat(new SourcePosition(4, 9).compareTo(new SourcePosition(4, 9)), is(0));
    }
    
    @Test public void
    sourcePositionOnSameLineAreComparedUsingCharacterNumber() {
        assertThat(new SourcePosition(4, 9).compareTo(new SourcePosition(4, 8)), is(greaterThan(0)));
        assertThat(new SourcePosition(4, 8).compareTo(new SourcePosition(4, 9)), is(lessThan(0)));
    }
    
    @Test public void
    sourcePositionOnDifferentLinesAreComparedUsingLineNumber() {
        assertThat(new SourcePosition(4, 8).compareTo(new SourcePosition(3, 9)), is(greaterThan(0)));
        assertThat(new SourcePosition(4, 9).compareTo(new SourcePosition(5, 8)), is(lessThan(0)));
    }
}
