package org.zwobble.shed.compiler.files;

import java.util.regex.Pattern;

import org.zwobble.shed.compiler.ShedCompiler;
import org.zwobble.shed.compiler.typechecker.StaticContext;

import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;

public class RuntimeImporter {
    private final ShedCompiler compiler;

    public RuntimeImporter(ShedCompiler compiler) {
        this.compiler = compiler;
    }
    
    public void importRuntime(StaticContext context, Iterable<RuntimeFile> files) {
        Iterable<RuntimeFile> nodeSpecificFiles = filter(files, isNodeFile());
        Iterable<RuntimeFile> generalFiles = filter(files, isGeneralFile());
        
        for (RuntimeFile file : concat(nodeSpecificFiles, generalFiles)) {
            compiler.compile(file.readAll(), context);
        }
    }

    private Predicate<RuntimeFile> isNodeFile() {
        return new Predicate<RuntimeFile>() {
            @Override
            public boolean apply(RuntimeFile input) {
                return input.path().endsWith(".node.shed");
            }
        };
    }

    private Predicate<RuntimeFile> isGeneralFile() {
        return new Predicate<RuntimeFile>() {
            @Override
            public boolean apply(RuntimeFile input) {
                return Pattern.compile("^([^.]+)\\.shed$").matcher(input.path()).matches();
            }
        };
    }
}
