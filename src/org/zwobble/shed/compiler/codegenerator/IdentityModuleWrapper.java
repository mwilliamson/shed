package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;

public class IdentityModuleWrapper implements JavaScriptModuleWrapper {
    @Override
    public JavaScriptNode wrap(JavaScriptNode module) {
        return module;
    }
}
