package org.zwobble.shed.compiler;

public interface CompilerStage {
    CompilerStageResult execute(CompilationData data);
}
