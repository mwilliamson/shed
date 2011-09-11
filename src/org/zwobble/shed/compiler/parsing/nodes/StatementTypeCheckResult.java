package org.zwobble.shed.compiler.parsing.nodes;

import lombok.Data;

@Data
public class StatementTypeCheckResult {
    public static StatementTypeCheckResult noReturn() {
        return new StatementTypeCheckResult(false);
    }

    public static StatementTypeCheckResult alwaysReturns() {
        return new StatementTypeCheckResult(true);
    }
    
    private final boolean hasReturned;
}
