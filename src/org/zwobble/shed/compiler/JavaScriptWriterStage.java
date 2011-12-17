package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.codegenerator.JavaScriptWriter;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;

public class JavaScriptWriterStage implements CompilerStage {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        Iterable<JavaScriptNode> nodes = data.get(CompilationDataKeys.generatedJavaScript);
        StringBuilder asString = new StringBuilder();
        JavaScriptWriter javaScriptWriter = new JavaScriptWriter();
        
        for (JavaScriptNode node : nodes) {
            asString.append(javaScriptWriter.write(node));
            asString.append("\n");
        }
        
        CompilerStageResult result = CompilerStageResult.create();
        result.add(CompilationDataKeys.generatedJavaScriptAsString, asString.toString());
        return result;
    }

}
