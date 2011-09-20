package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.List;

import lombok.Data;

@Data
public class JavaScriptStatements implements JavaScriptStatementNode {
    private final List<JavaScriptStatementNode> statements;
}
