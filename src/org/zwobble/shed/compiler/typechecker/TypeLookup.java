package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.VariableLookup.lookupVariableReference;

public class TypeLookup {
    private final NodeLocations nodeLocations;

    public TypeLookup(NodeLocations nodeLocations) {
        this.nodeLocations = nodeLocations;
    }
    
    public TypeResult<Type> lookupTypeReference(TypeReferenceNode typeReference, StaticContext context) {
        if (typeReference instanceof TypeIdentifierNode) {
            String identifier = ((TypeIdentifierNode)typeReference).getIdentifier();
            SourceRange nodeLocation = nodeLocations.locate(typeReference);
            TypeResult<Type> variableType = lookupVariableReference(identifier, nodeLocation, context);
            
            if (!variableType.hasValue()) {
                return variableType;
            }
            
            if (!(variableType.get() instanceof TypeApplication) || 
                    !((TypeApplication)variableType.get()).getTypeFunction().equals(CoreTypes.CLASS)) {
                return failure(asList(new CompilerError(
                    nodeLocation,
                    "\"" + identifier + "\" is not a type but an instance of \"" + variableType.get().shortName() + "\""
                )));
            }
            
            return success(((TypeApplication)variableType.get()).getTypeParameters().get(0));
        }
        throw new RuntimeException("Cannot look up type reference: " + typeReference);
    }
}
