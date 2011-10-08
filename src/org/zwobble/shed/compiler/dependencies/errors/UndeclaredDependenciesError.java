package org.zwobble.shed.compiler.dependencies.errors;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.CompilerErrorDescription;

import com.google.common.base.Joiner;

import static com.google.common.collect.Lists.reverse;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UndeclaredDependenciesError implements CompilerErrorDescription {
    private final List<String> identifiers;
    
    @Override
    public String describe() {
        return "Reference to undeclared variable " + Joiner.on(", which is required to declare ").join(reverse(identifiers));
    }
}
