package org.zwobble.shed.compiler.tokeniser;

import java.util.Iterator;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="create")
public class TokenisedSource implements Iterable<TokenPosition> {
    private final List<TokenPosition> tokens;
    
    @Override
    public Iterator<TokenPosition> iterator() {
        return tokens.iterator();
    }
}
