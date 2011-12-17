package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.tokeniser.TokenisedSource;
import org.zwobble.shed.compiler.tokeniser.Tokeniser;
import org.zwobble.shed.compiler.util.Eager;

import com.google.common.base.Function;

public class TokeniserStage implements CompilerStage {
    private final Tokeniser tokeniser;

    public TokeniserStage() {
        this.tokeniser = new Tokeniser();
    }
    
    @Override
    public CompilerStageResult execute(CompilationData data) {
        Iterable<String> sourceStrings = data.get(CompilationDataKeys.sourceStrings);
        Iterable<TokenisedSource> tokenisedSources = Eager.transform(sourceStrings, tokenise());
        CompilerStageResult result = CompilerStageResult.create();
        result.add(CompilationDataKeys.tokenisedSources, tokenisedSources);
        return result;
    }

    private Function<String, TokenisedSource> tokenise() {
        return new Function<String, TokenisedSource>() {
            @Override
            public TokenisedSource apply(String input) {
                return tokeniser.tokenise(input);
            }
        };
    }
}
