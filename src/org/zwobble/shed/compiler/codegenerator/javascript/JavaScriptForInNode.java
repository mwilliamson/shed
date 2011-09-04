package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.List;

import lombok.Data;

@Data
public class JavaScriptForInNode implements JavaScriptStatementNode {
    private final String name;
    private final JavaScriptExpressionNode object;
    private final List<JavaScriptStatementNode> statements;
}
