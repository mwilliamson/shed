package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.List;

import lombok.Data;

@Data
public class JavaScriptFunctionNode implements JavaScriptNode {
    private final List<JavaScriptNode> statements;
}
