package org.zwobble.shed.compiler.nodejs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import org.zwobble.shed.compiler.CompilationResult;
import org.zwobble.shed.compiler.OptimisationLevel;
import org.zwobble.shed.compiler.ShedCompiler;
import org.zwobble.shed.compiler.codegenerator.BrowserModuleWrapper;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;

import com.google.common.io.CharStreams;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

public class ShedToNodeJsCompiler {
    public static ShedToNodeJsCompilationResult compile(File sourceDirectory, String target, Writer writer) {
        try {
            compileFiles(sourceDirectory, writer);
            writer.flush();
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
        System.out.println("Compiling " + file.getAbsolutePath());
        if (file.getName().endsWith(".js")) {
            if (!file.getName().endsWith(".browser.js")) {
                System.out.println("* Copying " + file.getAbsolutePath());
                writer.write(CharStreams.toString(new FileReader(file)));
            }
        } else if (file.getName().endsWith(".shed")) {
            if (!file.getName().endsWith(".browser.shed")) {
                System.out.println("* Compiling " + file.getAbsolutePath());
                String source = CharStreams.toString(new FileReader(file));
                CompilationResult result = compiler().compile(source, context());
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
    
    private static StaticContext context() {
        StaticContext context = StaticContext.defaultContext();

        ClassType sysType = new ClassType(fullyQualifiedName("shed", "sys"));
        ScalarTypeInfo sysTypeInfo = new ScalarTypeInfo(
            interfaces(),
            members("print", unassignableValue(CoreTypes.functionTypeOf(CoreTypes.STRING, CoreTypes.UNIT)))
        );
        
        context.addGlobal(asList("shed", "sys"), sysType);
        context.addInfo(sysType, sysTypeInfo);
        return context;
    }
}
