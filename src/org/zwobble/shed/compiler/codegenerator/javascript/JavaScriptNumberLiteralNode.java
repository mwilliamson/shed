package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptNumberLiteralNode implements JavaScriptExpressionNode {
    private final String value;
}
