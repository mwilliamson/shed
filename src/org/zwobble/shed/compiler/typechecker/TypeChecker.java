package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Joiner;

import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class TypeChecker {
    private final NodeLocations nodeLocations;
    private final TypeLookup typeLookup;
    private final TypeInferer typeInferer;

    public TypeChecker(NodeLocations nodeLocations, TypeLookup typeLookup, TypeInferer typeInferer) {
        this.nodeLocations = nodeLocations;
        this.typeLookup = typeLookup;
        this.typeInferer = typeInferer;
    }

    public TypeResult<Void> typeCheck(SourceNode source, StaticContext staticContext) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        for (ImportNode importNode : source.getImports()) {
            List<String> identifiers = importNode.getNames();
            String identifier = identifiers.get(identifiers.size() - 1);
            Option<Type> importedValueType = staticContext.lookupGlobal(identifiers);
            if (importedValueType.hasValue()) {
                staticContext.add(identifier, importedValueType.get());
            } else {
                errors.add(new CompilerError(
                    nodeLocations.locate(importNode),
                    "The import \"" + Joiner.on(".").join(identifiers) + "\" cannot be resolved"
                ));
            }
        }
        
        for (StatementNode statement : source.getStatements()) {
            TypeResult<Void> result = typeCheckStatement(statement, staticContext);
            errors.addAll(result.getErrors());
        }
        if (errors.isEmpty()) {
            return success(null);
        } else {
            return failure(errors);
        }
    }

    private TypeResult<Void> typeCheckStatement(StatementNode statement, StaticContext staticContext) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        if (statement instanceof ImmutableVariableNode) {
            ImmutableVariableNode immutableVariable = (ImmutableVariableNode)statement;
            
            TypeResult<Type> valueTypeResult = typeInferer.inferType(immutableVariable.getValue(), staticContext);
            errors.addAll(valueTypeResult.getErrors());
            
            if (immutableVariable.getTypeReference().hasValue()) {
                TypeResult<Type> typeResult = typeLookup.lookupTypeReference(immutableVariable.getTypeReference().get(), staticContext);
                errors.addAll(typeResult.getErrors());
                if (errors.isEmpty() && !valueTypeResult.get().equals(typeResult.get())) {
                    errors.add(new CompilerError(
                        nodeLocations.locate(statement),
                        "Cannot initialise variable of type \"" + typeResult.get().shortName() +
                            "\" with expression of type \"" + valueTypeResult.get().shortName() + "\""
                    ));
                }
            }
            
            if (errors.isEmpty()) {
                return success(null);
            } else {
                return failure(errors);
            }
        }
        throw new RuntimeException("Cannot check type of statement: " + statement);
    }
}
