package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeInferer;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public class ClassDeclarationTypeChecker implements HoistableStatementTypeChecker<ClassDeclarationNode> {
    private final TypeInferer typeInferer;
    private final TypeLookup typeLookup;
    private final FullyQualifiedNames fullyQualifiedNames;

    @Inject
    public ClassDeclarationTypeChecker(TypeInferer typeInferer, TypeLookup typeLookup, FullyQualifiedNames fullyQualifiedNames) {
        this.typeInferer = typeInferer;
        this.typeLookup = typeLookup;
        this.fullyQualifiedNames = fullyQualifiedNames;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(ClassDeclarationNode statement, StaticContext context) {
        FullyQualifiedName name = fullyQualifiedNames.fullyQualifiedNameOf(statement);
        Set<InterfaceType> interfaces = Collections.<InterfaceType>emptySet();
        Map<String, ValueInfo> members = buildMembers(statement, context);
        ClassType type = new ClassType(name, interfaces, members);
        context.add(statement, ValueInfo.unassignableValue(type));
        return TypeResult.success();
    }

    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        ClassDeclarationNode statement, StaticContext context, Option<Type> returnType
    ) {
        return null;
    }

    private Map<String, ValueInfo> buildMembers(ClassDeclarationNode classDeclaration, StaticContext context) {
        ImmutableMap.Builder<String, ValueInfo> members = ImmutableMap.builder();
        Iterable<PublicDeclarationNode> publicDeclarations = Iterables.filter(classDeclaration.getBody(), PublicDeclarationNode.class);
        for (PublicDeclarationNode publicDeclaration : publicDeclarations) {
            DeclarationNode memberDeclaration = publicDeclaration.getDeclaration();
            TypeResult<ValueInfo> memberType = findMemberType(memberDeclaration, context);
            if (memberType.hasValue()) {
                members.put(memberDeclaration.getIdentifier(), memberType.get());
            } else {
                throw new RuntimeException(memberType.getErrors().toString());
            }
        }
        return members.build();
    }

    private TypeResult<ValueInfo> findMemberType(DeclarationNode memberDeclaration, StaticContext context) {
        // TODO: should delegate to existing type checkers
        if (memberDeclaration instanceof FunctionDeclarationNode) {
            return typeInferer.inferFunctionType((FunctionDeclarationNode)memberDeclaration, context);
        } else if (memberDeclaration instanceof VariableDeclarationNode) {
            VariableDeclarationNode variableDeclaration = (VariableDeclarationNode)memberDeclaration;
            Option<? extends ExpressionNode> typeReference = variableDeclaration.getTypeReference();
            if (typeReference.hasValue()) {
                return typeLookup.lookupTypeReference(typeReference.get(), context).ifValueThen(toUnassignableValue());
            }
        }
        throw new RuntimeException("Cannot find type of member: " + memberDeclaration);
    }

    private Function<Type, TypeResult<ValueInfo>> toUnassignableValue() {
        return new Function<Type, TypeResult<ValueInfo>>() {
            @Override
            public TypeResult<ValueInfo> apply(Type input) {
                return TypeResult.success(ValueInfo.unassignableValue(input));
            }
        };
    }
}
