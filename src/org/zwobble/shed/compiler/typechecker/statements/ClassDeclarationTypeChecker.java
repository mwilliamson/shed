package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Map;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult;
import org.zwobble.shed.compiler.typechecker.VariableLookupResult.Status;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

public class ClassDeclarationTypeChecker implements DeclarationTypeChecker<ClassDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final FullyQualifiedNames fullyQualifiedNames;
    private final StaticContext context;

    @Inject
    public ClassDeclarationTypeChecker(BlockTypeChecker blockTypeChecker, FullyQualifiedNames fullyQualifiedNames, StaticContext context) {
        this.blockTypeChecker = blockTypeChecker;
        this.fullyQualifiedNames = fullyQualifiedNames;
        this.context = context;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(ClassDeclarationNode classDeclaration) {
        forwardDeclareBody(classDeclaration);
        buildClassType(classDeclaration);
        return TypeResult.success();
    }

    private void forwardDeclareBody(ClassDeclarationNode classDeclaration) {
        TypeResult<?> forwardDeclareResult = blockTypeChecker.forwardDeclare(classDeclaration.getBody());
        if (!forwardDeclareResult.isSuccess()) {
            throw new RuntimeException("Errors: " + forwardDeclareResult.getErrors());
        }
    }

    private void buildClassType(ClassDeclarationNode classDeclaration) {
        FullyQualifiedName name = fullyQualifiedNames.fullyQualifiedNameOf(classDeclaration);
        Map<String, ValueInfo> members = buildMembers(classDeclaration);
        ClassType type = new ClassType(name);
        context.add(classDeclaration, ValueInfo.unassignableValue(type));
        context.addInfo(type, new ScalarTypeInfo(interfaces(), members));
    }

    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(ClassDeclarationNode statement, Option<Type> returnType) {
        // TODO:
        return null;
    }

    private Map<String, ValueInfo> buildMembers(ClassDeclarationNode classDeclaration) {
        ImmutableMap.Builder<String, ValueInfo> members = ImmutableMap.builder();
        
        Iterable<PublicDeclarationNode> publicDeclarations = Iterables.filter(classDeclaration.getBody(), PublicDeclarationNode.class);
        for (PublicDeclarationNode publicDeclaration : publicDeclarations) {
            DeclarationNode memberDeclaration = publicDeclaration.getDeclaration();
            TypeResult<ValueInfo> memberType = findMemberType(memberDeclaration);
            if (memberType.hasValue()) {
                members.put(memberDeclaration.getIdentifier(), memberType.get());
            } else {
                throw new RuntimeException(memberType.getErrors().toString());
            }
        }
        return members.build();
    }

    private TypeResult<ValueInfo> findMemberType(DeclarationNode memberDeclaration) {
        VariableLookupResult result = context.get(memberDeclaration);
        if (result.getStatus() == Status.SUCCESS) {
            return TypeResult.success(result.getValueInfo());
        } else {
            throw new RuntimeException("Could not find type of member: " + memberDeclaration);
        }
    }
}
