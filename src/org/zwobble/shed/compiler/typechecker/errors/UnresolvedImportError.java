package org.zwobble.shed.compiler.typechecker.errors;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;

import com.google.common.base.Joiner;

@Data
public class UnresolvedImportError implements CompilerErrorDescription {
    private final List<String> identifiers;

    @Override
    public String describe() {
        return "The import \"" + Joiner.on(".").join(identifiers) + "\" cannot be resolved";
    }
}
