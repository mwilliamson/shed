package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.codegenerator.JavaScriptGenerator;
import org.zwobble.shed.compiler.codegenerator.JavaScriptModuleWrapper;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.StaticContext;

public class JavaScriptGenerationStage implements CompilerStage {
    private final JavaScriptModuleWrapper moduleWrapper;

    public JavaScriptGenerationStage(JavaScriptModuleWrapper moduleWrapper) {
        this.moduleWrapper = moduleWrapper;
    }

    @Override
    public CompilerStageResult execute(CompilationData data) {
        References references = data.get(CompilationDataKeys.references);
        JavaScriptGenerator generator = new JavaScriptGenerator(moduleWrapper, references);
        SourceNode node = data.get(CompilationDataKeys.sourceNode);
        StaticContext context = data.get(CompilationDataKeys.staticContext);
        JavaScriptNode generatedJavaScript = generator.generate(node, context.getBuiltIns().keySet());
        CompilerStageResult result = CompilerStageResult.create();
        result.add(CompilationDataKeys.generatedJavaScript, generatedJavaScript);
        return result;
    }

}
