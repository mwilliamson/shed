package org.zwobble.shed.compiler.codegenerator;

import java.util.Collections;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatements;

public class BrowserModuleWrapper implements JavaScriptModuleWrapper {
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Override
    public JavaScriptNode wrap(JavaScriptStatements module) {
        return js.statements(
            js.expressionStatement(
                js.call(
                    js.func(Collections.<String>emptyList(), module.getStatements())
                )
            )
        );
    }
}
