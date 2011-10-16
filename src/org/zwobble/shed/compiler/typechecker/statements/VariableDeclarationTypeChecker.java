package org.zwobble.shed.compiler.typechecker.statements;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.SubTyping.isSubType;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class VariableDeclarationTypeChecker implements DeclarationTypeChecker<VariableDeclarationNode> {
    private final TypeInferer typeInferer;
    private final TypeLookup typeLookup;
    private final NodeLocations nodeLocations;

    @Inject
    public VariableDeclarationTypeChecker(TypeInferer typeInferer, TypeLookup typeLookup, NodeLocations nodeLocations) {
        this.typeInferer = typeInferer;
        this.typeLookup = typeLookup;
        this.nodeLocations = nodeLocations;
    }

    @Override
    public TypeResult<?> forwardDeclare(VariableDeclarationNode variableDeclaration, StaticContext context) {
        if (isForwardDeclarable(variableDeclaration)) {
            TypeResult<Type> typeResult = typeLookup.lookupTypeReference(variableDeclaration.getTypeReference().get(), context);
            if (typeResult.hasValue()) {
                Type type = typeResult.get();
                ValueInfo valueInfo = toValueInfo(variableDeclaration, type);
                context.add(variableDeclaration, valueInfo);
            }
            return typeResult;
        } else {
            context.add(variableDeclaration, ValueInfo.unknown());
            return TypeResult.success();
        }
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        VariableDeclarationNode variableDeclaration, StaticContext staticContext, Option<Type> returnType
    ) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        TypeResult<Type> valueTypeResult = typeInferer.inferType(variableDeclaration.getValue(), staticContext);
        errors.addAll(valueTypeResult.getErrors());
        
        if (isForwardDeclarable(variableDeclaration)) {
            VariableLookupResult variableLookupResult = staticContext.get(variableDeclaration);
            if (variableLookupResult.getStatus() == Status.SUCCESS) {
                Type specifiedType = variableLookupResult.getType();
                if (valueTypeResult.hasValue() && !isSubType(valueTypeResult.get(), specifiedType, staticContext)) {
                    errors.add(CompilerError.error(
                        nodeLocations.locate(variableDeclaration.getValue()),
                        "Cannot initialise variable of type \"" + specifiedType.shortName() +
                        "\" with expression of type \"" + valueTypeResult.get().shortName() + "\""
                    ));
                }
            }
        } else {
            Type type = valueTypeResult.get();
            ValueInfo valueInfo = toValueInfo(variableDeclaration, type);
            staticContext.add(variableDeclaration, valueInfo);
        }
        
        if (errors.isEmpty()) {
            return success(StatementTypeCheckResult.noReturn());
        } else {
            return failure(errors);
        }
    }

    private ValueInfo toValueInfo(VariableDeclarationNode variableDeclaration, Type type) {
        return variableDeclaration.isMutable()
            ? ValueInfo.assignableValue(type)
            : unassignableValue(type);
    }

    private boolean isForwardDeclarable(VariableDeclarationNode variableDeclaration) {
        return variableDeclaration.getTypeReference().hasValue();
    }
}
