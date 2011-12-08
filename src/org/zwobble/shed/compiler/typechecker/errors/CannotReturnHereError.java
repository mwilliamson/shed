package org.zwobble.shed.compiler.typechecker.errors;

import lombok.Data;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;

@Data
public class CannotReturnHereError implements CompilerErrorDescription {
    @Override
    public String describe() {
        return "Cannot return from here";
    }
}
