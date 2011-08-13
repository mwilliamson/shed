package org.zwobble.shed.compiler.codegenerator.javascript;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class JavaScriptBooleanLiteralNode implements JavaScriptExpressionNode {
    private final boolean value;
    
    public boolean getValue() {
        return value;
    }
}
