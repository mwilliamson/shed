package org.zwobble.shed.compiler.referenceresolution;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class References {
    private final Map<Identity<VariableIdentifierNode>, Identity<DeclarationNode>> references;
    
    public DeclarationNode findReferent(VariableIdentifierNode reference) {
        return references.get(new Identity<VariableIdentifierNode>(reference)).get();
    }
}
