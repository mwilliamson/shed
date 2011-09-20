package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.List;

import lombok.Data;

@Data
public class JavaScriptWhileNode implements JavaScriptStatementNode {
    private final JavaScriptExpressionNode condition;
    private final List<JavaScriptStatementNode> body;
}
