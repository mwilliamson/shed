package org.zwobble.shed.compiler.types;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.SourcePosition;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.types.VariableLookup.lookupVariableReference;

public class TypeLookup {
    public static TypeResult lookupTypeReference(TypeReferenceNode typeReference, StaticContext context) {
        if (typeReference instanceof TypeIdentifierNode) {
            String identifier = ((TypeIdentifierNode)typeReference).getIdentifier();
            TypeResult variableType = lookupVariableReference(identifier, context);
            
            if (!variableType.hasValue()) {
                return variableType;
            }
            
            if (!(variableType.get() instanceof TypeApplication) || 
                    !((TypeApplication)variableType.get()).getTypeFunction().equals(CoreTypes.CLASS)) {
                return failure(asList(new CompilerError(
                    new SourcePosition(-1, -1),
                    new SourcePosition(-1, -1),
                    "\"" + identifier + "\" is not a type but an instance of \"" + variableType.get().shortName() + "\""
                )));
            }
            
            return success(((TypeApplication)variableType.get()).getTypeParameters().get(0));
        }
        throw new RuntimeException("Cannot look up type reference: " + typeReference);
    }
}
