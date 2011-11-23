package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Map;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

public class ClassDeclarationTypeChecker implements DeclarationTypeChecker<ClassDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final MembersBuilder membersBuilder;
    private final TypeLookup typeLookup;
    private final FullyQualifiedNames fullyQualifiedNames;
    private final StaticContext context;

    @Inject
    public ClassDeclarationTypeChecker(
        BlockTypeChecker blockTypeChecker, MembersBuilder membersBuilder, TypeLookup typeLookup, FullyQualifiedNames fullyQualifiedNames, StaticContext context) {
        this.blockTypeChecker = blockTypeChecker;
        this.membersBuilder = membersBuilder;
        this.typeLookup = typeLookup;
        this.fullyQualifiedNames = fullyQualifiedNames;
        this.context = context;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(ClassDeclarationNode classDeclaration) {
        TypeResult<?> result = forwardDeclareBody(classDeclaration);
        buildClassType(classDeclaration);
        return result;
    }

    private TypeResult<?> forwardDeclareBody(ClassDeclarationNode classDeclaration) {
        return blockTypeChecker.forwardDeclare(classDeclaration.getBody());
    }

    private void buildClassType(ClassDeclarationNode classDeclaration) {
        FullyQualifiedName name = fullyQualifiedNames.fullyQualifiedNameOf(classDeclaration);
        Map<String, ValueInfo> members = buildMembers(classDeclaration);
        ClassType type = new ClassType(name);
        Iterable<Type> classParameters = transform(classDeclaration.getFormalArguments(), toType());
        ScalarTypeInfo classTypeInfo = new ScalarTypeInfo(interfaces(), members);
        context.addClass(classDeclaration, type, classParameters, classTypeInfo);
    }

    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(ClassDeclarationNode classDeclaration, Option<Type> returnType) {
        TypeResult<StatementTypeCheckResult> result = blockTypeChecker.typeCheck(classDeclaration.getBody(), returnType);
        buildClassType(classDeclaration);
        return result;
    }

    private Map<String, ValueInfo> buildMembers(ClassDeclarationNode classDeclaration) {
        Iterable<PublicDeclarationNode> publicDeclarations = filter(classDeclaration.getBody(), PublicDeclarationNode.class);
        Iterable<DeclarationNode> memberDeclarations = transform(publicDeclarations, toMemberDeclaration());
        return membersBuilder.buildMembers(memberDeclarations);
    }

    private Function<PublicDeclarationNode, DeclarationNode> toMemberDeclaration() {
        return new Function<PublicDeclarationNode, DeclarationNode>() {
            @Override
            public DeclarationNode apply(PublicDeclarationNode input) {
                return input.getDeclaration();
            }
        };
    }

    private Function<FormalArgumentNode, Type> toType() {
        return new Function<FormalArgumentNode, Type>() {
            @Override
            public Type apply(FormalArgumentNode input) {
                TypeResult<Type> lookupResult = typeLookup.lookupTypeReference(input.getType());
                if (!lookupResult.isSuccess()) {
                    // TODO:
                    throw new RuntimeException("Failed type lookup");
                }
                return lookupResult.get();
            }
        };
    }
}
