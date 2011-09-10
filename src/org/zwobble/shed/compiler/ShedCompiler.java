package org.zwobble.shed.compiler;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.codegenerator.BrowserModuleWrapper;
import org.zwobble.shed.compiler.codegenerator.JavaScriptGenerator;
import org.zwobble.shed.compiler.codegenerator.JavaScriptModuleWrapper;
import org.zwobble.shed.compiler.codegenerator.JavaScriptWriter;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.ParseResult;
import org.zwobble.shed.compiler.parsing.Parser;
import org.zwobble.shed.compiler.parsing.TokenNavigator;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.tokeniser.TokenPosition;
import org.zwobble.shed.compiler.tokeniser.Tokeniser;
import org.zwobble.shed.compiler.typechecker.CoreModule;
import org.zwobble.shed.compiler.typechecker.TypeChecker;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import static org.zwobble.shed.compiler.typechecker.DefaultBrowserContext.defaultBrowserContext;

public class ShedCompiler {
    public static ShedCompiler forBrowser() {
        return new ShedCompiler(new BrowserModuleWrapper());
    }

    private final Tokeniser tokeniser;
    private final Parser parser;
    private final JavaScriptGenerator javaScriptGenerator;
    private final JavaScriptWriter javaScriptWriter;
    
    private ShedCompiler(JavaScriptModuleWrapper moduleWrapper) {
        this.tokeniser = new Tokeniser();
        this.parser = new Parser();
        this.javaScriptGenerator = new JavaScriptGenerator(moduleWrapper);
        this.javaScriptWriter = new JavaScriptWriter();
    }
    
    public CompilationResult compile(String source) {
        List<TokenPosition> tokens = tokeniser.tokenise(source);
        ParseResult<SourceNode> parseResult = parser.parse(new TokenNavigator(tokens));
        List<CompilerError> errors = new ArrayList<CompilerError>();
        errors.addAll(parseResult.getErrors());
        String javaScriptOutput = null;
        
        if (parseResult.isSuccess()) {
            TypeResult<Void> typeCheckResult = TypeChecker.typeCheck(parseResult.get(), parseResult, defaultBrowserContext());
            errors.addAll(typeCheckResult.getErrors());
            
            if (typeCheckResult.isSuccess()) {
                JavaScriptNode javaScript = javaScriptGenerator.generate(parseResult.get(), CoreModule.VALUES.keySet());
                javaScriptOutput = javaScriptWriter.write(javaScript);
            }
        }
        return new CompilationResult(tokens, errors, javaScriptOutput);
    }
}
