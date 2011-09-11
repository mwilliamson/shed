package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class StatementTypeCheckResult {
    private final boolean hasReturned;
}
