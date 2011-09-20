package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptAssignmentNode implements JavaScriptExpressionNode {
    private final JavaScriptExpressionNode target;
    private final JavaScriptExpressionNode value;
}
