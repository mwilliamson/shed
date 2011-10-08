package org.zwobble.shed.compiler.dependencies.errors;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.CompilerErrorDescription;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UndeclaredDependenciesError implements CompilerErrorDescription {
    private final Iterable<String> identifiers;
    
    @Override
    public String describe() {
        return "Unresolved dependencies";
    }
}
