package org.zwobble.shed.compiler.codegenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.referenceresolution.References;

public class JavaScriptNamer {
    private final References references;
    private final Map<Identity<Declaration>, String> shedToJavaScriptNames = new HashMap<Identity<Declaration>, String>();
    private final Set<String> usedNames = new HashSet<String>();

    public JavaScriptNamer(References references) {
        this.references = references;
    }
    
    public String javaScriptIdentifierFor(Declaration declaration) {
        Identity<Declaration> identity = new Identity<Declaration>(declaration);
        if (!shedToJavaScriptNames.containsKey(identity)) {
            shedToJavaScriptNames.put(identity, freshJavaScriptIdentifierFor(declaration));
        }
        return shedToJavaScriptNames.get(identity);
    }

    public String javaScriptIdentifierFor(VariableIdentifierNode node) {
        return javaScriptIdentifierFor(references.findReferent(node));
    }

    public String freshJavaScriptIdentifierFor(Declaration declaration) {
        if (declaration instanceof GlobalDeclaration) {
            usedNames.add(declaration.getIdentifier());
            return declaration.getIdentifier();
        }
        String identifier = freshJavaScriptIdentifier(declaration.getIdentifier());
        shedToJavaScriptNames.put(new Identity<Declaration>(declaration), identifier);
        return identifier;
    }

    public String freshJavaScriptIdentifier(String base) {
        int index = 1;
        String identifier;
        do {
            identifier = base + "__" + index;
            index += 1;
        } while (usedNames.contains(identifier));
        usedNames.add(identifier);
        return identifier;
    }
}
