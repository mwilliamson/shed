package org.zwobble.shed.compiler;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.errors.HasErrors;

@RequiredArgsConstructor(staticName="create")
@Getter
public class CompilerStageResult implements HasErrors {
    public static CompilerStageResult create() {
        return create(Collections.<CompilerError>emptyList());
    }
    
    private final List<CompilerError> errors;
    private final CompilationData data = new CompilationData();
    
    public <T> void add(CompilationDataKey<T> key, T value) {
        data.add(key, value);
    }
}
