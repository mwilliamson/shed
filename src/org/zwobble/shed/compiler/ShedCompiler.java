package org.zwobble.shed.compiler;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.zwobble.shed.compiler.codegenerator.BrowserModuleWrapper;
import org.zwobble.shed.compiler.codegenerator.JavaScriptModuleWrapper;
import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.files.FileSource;
import org.zwobble.shed.compiler.files.RuntimeFile;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.typechecker.BrowserContextInitialiser;
import org.zwobble.shed.compiler.typechecker.BuiltIns;
import org.zwobble.shed.compiler.typechecker.DefaultContextInitialiser;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.StaticContextInitialiser;

import com.google.common.collect.Lists;

import static java.util.Arrays.asList;

public class ShedCompiler {
    public static ShedCompiler forBrowser(OptimisationLevel optimisationLevel) {
        return new ShedCompiler(
            new BrowserModuleWrapper(),
            optimiserFor(optimisationLevel),
            new BrowserContextInitialiser(new DefaultContextInitialiser()),
            "browser"
        );
    }
    
    public static ShedCompiler build(
        JavaScriptModuleWrapper moduleWrapper,
        OptimisationLevel optimisationLevel,
        StaticContextInitialiser staticContextInitialiser,
        String platformSlug
    ) {
        return new ShedCompiler(moduleWrapper, optimiserFor(optimisationLevel), staticContextInitialiser, platformSlug);
    }

    private static JavaScriptOptimiser optimiserFor(OptimisationLevel optimisationLevel) {
        if (optimisationLevel == OptimisationLevel.SIMPLE) {
            return new GoogleClosureJavaScriptOptimiser();
        }
        return new NoOpJavaScriptOptimiser();
    }

    private final JavaScriptOptimiser javaScriptOptimiser;
    private final JavaScriptModuleWrapper moduleWrapper;
    private final StaticContextInitialiser staticContextInitialiser;
    private final String platformSlug;
    
    private ShedCompiler(JavaScriptModuleWrapper moduleWrapper, JavaScriptOptimiser javaScriptOptimiser, 
            StaticContextInitialiser staticContextInitialiser, String platformSlug) {
        this.moduleWrapper = moduleWrapper;
        this.javaScriptOptimiser = javaScriptOptimiser;
        this.staticContextInitialiser = staticContextInitialiser;
        this.platformSlug = platformSlug;
    }
    
    public CompilationResult compile(FileSource fileSource) {
        MetaClasses metaClasses = MetaClasses.create();
        BuiltIns builtIns = new BuiltIns();
        StaticContext context = new StaticContext(metaClasses);
        staticContextInitialiser.initialise(context, builtIns, metaClasses);
        
        StringBuilder output = new StringBuilder();
        List<SourceFileCompilationResult> results = Lists.newArrayList();
        for (RuntimeFile file : fileSource) {
            if (isShedFile(file)) {
                SourceFileCompilationResult result = compile(file.readAll(), context, builtIns, metaClasses);
                results.add(result);
                output.append(result.getJavaScript());
            } else if (isJavaScriptFile(file)) {
                output.append(file.readAll());
            }
            output.append("\n\n");
        }
        String optimisedJavaScript = javaScriptOptimiser.optimise(output.toString());
        return new CompilationResult(results, optimisedJavaScript);
    }

    private boolean isShedFile(RuntimeFile file) {
        return isGeneralShedFile(file) || isNodeSpecificShedFile(file);
    }

    private boolean isGeneralShedFile(RuntimeFile file) {
        return Pattern.compile("^([^.]+)\\.shed$").matcher(file.path()).matches();
    }

    private boolean isNodeSpecificShedFile(RuntimeFile file) {
        return file.path().endsWith("." + platformSlug + ".shed");
    }
    
    private boolean isJavaScriptFile(RuntimeFile file) {
        return isGeneralJavaScriptFile(file) || isNodeSpecificJavaScriptFile(file);
    }

    private boolean isGeneralJavaScriptFile(RuntimeFile file) {
        return Pattern.compile("^([^.]+)\\.js$").matcher(file.path()).matches();
    }

    private boolean isNodeSpecificJavaScriptFile(RuntimeFile file) {
        return file.path().endsWith("." + platformSlug + ".js");
    }

    private SourceFileCompilationResult compile(String source, StaticContext context, BuiltIns builtIns, MetaClasses metaClasses) {
        CompilationData compilationData = new CompilationData();
        compilationData.add(CompilationDataKeys.sourceString, source);
        compilationData.add(CompilationDataKeys.metaClasses, metaClasses);
        compilationData.add(CompilationDataKeys.staticContext, context);
        compilationData.add(CompilationDataKeys.builtIns, builtIns);
        
        List<CompilerError> errors = executeStages(compilationData);
        
        Iterable<TokenPosition> tokens = compilationData.get(CompilationDataKeys.tokenisedSource);
        NodeLocations nodeLocations = compilationData.get(CompilationDataKeys.nodeLocations);
        String javaScriptOutput = compilationData.get(CompilationDataKeys.generatedJavaScriptAsString);
        return new SourceFileCompilationResult(tokens, nodeLocations, errors, javaScriptOutput);
    }

    private List<CompilerError> executeStages(CompilationData compilationData) {
        for (CompilerStage stage : stages()) {
            CompilerStageResult result = stage.execute(compilationData);
            compilationData.addAll(result.getData());
            if (!result.getErrors().isEmpty()) {
                return result.getErrors();
            }
        }
        return Collections.emptyList();
    }

    private Iterable<CompilerStage> stages() {
        return asList(
            new TokeniserStage(),
            new ParserStage(),
            new ReferenceResolutionStage(),
            new NameGenerationStage(),
            new TypeGenerationStage(),
            new TypeCheckingStage(),
            new DependencyCheckingStage(),
            new JavaScriptGenerationStage(moduleWrapper),
            new JavaScriptWriterStage()
        );
    }
}
