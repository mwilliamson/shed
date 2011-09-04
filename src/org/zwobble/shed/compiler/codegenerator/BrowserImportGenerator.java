package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;

import com.google.common.base.Joiner;

public class BrowserImportGenerator implements JavaScriptImportGenerator {
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Override
    public JavaScriptNode generateExpression(PackageDeclarationNode packageDeclaration, ImportNode importNode) {
        return js.call(js.id("SHED.require"), js.string(Joiner.on(".").join(importNode.getNames())));
    }
}
