package org.zwobble.shed.compiler.typechecker.statements;

import lombok.Data;

@Data
public class StatementTypeCheckResult {
    public static StatementTypeCheckResult noReturn() {
        return new StatementTypeCheckResult(false);
    }

    public static StatementTypeCheckResult alwaysReturns() {
        return new StatementTypeCheckResult(true);
    }
    
    public static StatementTypeCheckResult doesReturn(boolean value) {
        return new StatementTypeCheckResult(value);
    }
    
    private final boolean hasReturned;
}
