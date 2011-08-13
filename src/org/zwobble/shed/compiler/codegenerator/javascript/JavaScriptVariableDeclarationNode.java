package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptVariableDeclarationNode implements JavaScriptStatementNode {
    private final String name;
    private final JavaScriptNode initialValue;
}
