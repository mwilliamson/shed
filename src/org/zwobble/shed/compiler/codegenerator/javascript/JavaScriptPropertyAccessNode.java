package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptPropertyAccessNode implements JavaScriptExpressionNode {
    private final JavaScriptExpressionNode expression;
    private final String propertyName;
}
