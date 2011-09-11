package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.List;

import lombok.Data;

@Data
public class JavaScriptIfThenElseNode implements JavaScriptStatementNode {
    private final JavaScriptExpressionNode condition;
    private final List<JavaScriptStatementNode> ifTrue;
    private final List<JavaScriptStatementNode> ifFalse;
}
