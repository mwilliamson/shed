package org.zwobble.shed.compiler.sourceordering;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;

import com.google.common.base.Joiner;

@EqualsAndHashCode
@ToString
@AllArgsConstructor(staticName="create")
public class CircularModuleDependency implements CompilerErrorDescription {
    private final List<FullyQualifiedName> moduleNames;
    
    @Override
    public String describe() {
        return "Circular module dependency: " + Joiner.on(" -> ").join(moduleNames);
    }
    
}
