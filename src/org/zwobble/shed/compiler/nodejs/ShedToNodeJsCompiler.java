package org.zwobble.shed.compiler.nodejs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import org.zwobble.shed.compiler.CompilationResult;
import org.zwobble.shed.compiler.OptimisationLevel;
import org.zwobble.shed.compiler.ShedCompiler;
import org.zwobble.shed.compiler.SourceFileCompilationResult;
import org.zwobble.shed.compiler.codegenerator.BrowserModuleWrapper;
import org.zwobble.shed.compiler.files.DelegatingFileSource;
import org.zwobble.shed.compiler.files.DirectoryFileSource;
import org.zwobble.shed.compiler.files.FileSource;
import org.zwobble.shed.compiler.files.ResourceFileSource;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.typechecker.StaticContext;

import com.google.common.io.CharStreams;

public class ShedToNodeJsCompiler {
    private static final ShedCompiler compiler = ShedCompiler.build(new BrowserModuleWrapper(), OptimisationLevel.SIMPLE);
    
    public static CompilationResult compile(File sourceDirectory, String target) {
        MetaClasses metaClasses = MetaClasses.create();
        StaticContext context = new StaticContext(metaClasses);
        DefaultNodeJsContext.defaultNodeJsContext(context, metaClasses);
        FileSource fileSource = DelegatingFileSource.create(ResourceFileSource.create(), DirectoryFileSource.create(sourceDirectory));
        return compiler.compile(fileSource, context, metaClasses);
    }

    private static void compileFiles(File file, Writer writer, StaticContext context, MetaClasses metaClasses) throws IOException {
        if (file.isDirectory()) {
            compileDirectory(file, writer, context, metaClasses);
        } else {
            compileFile(file, writer, context, metaClasses);
        }
    }

    private static void compileDirectory(File directory, Writer writer, StaticContext context, MetaClasses metaClasses) throws IOException {
        for (File file : directory.listFiles()) {
            compileFiles(file, writer, context, metaClasses);
        }
    }

    private static void compileFile(File file, Writer writer, StaticContext context, MetaClasses metaClasses) throws IOException {
        if (file.getName().endsWith(".js")) {
            if (!file.getName().endsWith(".browser.js")) {
                writer.write(CharStreams.toString(new FileReader(file)));
            }
        } else if (file.getName().endsWith(".shed")) {
            if (!file.getName().endsWith(".browser.shed")) {
                String source = CharStreams.toString(new FileReader(file));
                SourceFileCompilationResult result = compiler.compile(source, context, metaClasses);
                if (!result.isSuccess()) {
                    throw new RuntimeException(result.getErrors().toString());
                }
                writer.write(result.getJavaScript());
                writer.write("\n\n");
            }
        }
    }
}
