package org.zwobble.shed.compiler;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class CompilationResult {
    private final Iterable<SourceFileCompilationResult> results;
    private final String output;

    public CompilationResult(Iterable<SourceFileCompilationResult> results, String output) {
        this.results = results;
        this.output = output;
    }
    
    public boolean isSuccess() {
        for (SourceFileCompilationResult result : results) {
            if (!result.isSuccess()) {
                return false;
            }
        }
        return true;
    }
    
    public Iterable<CompilerError> errors() {
        return concat(transform(results, toErrors()));
    }
    
    public String output() {
        return output;
    }

    private Function<SourceFileCompilationResult, Iterable<CompilerError>> toErrors() {
        return new Function<SourceFileCompilationResult, Iterable<CompilerError>>() {
            @Override
            public Iterable<CompilerError> apply(SourceFileCompilationResult input) {
                return input.getErrors();
            }
        };
    }
}
