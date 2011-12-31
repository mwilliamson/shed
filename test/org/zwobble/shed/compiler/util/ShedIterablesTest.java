package org.zwobble.shed.compiler.util;

import org.hamcrest.Matchers;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.zwobble.shed.compiler.util.Pair.pair;
import static org.zwobble.shed.compiler.util.Triple.triple;

@SuppressWarnings("unchecked")
public class ShedIterablesTest {
    @Test public void
    zippingTogetherTwoEmptyIterablesResultsInEmptyIterable() {
        Iterable<Pair<Object, Object>> zipped = ShedIterables.zip(emptyList(), emptyList());
        assertThat(zipped, Matchers.<Pair<Object, Object>>emptyIterable());
    }
    
    @Test public void
    canZipTogetherTwoIterablesOfEqualLength() {
        Iterable<Pair<String, Boolean>> zipped = ShedIterables.zip(asList("True", "False"), asList(true, false));
        assertThat(zipped, contains(pair("True", true), pair("False", false)));
    }
    
    @Test public void
    returnedPairIterableIsTruncatedToLengthOfFirstIterableIfFirstIterableIsShorter() {
        Iterable<Pair<String, Boolean>> zipped = ShedIterables.zip(asList("True"), asList(true, false));
        assertThat(zipped, contains(pair("True", true)));
    }
    
    @Test public void
    returnedPairIterableIsTruncatedToLengthOfSecondIterableIfSecondIterableIsShorter() {
        Iterable<Pair<String, Boolean>> zipped = ShedIterables.zip(asList("True", "False"), asList(true));
        assertThat(zipped, contains(pair("True", true)));
    }
    @Test public void
    zippingTogetherThreeEmptyIterablesResultsInEmptyIterable() {
        Iterable<Triple<Object, Object, Object>> zipped = ShedIterables.zip(emptyList(), emptyList(), emptyList());
        assertThat(zipped, Matchers.<Triple<Object, Object, Object>>emptyIterable());
    }
    
    @Test public void
    canZipTogetherThreeIterablesOfEqualLength() {
        Iterable<Triple<String, Boolean, Integer>> zipped = ShedIterables.zip(asList("True", "False"), asList(true, false), asList(1, 0));
        assertThat(zipped, contains(triple("True", true, 1), triple("False", false, 0)));
    }
    
    @Test public void
    returnedTripleIterableIsTruncatedToLengthOfFirstIterableIfFirstIterableIsShorter() {
        Iterable<Triple<String, Boolean, Integer>> zipped = ShedIterables.zip(asList("True"), asList(true, false), asList(1, 0));
        assertThat(zipped, contains(triple("True", true, 1)));
    }
    
    @Test public void
    returnedTripleIterableIsTruncatedToLengthOfSecondIterableIfSecondIterableIsShorter() {
        Iterable<Triple<String, Boolean, Integer>> zipped = ShedIterables.zip(asList("True", "False"), asList(true), asList(1, 0));
        assertThat(zipped, contains(triple("True", true, 1)));
    }
    
    @Test public void
    returnedTripleIterableIsTruncatedToLengthOfThirdIterableIfThirdIterableIsShorter() {
        Iterable<Triple<String, Boolean, Integer>> zipped = ShedIterables.zip(asList("True", "False"), asList(true, false), asList(1));
        assertThat(zipped, contains(triple("True", true, 1)));
    }
}
