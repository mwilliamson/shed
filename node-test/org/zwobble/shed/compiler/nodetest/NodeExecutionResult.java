package org.zwobble.shed.compiler.nodetest;

import lombok.Data;

@Data
public class NodeExecutionResult {
    private final int returnCode;
    private final String output;
    private final String errorOutput;
}
