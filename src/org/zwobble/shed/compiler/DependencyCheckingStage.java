package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.dependencies.DependencyChecker;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.TypeResult;

public class DependencyCheckingStage implements CompilerStage {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        SyntaxNode sourceNode = data.get(CompilationDataKeys.sourceNode);
        References references = data.get(CompilationDataKeys.references);
        TypeResult<Void> dependencyCheckResult = new DependencyChecker().check(sourceNode, references);
        return CompilerStageResult.create(dependencyCheckResult.getErrors());
    }

}
