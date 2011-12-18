package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.modules.ModuleGenerator;
import org.zwobble.shed.compiler.modules.Modules;
import org.zwobble.shed.compiler.parsing.nodes.EntireSourceNode;
import org.zwobble.shed.compiler.typechecker.TypeResultWithValue;

public class ModuleGenerationStage implements CompilerStage {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        ModuleGenerator moduleGenerator = new ModuleGenerator();
        EntireSourceNode sourceNodes = data.get(CompilationDataKeys.sourceNodes);
        TypeResultWithValue<Modules> modules = moduleGenerator.generateModules(sourceNodes);
        CompilerStageResult result = CompilerStageResult.create(modules.getErrors());
        result.add(CompilationDataKeys.modules, modules.get());
        return result;
    }
}
