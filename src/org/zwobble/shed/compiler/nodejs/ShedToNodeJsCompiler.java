package org.zwobble.shed.compiler.nodejs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import org.zwobble.shed.compiler.CompilationResult;
import org.zwobble.shed.compiler.OptimisationLevel;
import org.zwobble.shed.compiler.ShedCompiler;
import org.zwobble.shed.compiler.codegenerator.BrowserModuleWrapper;

import com.google.common.io.CharStreams;

import static org.zwobble.shed.compiler.nodejs.DefaultNodeJsContext.defaultNodeJsContext;

public class ShedToNodeJsCompiler {
    public static ShedToNodeJsCompilationResult compile(File sourceDirectory, String target, Writer writer) {
        try {
            compileFiles(sourceDirectory, writer);
            return new ShedToNodeJsCompilationResult();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compileFiles(File file, Writer writer) throws IOException {
        if (file.isDirectory()) {
            compileDirectory(file, writer);
        } else {
            compileFile(file, writer);
        }
    }

    private static void compileDirectory(File directory, Writer writer) throws IOException {
        for (File file : directory.listFiles()) {
            compileFiles(file, writer);
        }
    }

    private static void compileFile(File file, Writer writer) throws IOException {
        if (file.getName().endsWith(".js")) {
            if (!file.getName().endsWith(".browser.js")) {
                writer.write(CharStreams.toString(new FileReader(file)));
            }
        } else if (file.getName().endsWith(".shed")) {
            if (!file.getName().endsWith(".browser.shed")) {
                String source = CharStreams.toString(new FileReader(file));
                CompilationResult result = compiler().compile(source, defaultNodeJsContext());
                if (!result.isSuccess()) {
                    throw new RuntimeException(result.getErrors().toString());
                }
                writer.write(result.getJavaScript());
                writer.write("\n\n");
            }
        }
    }

    private static ShedCompiler compiler() {
        return ShedCompiler.build(new BrowserModuleWrapper(), OptimisationLevel.SIMPLE);
    }
}
