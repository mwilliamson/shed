package org.zwobble.shed.compiler.referenceresolution;

import java.util.List;

import lombok.Data;

import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.errors.HasErrors;

@Data(staticConstructor="build")
public class ReferenceResolverResult implements HasErrors{
    private final References references;
    private final List<CompilerError> errors;
    
    public boolean isSuccess() {
        return errors.isEmpty();
    }
}
