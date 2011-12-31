package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.modules.Modules;
import org.zwobble.shed.compiler.parsing.nodes.EntireSourceNode;
import org.zwobble.shed.compiler.sourceordering.SourceOrderer;
import org.zwobble.shed.compiler.typechecker.TypeResult;

public class SourceReorderingStage implements CompilerStage {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        Modules modules = data.get(CompilationDataKeys.modules);
        SourceOrderer sourceOrderer = new SourceOrderer(modules);
        
        EntireSourceNode sourceNodes = data.get(CompilationDataKeys.unorderedSourceNodes);
        TypeResult<EntireSourceNode> reorderResult = sourceOrderer.reorder(sourceNodes);
        
        CompilerStageResult result = CompilerStageResult.create(reorderResult.getErrors());
        result.add(CompilationDataKeys.sourceNodes, reorderResult.getOrThrow());
        return result;
    }

}
