package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Joiner;

import static org.zwobble.shed.compiler.typechecker.TypeInferer.inferType;
import static org.zwobble.shed.compiler.typechecker.TypeLookup.lookupTypeReference;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class TypeChecker {
    public static TypeResult<Void> typeCheck(SourceNode source, NodeLocations nodeLocations, StaticContext staticContext) {
        staticContext.enterNewScope();
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        for (ImportNode importNode : source.getImports()) {
            List<String> identifiers = importNode.getNames();
            String identifier = identifiers.get(identifiers.size() - 1);
            Option<Type> importedValueType = staticContext.lookupGlobal(identifiers);
            if (importedValueType.hasValue()) {
                if (staticContext.isDeclaredInCurrentScope(identifier)) {
                    errors.add(duplicateIdentifierError(identifier, nodeLocations.locate(importNode)));
                } else {
                    staticContext.add(identifier, importedValueType.get());                    
                }
            } else {
                errors.add(new CompilerError(
                    nodeLocations.locate(importNode),
                    "The import \"" + Joiner.on(".").join(identifiers) + "\" cannot be resolved"
                ));
            }
        }
        
        for (StatementNode statement : source.getStatements()) {
            TypeResult<Void> result = typeCheckStatement(statement, nodeLocations, staticContext);
            errors.addAll(result.getErrors());
        }
        if (errors.isEmpty()) {
            return success(null);
        } else {
            return failure(errors);
        }
    }

    public static TypeResult<Void> typeCheckStatement(StatementNode statement, NodeLocations nodeLocations, StaticContext staticContext) {
        if (statement instanceof ImmutableVariableNode) {
            return typeCheckImmutableVariableDeclaration(statement, nodeLocations, staticContext);
        }
        if (statement instanceof ReturnNode) {
            return TypeResult.<Void>success(null);
        }
        throw new RuntimeException("Cannot check type of statement: " + statement);
    }

    private static TypeResult<Void>
    typeCheckImmutableVariableDeclaration(StatementNode statement, NodeLocations nodeLocations, StaticContext staticContext) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        ImmutableVariableNode immutableVariable = (ImmutableVariableNode)statement;
        
        TypeResult<Type> valueTypeResult = inferType(immutableVariable.getValue(), nodeLocations, staticContext);
        errors.addAll(valueTypeResult.getErrors());
        
        if (immutableVariable.getTypeReference().hasValue()) {
            TypeReferenceNode typeReference = immutableVariable.getTypeReference().get();
            TypeResult<Type> typeResult = lookupTypeReference(typeReference, nodeLocations, staticContext);
            errors.addAll(typeResult.getErrors());
            if (errors.isEmpty() && !valueTypeResult.get().equals(typeResult.get())) {
                errors.add(new CompilerError(
                    nodeLocations.locate(statement),
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

    private static CompilerError duplicateIdentifierError(String identifier, SourceRange nodeLocation) {
        return new CompilerError(
            nodeLocation,
            "The variable \"" + identifier + "\" has already been declared in this scope"
        );
    }
}
