package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.Map;

import lombok.Data;

@Data
public class JavaScriptObjectLiteralNode implements JavaScriptExpressionNode {
    private final Map<String, JavaScriptExpressionNode> properties;
}
