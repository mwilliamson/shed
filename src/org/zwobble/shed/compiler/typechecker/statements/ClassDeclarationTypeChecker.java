package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Set;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerErrorDescription;
import org.zwobble.shed.compiler.CompilerErrorWithSyntaxNode;
import org.zwobble.shed.compiler.HasErrors;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.typechecker.ArgumentTypeInferer;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.SubTyping;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResultBuilder;
import org.zwobble.shed.compiler.typechecker.errors.MissingMemberError;
import org.zwobble.shed.compiler.typechecker.errors.WrongMemberTypeError;
import org.zwobble.shed.compiler.typegeneration.TypeStore;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.Member;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;
import static org.zwobble.shed.compiler.typechecker.statements.StatementTypeCheckResult.noReturn;

public class ClassDeclarationTypeChecker implements DeclarationTypeChecker<ClassDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final ArgumentTypeInferer argumentTypeInferer;
    private final SubTyping subTyping; 
    private final MembersBuilder membersBuilder;
    private final TypeLookup typeLookup;
    private final TypeStore typeStore;
    private final StaticContext context;

    @Inject
    public ClassDeclarationTypeChecker(
        BlockTypeChecker blockTypeChecker, ArgumentTypeInferer argumentTypeInferer, SubTyping subTyping, 
        MembersBuilder membersBuilder, TypeLookup typeLookup, TypeStore typeStore, StaticContext context
    ) {
        this.blockTypeChecker = blockTypeChecker;
        this.argumentTypeInferer = argumentTypeInferer;
        this.subTyping = subTyping;
        this.membersBuilder = membersBuilder;
        this.typeLookup = typeLookup;
        this.typeStore = typeStore;
        this.context = context;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(ClassDeclarationNode classDeclaration) {
        TypeResult<?> result = forwardDeclareBody(classDeclaration);
        buildClassType(classDeclaration);
        return result;
    }

    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(ClassDeclarationNode classDeclaration, Option<Type> returnType) {
        TypeResultBuilder<StatementTypeCheckResult> resultBuilder = typeResultBuilder(noReturn());

        argumentTypeInferer.inferArgumentTypesAndAddToContext(classDeclaration.getFormalArguments());
        resultBuilder.addErrors(blockTypeChecker.typeCheck(classDeclaration.getBody(), returnType));
        ClassType type = buildClassType(classDeclaration);
        
        resultBuilder.addErrors(checkInterfaces(classDeclaration, type));
        
        return resultBuilder.build();
    }

    private TypeResult<?> forwardDeclareBody(ClassDeclarationNode classDeclaration) {
        return blockTypeChecker.forwardDeclare(classDeclaration.getBody());
    }

    private ClassType buildClassType(ClassDeclarationNode classDeclaration) {
        Members members = buildMembers(classDeclaration);
        Set<ScalarType> superTypes = buildSuperTypes(classDeclaration);
        ClassType type = (ClassType)typeStore.typeDeclaredBy(classDeclaration);
        Iterable<Type> classParameters = transform(classDeclaration.getFormalArguments(), toType());
        ScalarTypeInfo classTypeInfo = new ScalarTypeInfo(superTypes, members);
        context.addClass(classDeclaration, type, classParameters, classTypeInfo);
        return type;
    }

    private Members buildMembers(ClassDeclarationNode classDeclaration) {
        Iterable<PublicDeclarationNode> publicDeclarations = filter(classDeclaration.getBody(), PublicDeclarationNode.class);
        Iterable<DeclarationNode> memberDeclarations = transform(publicDeclarations, toMemberDeclaration());
        return membersBuilder.buildMembers(memberDeclarations);
    }

    private Set<ScalarType> buildSuperTypes(ClassDeclarationNode classDeclaration) {
        return ImmutableSet.copyOf(transform(classDeclaration.getSuperTypes(), lookupType()));
    }

    private HasErrors checkInterfaces(ClassDeclarationNode classDeclaration, ClassType type) {
        TypeResultBuilder<?> resultBuilder = typeResultBuilder();
        ScalarTypeInfo typeInfo = context.getInfo(type);
        for (ScalarType superType : typeInfo.getSuperTypes()) {
            ScalarTypeInfo superTypeInfo = context.getInfo(superType);
            
            for (Member member : superTypeInfo.getMembers()) {
                resultBuilder.addErrors(checkInterfaceMember(classDeclaration, typeInfo, superType, member));
            }
        }
        return resultBuilder.build();
    }

    private TypeResult<?> checkInterfaceMember(ClassDeclarationNode classDeclaration, ScalarTypeInfo typeInfo, Type superType, Member member) {
        String memberName = member.getName();
        Members classMembers = typeInfo.getMembers();
        Option<Member> actualMemberOption = classMembers.lookup(memberName);
        if (!actualMemberOption.hasValue()) {
            return TypeResult.failure(new CompilerErrorWithSyntaxNode(classDeclaration, new MissingMemberError(superType, memberName)));
        }
        Type expectedMemberType = member.getType();
        Type actualMemberType = actualMemberOption.get().getType();
        if (!subTyping.isSubType(actualMemberType, expectedMemberType)) {
            CompilerErrorDescription description = new WrongMemberTypeError(superType, memberName, expectedMemberType, actualMemberType);
            return TypeResult.failure(new CompilerErrorWithSyntaxNode(classDeclaration, description));
        }
        
        return TypeResult.success();
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

    private Function<ExpressionNode, ScalarType> lookupType() {
        return new Function<ExpressionNode, ScalarType>() {
            @Override
            public ScalarType apply(ExpressionNode input) {
                TypeResult<Type> lookupResult = typeLookup.lookupTypeReference(input);
                if (!lookupResult.isSuccess()) {
                    // TODO:
                    throw new RuntimeException("Failed type lookup " + lookupResult.getErrors());
                }
                // TODO: handle non-scalar types
                return (ScalarType)lookupResult.get();
            }
        };
    }
}
