package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.codegenerator.JavaScriptGenerator;
import org.zwobble.shed.compiler.codegenerator.JavaScriptModuleWrapper;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.BuiltIns;
import org.zwobble.shed.compiler.util.Eager;

import com.google.common.base.Function;

public class JavaScriptGenerationStage implements CompilerStage {
    private final JavaScriptModuleWrapper moduleWrapper;

    public JavaScriptGenerationStage(JavaScriptModuleWrapper moduleWrapper) {
        this.moduleWrapper = moduleWrapper;
    }

    @Override
    public CompilerStageResult execute(CompilationData data) {
        References references = data.get(CompilationDataKeys.references);
        JavaScriptGenerator generator = new JavaScriptGenerator(moduleWrapper, references);
        BuiltIns builtIns = data.get(CompilationDataKeys.builtIns);
        Iterable<SourceNode> nodes = data.get(CompilationDataKeys.sourceNodes);
        CompilerStageResult result = CompilerStageResult.create();
        Iterable<JavaScriptNode> generatedJavaScript = Eager.transform(nodes, generate(generator, builtIns));
        result.add(CompilationDataKeys.generatedJavaScript, generatedJavaScript);
        return result;
    }

    private Function<SourceNode, JavaScriptNode> generate(final JavaScriptGenerator generator, final BuiltIns builtIns) {
        return new Function<SourceNode, JavaScriptNode>() {
            @Override
            public JavaScriptNode apply(SourceNode node) {
                return generator.generate(node, builtIns.allNames());
            }
        };
    }

}
