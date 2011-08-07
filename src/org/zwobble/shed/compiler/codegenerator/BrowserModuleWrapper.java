package org.zwobble.shed.compiler.codegenerator;

import java.util.Collections;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;

import static java.util.Arrays.asList;

public class BrowserModuleWrapper implements JavaScriptModuleWrapper {
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Override
    public JavaScriptNode wrap(JavaScriptNode module) {
        return js.call(js.func(Collections.<String>emptyList(), asList(module)));
    }
}
