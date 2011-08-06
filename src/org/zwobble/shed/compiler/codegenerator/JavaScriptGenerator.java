package org.zwobble.shed.compiler.codegenerator;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

public class JavaScriptGenerator {
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    public JavaScriptNode generate(SyntaxNode node) {
        if (node instanceof BooleanLiteralNode) {
            return js.call(js.id("SHED.shed.lang.Boolean"), js.bool(((BooleanLiteralNode)node).getValue()));
        }
        return null;
    }
}
