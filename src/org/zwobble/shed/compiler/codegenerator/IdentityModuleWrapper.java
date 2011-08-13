package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatements;

public class IdentityModuleWrapper implements JavaScriptModuleWrapper {
    @Override
    public JavaScriptNode wrap(JavaScriptStatements module) {
        return module;
    }
}
