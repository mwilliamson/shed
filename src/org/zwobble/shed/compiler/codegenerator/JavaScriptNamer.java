package org.zwobble.shed.compiler.codegenerator;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.References;

public class JavaScriptNamer {
    private final References references;
    private final Map<Identity<DeclarationNode>, String> shedToJavaScriptNames = new HashMap<Identity<DeclarationNode>, String>();

    public JavaScriptNamer(References references) {
        this.references = references;
    }
    
    public String javaScriptIdentifierFor(DeclarationNode node) {
        Identity<DeclarationNode> identity = new Identity<DeclarationNode>(node);
        if (!shedToJavaScriptNames.containsKey(identity)) {
            shedToJavaScriptNames.put(identity, freshJavaScriptIdentifierFor(node));
        }
        return shedToJavaScriptNames.get(identity);
    }

    public String javaScriptIdentifierFor(VariableIdentifierNode node) {
        return javaScriptIdentifierFor(references.findReferent(node));
    }

    public String freshJavaScriptIdentifierFor(DeclarationNode declaration) {
        if (declaration instanceof GlobalDeclarationNode) {
            return declaration.getIdentifier();
        }
        int index = 1;
        String identifier;
        do {
            identifier = declaration.getIdentifier() + "__" + index;
            index += 1;
        } while (shedToJavaScriptNames.containsValue(identifier));
        shedToJavaScriptNames.put(new Identity<DeclarationNode>(declaration), identifier);
        return identifier;
    }
}
