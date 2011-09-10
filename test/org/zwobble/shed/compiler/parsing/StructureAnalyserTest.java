package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.tokeniser.Tokeniser;
import org.zwobble.shed.compiler.tokeniser.Tokens;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.parsing.SourcePosition.position;

public class StructureAnalyserTest {
    private final StructureAnalyser analyser = new StructureAnalyser();
    
    @Test public void
    canFindMatchingClosingBrace() {
        Tokens tokens = tokens("{abc 1 43}");
        TokenStructure structure = analyser.analyse(tokens);
        assertThat(structure.findMatchingClosingBracesFor(tokens.iterator().next()).getPosition(), is(position(1, 10)));
    }
    
    private Tokens tokens(String input) {
        return new Tokeniser().tokenise(input);
    }
}
