package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.List;

import static java.util.Arrays.asList;

public class JavaScriptNodes {
    public JavaScriptBooleanLiteralNode bool(boolean value) {
        return new JavaScriptBooleanLiteralNode(value);
    }
    
    public JavaScriptNumberLiteralNode number(String value) {
        return new JavaScriptNumberLiteralNode(value);
    }
    
    public JavaScriptFunctionCallNode call(JavaScriptNode function, JavaScriptNode... arguments) {
        return new JavaScriptFunctionCallNode(function, asList(arguments));
    }
    
    public JavaScriptIdentifierNode id(String identifier) {
        return new JavaScriptIdentifierNode(identifier);
    }

    public JavaScriptVariableDeclarationNode var(String name, JavaScriptNode initialValue) {
        return new JavaScriptVariableDeclarationNode(name, initialValue);
    }
    
    public JavaScriptFunctionNode func(List<String> arguments, List<JavaScriptNode> statements) {
        return new JavaScriptFunctionNode(arguments, statements);
    }
    
    public JavaScriptReturnNode ret(JavaScriptNode value) {
        return new JavaScriptReturnNode(value);
    }

    public JavaScriptStringLiteralNode string(String value) {
        return new JavaScriptStringLiteralNode(value);
    }
}
