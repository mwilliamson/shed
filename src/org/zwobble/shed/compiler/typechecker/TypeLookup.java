package org.zwobble.shed.compiler.typechecker;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.VariableLookup.lookupVariableReference;

public class TypeLookup {
    public static TypeResult<Type>
    lookupTypeReference(ExpressionNode typeReference, NodeLocations nodeLocations, StaticContext context) {
        // TODO: lookup type from context
        if (typeReference instanceof VariableIdentifierNode) {
            String identifier = ((VariableIdentifierNode)typeReference).getIdentifier();
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
