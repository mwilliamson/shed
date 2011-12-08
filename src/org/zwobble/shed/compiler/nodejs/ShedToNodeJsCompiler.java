package org.zwobble.shed.compiler.nodejs;

import java.io.File;

import org.zwobble.shed.compiler.CompilationResult;
import org.zwobble.shed.compiler.OptimisationLevel;
import org.zwobble.shed.compiler.ShedCompiler;
import org.zwobble.shed.compiler.codegenerator.BrowserModuleWrapper;
import org.zwobble.shed.compiler.files.DelegatingFileSource;
import org.zwobble.shed.compiler.files.DirectoryFileSource;
import org.zwobble.shed.compiler.files.FileSource;
import org.zwobble.shed.compiler.files.ResourceFileSource;
import org.zwobble.shed.compiler.typechecker.DefaultContextInitialiser;

public class ShedToNodeJsCompiler {
    private static final ShedCompiler compiler = ShedCompiler.build(
        new BrowserModuleWrapper(),
        OptimisationLevel.SIMPLE,
        new NodeJsContextInitialiser(new DefaultContextInitialiser()),
        "node"
    );
    
    public static CompilationResult compile(File sourceDirectory, String target) {
        FileSource fileSource = DelegatingFileSource.create(ResourceFileSource.create(), DirectoryFileSource.create(sourceDirectory));
        return compiler.compile(fileSource);
    }
}
