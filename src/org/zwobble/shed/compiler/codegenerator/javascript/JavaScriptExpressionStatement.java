package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptExpressionStatement implements JavaScriptStatementNode {
    private final JavaScriptExpressionNode expression;
}
