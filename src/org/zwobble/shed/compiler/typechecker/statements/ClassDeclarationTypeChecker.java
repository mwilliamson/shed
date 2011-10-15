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
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public class ClassDeclarationTypeChecker implements DeclarationTypeChecker<ClassDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final TypeLookup typeLookup;
    private final FullyQualifiedNames fullyQualifiedNames;

    @Inject
    public ClassDeclarationTypeChecker(BlockTypeChecker blockTypeChecker, TypeLookup typeLookup, FullyQualifiedNames fullyQualifiedNames) {
        this.blockTypeChecker = blockTypeChecker;
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
        TypeResult<?> forwardDeclareResult = blockTypeChecker.forwardDeclare(classDeclaration.getBody(), context);
        if (!forwardDeclareResult.isSuccess()) {
            throw new RuntimeException("Errors: " + forwardDeclareResult.getErrors());
        }
        
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
        VariableLookupResult result = context.get(memberDeclaration);
        if (result.getStatus() == Status.SUCCESS) {
            return TypeResult.success(result.getValueInfo());
        } else {
            throw new RuntimeException("Could not find type of member: " + memberDeclaration);
        }
    }
}
