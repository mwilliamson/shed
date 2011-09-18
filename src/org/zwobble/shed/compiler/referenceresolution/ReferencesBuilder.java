package org.zwobble.shed.compiler.referenceresolution;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

import com.google.common.collect.ImmutableMap;

public class ReferencesBuilder {
    private final ImmutableMap.Builder<Identity<VariableIdentifierNode>, Identity<DeclarationNode>> builder = ImmutableMap.builder();
    
    public void addReference(VariableIdentifierNode reference, DeclarationNode declaration) {
        builder.put(new Identity<VariableIdentifierNode>(reference), new Identity<DeclarationNode>(declaration));
    }

    public References build() {
        return new References(builder.build());
    }
}
