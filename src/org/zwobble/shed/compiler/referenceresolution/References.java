package org.zwobble.shed.compiler.referenceresolution;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class References {
    private final Map<Identity<VariableIdentifierNode>, Identity<Declaration>> references;
    
    public Declaration findReferent(VariableIdentifierNode reference) {
        Identity<Declaration> identity = references.get(new Identity<VariableIdentifierNode>(reference));
        return identity == null ? null : identity.get();
    }
}
