package org.zwobble.shed.compiler.typechecker.errors;

import org.zwobble.shed.compiler.CompilerErrorDescription;

public class MissingReturnStatementError implements CompilerErrorDescription {
    @Override
    public String describe() {
        return "Expected return statement";
    }
}
