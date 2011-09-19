package org.zwobble.shed.compiler.typechecker.errors;

import lombok.EqualsAndHashCode;

import lombok.ToString;

import lombok.AllArgsConstructor;

import org.zwobble.shed.compiler.CompilerErrorDescription;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UntypedReferenceError implements CompilerErrorDescription {
    private final String identifier;
    
    @Override
    public String describe() {
        return "Could not determine type of reference: " + identifier;
    }
}
