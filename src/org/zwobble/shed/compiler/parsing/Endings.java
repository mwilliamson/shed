package org.zwobble.shed.compiler.parsing;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.tokeniser.Token;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class Endings {
    private final List<Ending> endings = new ArrayList<Ending>();
    
    public void add(TokenPosition tokenPosition, int scopeDepth) {
        endings.add(new Ending(tokenPosition, scopeDepth));
    }

    public Iterable<Ending> endsOfStatements() {
        return endings;
    }

    public Iterable<Ending> endsOfBlocks() {
        return Iterables.filter(endings, isEndOfBlock());
    }

    private Predicate<Ending> isEndOfBlock() {
        return new Predicate<Ending>() {
            @Override
            public boolean apply(Ending input) {
                Token token = input.getTokenPosition().getToken();
                return token.equals(StructureAnalyser.CLOSING_BRACE) || token.equals(Token.end());
            }
        };
    }
}
