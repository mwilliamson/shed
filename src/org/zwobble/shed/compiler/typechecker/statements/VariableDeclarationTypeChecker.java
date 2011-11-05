package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResultBuilder;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.typechecker.errors.TypeMismatchError;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.typechecker.SubTyping.isSubType;
import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class VariableDeclarationTypeChecker implements DeclarationTypeChecker<VariableDeclarationNode> {
    private final TypeInferer typeInferer;
    private final TypeLookup typeLookup;
    private final StaticContext context;

    @Inject
    public VariableDeclarationTypeChecker(TypeInferer typeInferer, TypeLookup typeLookup, StaticContext context) {
        this.typeInferer = typeInferer;
        this.typeLookup = typeLookup;
        this.context = context;
    }

    @Override
    public TypeResult<?> forwardDeclare(VariableDeclarationNode variableDeclaration) {
        if (isForwardDeclarable(variableDeclaration)) {
            TypeResult<Type> typeResult = typeLookup.lookupTypeReference(variableDeclaration.getTypeReference().get());
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
    public TypeResult<StatementTypeCheckResult> typeCheck(VariableDeclarationNode variableDeclaration, Option<Type> returnType) {
        TypeResultBuilder<StatementTypeCheckResult> typeResult = typeResultBuilder(StatementTypeCheckResult.noReturn());
        
        TypeResult<Type> valueTypeResult = typeInferer.inferType(variableDeclaration.getValue());
        typeResult.addErrors(valueTypeResult);
        if (isForwardDeclarable(variableDeclaration)) {
            VariableLookupResult variableLookupResult = context.get(variableDeclaration);
            if (variableLookupResult.getStatus() == Status.SUCCESS) {
                Type specifiedType = variableLookupResult.getType();
                if (valueTypeResult.hasValue() && !isSubType(valueTypeResult.get(), specifiedType, context)) {
                    typeResult.addError(error(
                        variableDeclaration.getValue(),
                        new TypeMismatchError(specifiedType, valueTypeResult.get())
                    ));
                }
            }
        } else if (valueTypeResult.hasValue()) {
            Type type = valueTypeResult.get();
            ValueInfo valueInfo = toValueInfo(variableDeclaration, type);
            context.add(variableDeclaration, valueInfo);
        }
        
        return typeResult.build();
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
