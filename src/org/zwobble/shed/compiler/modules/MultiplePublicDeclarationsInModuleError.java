package org.zwobble.shed.compiler.modules;

import lombok.Data;

import org.zwobble.shed.compiler.errors.CompilerErrorDescription;

@Data
public class MultiplePublicDeclarationsInModuleError implements CompilerErrorDescription {
    @Override
    public String describe() {
        return "A module may have no more than one public value";
    }
}
