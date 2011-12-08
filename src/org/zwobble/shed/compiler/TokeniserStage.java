package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.tokeniser.TokenisedSource;
import org.zwobble.shed.compiler.tokeniser.Tokeniser;

public class TokeniserStage implements CompilerStage {
    private final Tokeniser tokeniser;

    public TokeniserStage() {
        this.tokeniser = new Tokeniser();
    }
    
    @Override
    public CompilerStageResult execute(CompilationData data) {
        TokenisedSource tokens = tokeniser.tokenise(data.get(CompilationDataKeys.sourceString));
        CompilerStageResult result = CompilerStageResult.create();
        result.add(CompilationDataKeys.tokenisedSource, tokens);
        return result;
    }
}
