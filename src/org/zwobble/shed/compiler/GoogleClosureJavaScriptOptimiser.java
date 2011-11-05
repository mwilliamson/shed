package org.zwobble.shed.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;

import static java.util.Arrays.asList;

public class GoogleClosureJavaScriptOptimiser implements JavaScriptOptimiser {
    @Override
    public String optimise(String javaScript) {
        Compiler compiler = new Compiler();
        List<JSSourceFile> externs = Collections.emptyList();
        List<JSSourceFile> inputs = asList(stringToSourceFile(javaScript));
        compiler.compile(externs, inputs, options());
        return compiler.toSource();
    }
    
    private JSSourceFile stringToSourceFile(String javaScript) {
        try {
            return JSSourceFile.fromInputStream("dummy.js", new ByteArrayInputStream(javaScript.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CompilerOptions options() {
        CompilerOptions options = new CompilerOptions();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        options.prettyPrint = true;
        return options;
    }
}
