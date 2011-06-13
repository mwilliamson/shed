package org.zwobble.shed.compiler.parsing;

import org.zwobble.shed.compiler.tokeniser.Tokeniser;

public class ParserTesting {
    public static TokenIterator tokens(String input) {
        return new TokenIterator(new Tokeniser().tokenise(input));
    }
}
