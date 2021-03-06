package org.zwobble.shed.compiler.typechecker.statements;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.typechecker.ArgumentTypeInferer;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.InterfaceDereferencer;
import org.zwobble.shed.compiler.typechecker.InterfaceImplementationChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResultBuilder;
import org.zwobble.shed.compiler.typechecker.TypeResultWithValue;
import org.zwobble.shed.compiler.typegeneration.TypeStore;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;
import static org.zwobble.shed.compiler.typechecker.statements.StatementTypeCheckResult.noReturn;

public class ClassDeclarationTypeChecker implements DeclarationTypeChecker<ClassDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final ArgumentTypeInferer argumentTypeInferer;
    private final InterfaceDereferencer interfaceDereferencer;
    private final InterfaceImplementationChecker interfaceImplementationChecker; 
    private final MembersBuilder membersBuilder;
    private final TypeLookup typeLookup;
    private final TypeStore typeStore;
    private final StaticContext context;

    @Inject
    public ClassDeclarationTypeChecker(
        BlockTypeChecker blockTypeChecker, ArgumentTypeInferer argumentTypeInferer, InterfaceImplementationChecker interfaceImplementationChecker, 
        InterfaceDereferencer interfaceDereferencer, MembersBuilder membersBuilder, TypeLookup typeLookup, TypeStore typeStore, StaticContext context
    ) {
        this.blockTypeChecker = blockTypeChecker;
        this.argumentTypeInferer = argumentTypeInferer;
        this.interfaceImplementationChecker = interfaceImplementationChecker;
        this.interfaceDereferencer = interfaceDereferencer;
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
        TypeResultWithValue<ClassType> typeResult = buildClassType(classDeclaration);
        resultBuilder.addErrors(typeResult);
        
        resultBuilder.addErrors(interfaceImplementationChecker.checkInterfaces(classDeclaration, typeResult.get()));
        
        return resultBuilder.build();
    }

    private TypeResult<?> forwardDeclareBody(ClassDeclarationNode classDeclaration) {
        return blockTypeChecker.forwardDeclare(classDeclaration.getBody());
    }

    private TypeResultWithValue<ClassType> buildClassType(ClassDeclarationNode classDeclaration) {
        Members members = buildMembers(classDeclaration);
        TypeResultWithValue<Interfaces> interfacesResult = interfaceDereferencer.dereferenceInterfaces(classDeclaration.getSuperTypes());
        ClassType type = (ClassType)typeStore.typeDeclaredBy(classDeclaration);
        TypeResultWithValue<List<Type>> classParameters = TypeResultWithValue.combine(transform(classDeclaration.getFormalArguments(), toType()));
        ScalarTypeInfo classTypeInfo = new ScalarTypeInfo(interfacesResult.get(), members);
        context.addClass(classDeclaration, type, classParameters.get(), classTypeInfo);
        return success(type).withErrorsFrom(classParameters, interfacesResult);
    }

    private Members buildMembers(ClassDeclarationNode classDeclaration) {
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

    private Function<FormalArgumentNode, TypeResultWithValue<Type>> toType() {
        return new Function<FormalArgumentNode, TypeResultWithValue<Type>>() {
            @Override
            public TypeResultWithValue<Type> apply(FormalArgumentNode input) {
                return typeLookup.lookupTypeReference(input.getType());
            }
        };
    }
}
