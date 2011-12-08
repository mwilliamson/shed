package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.naming.TypeNamer;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

public class NameGenerationStage implements CompilerStage {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        SyntaxNode node = data.get(CompilationDataKeys.sourceNode);
        FullyQualifiedNames fullyQualifiedNames = new TypeNamer().generateFullyQualifiedNames(node);
        CompilerStageResult result = CompilerStageResult.create();
        result.add(CompilationDataKeys.fullyQualifiedNames, fullyQualifiedNames);
        return result;
    }

}
