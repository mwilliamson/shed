package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;

public interface JavaScriptModuleWrapper {
    JavaScriptNode wrap(JavaScriptNode module);
}
