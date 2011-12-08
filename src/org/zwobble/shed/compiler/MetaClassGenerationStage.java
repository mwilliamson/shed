package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;

public class MetaClassGenerationStage implements CompilerStage {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        CompilerStageResult result = CompilerStageResult.create();
        result.add(CompilationDataKeys.metaClasses, MetaClasses.create());
        return result;
    }
}
