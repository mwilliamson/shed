package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptBinaryOperatorNode implements JavaScriptExpressionNode {
    private final String operator;
    private final JavaScriptExpressionNode firstOperand;
    private final JavaScriptExpressionNode secondOperand;
}
