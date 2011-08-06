package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.Data;

@Data
public class JavaScriptStringLiteralNode implements JavaScriptNode {
    private final String value;
}
