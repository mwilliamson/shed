package org.zwobble.shed.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.zwobble.shed.compiler.codegenerator.BrowserModuleWrapper;
import org.zwobble.shed.compiler.codegenerator.JavaScriptGenerator;
import org.zwobble.shed.compiler.codegenerator.JavaScriptModuleWrapper;
import org.zwobble.shed.compiler.codegenerator.JavaScriptWriter;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.dependencies.DependencyChecker;
import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.files.FileSource;
import org.zwobble.shed.compiler.files.RuntimeFile;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.naming.TypeNamer;
import org.zwobble.shed.compiler.parsing.ParseResult;
import org.zwobble.shed.compiler.parsing.Parser;
import org.zwobble.shed.compiler.parsing.TokenNavigator;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolver;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolverResult;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.Tokeniser;
import org.zwobble.shed.compiler.typechecker.BrowserContextInitialiser;
import org.zwobble.shed.compiler.typechecker.DefaultContextInitialiser;
import org.zwobble.shed.compiler.typechecker.SourceTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.StaticContextInitialiser;
import org.zwobble.shed.compiler.typechecker.TypeCheckerInjector;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typegeneration.TypeGenerator;
import org.zwobble.shed.compiler.typegeneration.TypeStore;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

import static org.zwobble.shed.compiler.Results.isSuccess;

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

    private final Tokeniser tokeniser;
    private final Parser parser;
    private final ReferenceResolver referenceResolver;
    private final JavaScriptWriter javaScriptWriter;
    private final JavaScriptOptimiser javaScriptOptimiser;
    private final JavaScriptModuleWrapper moduleWrapper;
    private final StaticContextInitialiser staticContextInitialiser;
    private final String platformSlug;
    
    private ShedCompiler(JavaScriptModuleWrapper moduleWrapper, JavaScriptOptimiser javaScriptOptimiser, 
            StaticContextInitialiser staticContextInitialiser, String platformSlug) {
        this.moduleWrapper = moduleWrapper;
        this.tokeniser = new Tokeniser();
        this.parser = new Parser();
        this.referenceResolver = new ReferenceResolver();
        this.javaScriptWriter = new JavaScriptWriter();
        this.javaScriptOptimiser = javaScriptOptimiser;
        this.staticContextInitialiser = staticContextInitialiser;
        this.platformSlug = platformSlug;
    }
    
    public CompilationResult compile(FileSource fileSource) {
        MetaClasses metaClasses = MetaClasses.create();
        StaticContext context = new StaticContext(metaClasses);
        staticContextInitialiser.initialise(context, metaClasses);
        
        StringBuilder output = new StringBuilder();
        List<SourceFileCompilationResult> results = Lists.newArrayList();
        for (RuntimeFile file : fileSource) {
            if (isShedFile(file)) {
                SourceFileCompilationResult result = compile(file.readAll(), context, metaClasses);
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

    private SourceFileCompilationResult compile(String source, StaticContext context, MetaClasses metaClasses) {
        List<TokenPosition> tokens = tokeniser.tokenise(source);
        ParseResult<SourceNode> parseResult = parser.parse(new TokenNavigator(tokens));
        List<CompilerError> errors = new ArrayList<CompilerError>();
        errors.addAll(parseResult.getErrors());
        String javaScriptOutput = null;
        
        if (parseResult.isSuccess()) {
            SourceNode sourceNode = parseResult.get();
            ReferenceResolverResult referencesResult = referenceResolver.resolveReferences(sourceNode, context.getBuiltIns());
            errors.addAll(referencesResult.getErrors());
            if (referencesResult.isSuccess()) {
                FullyQualifiedNames fullNames = new TypeNamer().generateFullyQualifiedNames(sourceNode);
                TypeStore types = new TypeGenerator(fullNames).generateTypes(sourceNode);
                References references = referencesResult.getReferences();
                Injector typeCheckerInjector = TypeCheckerInjector.build(types, metaClasses, context, references);
                SourceTypeChecker sourceTypeChecker = typeCheckerInjector.getInstance(SourceTypeChecker.class);
                TypeResult<Void> typeCheckResult = sourceTypeChecker.typeCheck(sourceNode);
                errors.addAll(typeCheckResult.getErrors());
                
                TypeResult<Void> dependencyCheckResult = new DependencyChecker().check(sourceNode, references);
                errors.addAll(dependencyCheckResult.getErrors());
                
                if (isSuccess(typeCheckResult)) {
                    JavaScriptNode javaScript = javaScriptGenerator(references).generate(sourceNode, context.getBuiltIns().keySet());
                    javaScriptOutput = javaScriptWriter.write(javaScript);
                }   
            }
        }
        return new SourceFileCompilationResult(tokens, parseResult, errors, javaScriptOutput);
    }

    private JavaScriptGenerator javaScriptGenerator(References references) {
        return new JavaScriptGenerator(moduleWrapper, references);
    }
}
