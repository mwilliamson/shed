package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.List;

import lombok.Data;

@Data
public class JavaScriptFunctionCallNode implements JavaScriptExpressionNode {
    private final JavaScriptExpressionNode function;
    private final List<JavaScriptExpressionNode> arguments;
}
