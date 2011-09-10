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
    canFindClosingBrace() {
        Tokens tokens = tokens("{a 1 4}");
        TokenStructure structure = analyser.analyse(tokens);
        assertThat(structure.findMatchingClosingSymbolFor(tokens.iterator().next()).getPosition(), is(position(1, 7)));
    }
    
    @Test public void
    canFindMatchingClosingBraceIgnoringSubBlocks() {
        Tokens tokens = tokens("{a 1{a{b{}}c{d}} 4}");
        TokenStructure structure = analyser.analyse(tokens);
        assertThat(structure.findMatchingClosingSymbolFor(tokens.get(0)).getPosition(), is(position(1, 19)));
        assertThat(structure.findMatchingClosingSymbolFor(tokens.get(4)).getPosition(), is(position(1, 16)));
        assertThat(structure.findMatchingClosingSymbolFor(tokens.get(6)).getPosition(), is(position(1, 11)));
    }
    
    @Test public void
    canFindClosingParen() {
        Tokens tokens = tokens("(a 1 4)");
        TokenStructure structure = analyser.analyse(tokens);
        assertThat(structure.findMatchingClosingSymbolFor(tokens.iterator().next()).getPosition(), is(position(1, 7)));
    }
    
    @Test public void
    canFindMatchingClosingParenIgnoringSubBlocksAndParens() {
        Tokens tokens = tokens("(a 1{a(b{})c{d}} 4)");
        TokenStructure structure = analyser.analyse(tokens);
        assertThat(structure.findMatchingClosingSymbolFor(tokens.get(0)).getPosition(), is(position(1, 19)));
    }
    
    @Test public void
    closingBraceClosesAnyOpenParens() {
        Tokens tokens = tokens("{({(})}");
        TokenStructure structure = analyser.analyse(tokens);
        assertThat(structure.findMatchingClosingSymbolFor(tokens.get(1)).getPosition(), is(position(1, 6)));
        assertThat(structure.hasMatchingClosingSymbol(tokens.get(3)), is(false));
    }
    
    @Test public void
    closingParenClosesAnyOpenBraces() {
        Tokens tokens = tokens("({({)})");
        TokenStructure structure = analyser.analyse(tokens);
        assertThat(structure.findMatchingClosingSymbolFor(tokens.get(1)).getPosition(), is(position(1, 6)));
        assertThat(structure.hasMatchingClosingSymbol(tokens.get(3)), is(false));
    }
    
    @Test public void
    semiColonClosesAnyOpenParens() {
        Tokens tokens = tokens("({(;)");
        TokenStructure structure = analyser.analyse(tokens);
        assertThat(structure.hasMatchingClosingSymbol(tokens.get(2)), is(false));
        assertThat(structure.findMatchingClosingSymbolFor(tokens.get(0)).getPosition(), is(position(1, 5)));
    }
    
    @Test public void
    canHandleUnmatchedClosingBrace() {
        Tokens tokens = tokens("}");
        analyser.analyse(tokens);
    }
    
    @Test public void
    canHandleSemiColonWithNoOpenBraces() {
        Tokens tokens = tokens(";");
        analyser.analyse(tokens);
    }
    
    private Tokens tokens(String input) {
        return new Tokeniser().tokenise(input);
    }
}
