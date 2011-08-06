package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptReturnNode implements JavaScriptNode {
    private final JavaScriptNode value;
}
