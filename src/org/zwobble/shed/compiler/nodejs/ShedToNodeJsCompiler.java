package org.zwobble.shed.compiler.nodejs;

import org.zwobble.shed.compiler.OptimisationLevel;
import org.zwobble.shed.compiler.ShedCompiler;

public class ShedToNodeJsCompiler {
    public static ShedToNodeJsCompilationResult compile(String sourceDirectory, String targetDirectory) {
        return ShedCompiler.build(moduleWrapper, OptimisationLevel.SIMPLE).;
    }
}
