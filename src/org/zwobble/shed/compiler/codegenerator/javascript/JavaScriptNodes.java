package org.zwobble.shed.compiler.codegenerator.javascript;

import static java.util.Arrays.asList;

public class JavaScriptNodes {
    public JavaScriptBooleanLiteralNode bool(boolean value) {
        return new JavaScriptBooleanLiteralNode(value);
    }
    
    public JavaScriptFunctionCallNode call(JavaScriptNode function, JavaScriptNode... arguments) {
        return new JavaScriptFunctionCallNode(function, asList(arguments));
    }
    
    public JavaScriptIdentifierNode id(String identifier) {
        return new JavaScriptIdentifierNode(identifier);
    }
}
