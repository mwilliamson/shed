package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.List;

import lombok.Data;

@Data
public class JavaScriptFunctionNode implements JavaScriptExpressionNode {
    private final List<String> arguments;
    private final List<JavaScriptStatementNode> statements;
}
