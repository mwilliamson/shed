package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.List;

import lombok.Data;

@Data
public class JavaScriptFunctionCallNode implements JavaScriptNode {
    private final JavaScriptNode function;
    private final List<JavaScriptNode> arguments;
}
