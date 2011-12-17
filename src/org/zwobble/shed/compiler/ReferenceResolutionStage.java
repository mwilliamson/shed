package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolver;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolverResult;
import org.zwobble.shed.compiler.typechecker.BuiltIns;

public class ReferenceResolutionStage implements CompilerStage {
    private final ReferenceResolver referenceResolver;

    public ReferenceResolutionStage() {
        this.referenceResolver = new ReferenceResolver();
    }
    
    @Override
    public CompilerStageResult execute(CompilationData data) {
        SyntaxNode node = data.get(CompilationDataKeys.sourceNodes);
        BuiltIns builtIns = data.get(CompilationDataKeys.builtIns);
        ReferenceResolverResult referenceResolverResult = referenceResolver.resolveReferences(node, builtIns);
        CompilerStageResult result = CompilerStageResult.create(referenceResolverResult.getErrors());
        result.add(CompilationDataKeys.references, referenceResolverResult.getReferences());
        return result;
    }
}
