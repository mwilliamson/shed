package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;

public interface JavaScriptImportGenerator {
    JavaScriptNode generateExpression(PackageDeclarationNode packageDeclaration, ImportNode importNode);
}
