package org.zwobble.shed.compiler;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.codegenerator.BrowserModuleWrapper;
import org.zwobble.shed.compiler.codegenerator.JavaScriptGenerator;
import org.zwobble.shed.compiler.codegenerator.JavaScriptModuleWrapper;
import org.zwobble.shed.compiler.codegenerator.JavaScriptWriter;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.dependencies.DependencyChecker;
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
import org.zwobble.shed.compiler.typechecker.SourceTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerInjector;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.inject.Injector;

public class ShedCompiler {
    public static ShedCompiler forBrowser(OptimisationLevel optimisationLevel) {
        return new ShedCompiler(new BrowserModuleWrapper(), optimiserFor(optimisationLevel));
    }
    
    public static ShedCompiler build(JavaScriptModuleWrapper moduleWrapper, OptimisationLevel optimisationLevel) {
        return new ShedCompiler(moduleWrapper, optimiserFor(optimisationLevel));
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
    
    private ShedCompiler(JavaScriptModuleWrapper moduleWrapper, JavaScriptOptimiser javaScriptOptimiser) {
        this.moduleWrapper = moduleWrapper;
        this.tokeniser = new Tokeniser();
        this.parser = new Parser();
        this.referenceResolver = new ReferenceResolver();
        this.javaScriptWriter = new JavaScriptWriter();
        this.javaScriptOptimiser = javaScriptOptimiser;
    }
    
    public CompilationResult compile(String source, StaticContext context) {
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
                References references = referencesResult.getReferences();
                Injector typeCheckerInjector = TypeCheckerInjector.build(fullNames, context, references);
                SourceTypeChecker sourceTypeChecker = typeCheckerInjector.getInstance(SourceTypeChecker.class);
                TypeResult<Void> typeCheckResult = sourceTypeChecker.typeCheck(sourceNode);
                errors.addAll(typeCheckResult.getErrors());
                
                TypeResult<Void> dependencyCheckResult = new DependencyChecker().check(sourceNode, references);
                errors.addAll(dependencyCheckResult.getErrors());
                
                if (typeCheckResult.isSuccess()) {
                    JavaScriptNode javaScript = javaScriptGenerator(references).generate(sourceNode, context.getBuiltIns().keySet());
                    javaScriptOutput = javaScriptOptimiser.optimise(javaScriptWriter.write(javaScript));
                }   
            }
        }
        return new CompilationResult(tokens, parseResult, errors, javaScriptOutput);
    }

    private JavaScriptGenerator javaScriptGenerator(References references) {
        return new JavaScriptGenerator(moduleWrapper, references);
    }
}
