package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.codegenerator.JavaScriptWriter;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;

public class JavaScriptWriterStage implements CompilerStage {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        JavaScriptNode node = data.get(CompilationDataKeys.generatedJavaScript);
        String asString = new JavaScriptWriter().write(node);
        CompilerStageResult result = CompilerStageResult.create();
        result.add(CompilationDataKeys.generatedJavaScriptAsString, asString);
        return result;
    }

}
