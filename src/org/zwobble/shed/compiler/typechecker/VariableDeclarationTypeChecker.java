package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeErrors.duplicateIdentifierError;
import static org.zwobble.shed.compiler.typechecker.TypeInferer.inferType;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class VariableDeclarationTypeChecker {
    public static TypeResult<Void>
    typeCheckImmutableVariableDeclaration(ImmutableVariableNode immutableVariable, NodeLocations nodeLocations, StaticContext staticContext) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        TypeResult<Type> valueTypeResult = inferType(immutableVariable.getValue(), nodeLocations, staticContext);
        errors.addAll(valueTypeResult.getErrors());
        
        if (immutableVariable.getTypeReference().hasValue()) {
            TypeReferenceNode typeReference = immutableVariable.getTypeReference().get();
            TypeResult<Type> typeResult = lookupTypeReference(typeReference, nodeLocations, staticContext);
            errors.addAll(typeResult.getErrors());
            if (errors.isEmpty() && !valueTypeResult.get().equals(typeResult.get())) {
                errors.add(new CompilerError(
                    nodeLocations.locate(immutableVariable.getValue()),
                    "Cannot initialise variable of type \"" + typeResult.get().shortName() +
                        "\" with expression of type \"" + valueTypeResult.get().shortName() + "\""
                ));
            }
        }
        
        if (staticContext.isDeclaredInCurrentScope(immutableVariable.getIdentifier())) {
            errors.add(duplicateIdentifierError(immutableVariable.getIdentifier(), nodeLocations.locate(immutableVariable)));
        }
        
        if (errors.isEmpty()) {
            staticContext.add(immutableVariable.getIdentifier(), valueTypeResult.get());
            return success(null);
        } else {
            return failure(errors);
        }
    }
}
