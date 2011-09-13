package org.zwobble.shed.compiler.typechecker.errors;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.parsing.SourceRange;

import com.google.common.base.Joiner;

@Data
public class UnresolvedImportError implements CompilerError {
    private final SourceRange location;
    private final List<String> identifiers;

    @Override
    public String getDescription() {
        return "The import \"" + Joiner.on(".").join(identifiers) + "\" cannot be resolved";
    }
}
