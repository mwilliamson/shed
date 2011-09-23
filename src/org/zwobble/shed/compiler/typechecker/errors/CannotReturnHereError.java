package org.zwobble.shed.compiler.typechecker.errors;

import org.zwobble.shed.compiler.CompilerErrorDescription;

public class CannotReturnHereError implements CompilerErrorDescription {
    @Override
    public String describe() {
        return "Cannot return from here";
    }
}
