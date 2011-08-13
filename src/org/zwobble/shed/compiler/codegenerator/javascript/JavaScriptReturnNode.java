package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptReturnNode implements JavaScriptStatementNode {
    private final JavaScriptNode value;
}
