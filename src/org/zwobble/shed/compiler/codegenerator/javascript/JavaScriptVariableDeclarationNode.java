package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptVariableDeclarationNode implements JavaScriptNode {
    private final String name;
    private final JavaScriptNode initialValue;
}
