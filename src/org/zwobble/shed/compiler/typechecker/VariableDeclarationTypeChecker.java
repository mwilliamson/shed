package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.SubTyping.isSubType;

import static org.zwobble.shed.compiler.typechecker.TypeErrors.duplicateIdentifierError;
import static org.zwobble.shed.compiler.typechecker.TypeInferer.inferType;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class VariableDeclarationTypeChecker {
    public static TypeResult<Void>
    typeCheckVariableDeclaration(VariableDeclarationNode variableDeclaration, NodeLocations nodeLocations, StaticContext staticContext) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        TypeResult<Type> valueTypeResult = inferType(variableDeclaration.getValue(), nodeLocations, staticContext);
        errors.addAll(valueTypeResult.getErrors());
        
        if (variableDeclaration.getTypeReference().hasValue()) {
            TypeReferenceNode typeReference = variableDeclaration.getTypeReference().get();
            TypeResult<Type> typeResult = lookupTypeReference(typeReference, nodeLocations, staticContext);
            errors.addAll(typeResult.getErrors());
            if (errors.isEmpty() && !isSubType(valueTypeResult.get(), typeResult.get())) {
                errors.add(new CompilerError(
                    nodeLocations.locate(variableDeclaration.getValue()),
                    "Cannot initialise variable of type \"" + typeResult.get().shortName() +
                        "\" with expression of type \"" + valueTypeResult.get().shortName() + "\""
                ));
            }
        }
        
        if (staticContext.isDeclaredInCurrentScope(variableDeclaration.getIdentifier())) {
            errors.add(duplicateIdentifierError(variableDeclaration.getIdentifier(), nodeLocations.locate(variableDeclaration)));
        }
        
        if (errors.isEmpty()) {
            staticContext.add(variableDeclaration.getIdentifier(), valueTypeResult.get());
            return success(null);
        } else {
            return failure(errors);
        }
    }
}
