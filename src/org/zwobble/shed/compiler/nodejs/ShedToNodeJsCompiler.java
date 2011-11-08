package org.zwobble.shed.compiler.nodejs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import org.zwobble.shed.compiler.CompilationResult;
import org.zwobble.shed.compiler.OptimisationLevel;
import org.zwobble.shed.compiler.ShedCompiler;
import org.zwobble.shed.compiler.codegenerator.BrowserModuleWrapper;
import org.zwobble.shed.compiler.files.ResourceRuntimeFileReader;
import org.zwobble.shed.compiler.files.RuntimeImporter;
import org.zwobble.shed.compiler.typechecker.StaticContext;

import com.google.common.io.CharStreams;

import static org.zwobble.shed.compiler.nodejs.DefaultNodeJsContext.defaultNodeJsContext;

public class ShedToNodeJsCompiler {
    private static final ShedCompiler compiler = ShedCompiler.build(new BrowserModuleWrapper(), OptimisationLevel.SIMPLE);
    
    public static ShedToNodeJsCompilationResult compile(File sourceDirectory, String target, Writer writer) {
        StaticContext context = defaultNodeJsContext();
        try {
            RuntimeImporter runtimeImporter = new RuntimeImporter(compiler);
            runtimeImporter.importRuntime(context, ResourceRuntimeFileReader.build().listFiles());
            compileFiles(sourceDirectory, writer, context);
            return new ShedToNodeJsCompilationResult();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compileFiles(File file, Writer writer, StaticContext context) throws IOException {
        if (file.isDirectory()) {
            compileDirectory(file, writer, context);
        } else {
            compileFile(file, writer, context);
        }
    }

    private static void compileDirectory(File directory, Writer writer, StaticContext context) throws IOException {
        for (File file : directory.listFiles()) {
            compileFiles(file, writer, context);
        }
    }

    private static void compileFile(File file, Writer writer, StaticContext context) throws IOException {
        if (file.getName().endsWith(".js")) {
            if (!file.getName().endsWith(".browser.js")) {
                writer.write(CharStreams.toString(new FileReader(file)));
            }
        } else if (file.getName().endsWith(".shed")) {
            if (!file.getName().endsWith(".browser.shed")) {
                String source = CharStreams.toString(new FileReader(file));
                CompilationResult result = compiler.compile(source, context);
                if (!result.isSuccess()) {
                    throw new RuntimeException(result.getErrors().toString());
                }
                writer.write(result.getJavaScript());
                writer.write("\n\n");
            }
        }
    }
}
