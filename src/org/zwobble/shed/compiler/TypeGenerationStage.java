package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.typegeneration.TypeGenerator;
import org.zwobble.shed.compiler.typegeneration.TypeStore;

public class TypeGenerationStage implements CompilerStage {
    
    @Override
    public CompilerStageResult execute(CompilationData data) {
        SourceNode sourceNode = data.get(CompilationDataKeys.sourceNode);
        FullyQualifiedNames fullyQualifiedNames = data.get(CompilationDataKeys.fullyQualifiedNames);
        TypeGenerator typeGenerator = new TypeGenerator(fullyQualifiedNames);
        TypeStore generatedTypes = typeGenerator.generateTypes(sourceNode);
        CompilerStageResult result = CompilerStageResult.create();
        result.add(CompilationDataKeys.generatedTypes, generatedTypes);
        return result;
    }

}
