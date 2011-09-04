package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatements;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;

public class IdentityModuleWrapper implements JavaScriptModuleWrapper {
    @Override
    public JavaScriptNode wrap(PackageDeclarationNode packageDeclaration, Iterable<ImportNode> imports, JavaScriptStatements module) {
        return module;
    }
}
