package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatements;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;

public interface JavaScriptModuleWrapper {
    JavaScriptNode wrap(PackageDeclarationNode packageDeclaration, Iterable<ImportNode> imports, JavaScriptStatements module, JavaScriptNamer namer);
}
