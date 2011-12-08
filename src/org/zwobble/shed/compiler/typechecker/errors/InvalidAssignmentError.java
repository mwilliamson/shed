package org.zwobble.shed.compiler.typechecker.errors;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;

import lombok.Data;

@Data
public class InvalidAssignmentError implements CompilerErrorDescription {
    @Override
    public String describe() {
        return "Invalid assignment target";
    }
}
