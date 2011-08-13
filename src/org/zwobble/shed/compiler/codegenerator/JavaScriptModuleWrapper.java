package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatements;

public interface JavaScriptModuleWrapper {
    JavaScriptNode wrap(JavaScriptStatements module);
}
