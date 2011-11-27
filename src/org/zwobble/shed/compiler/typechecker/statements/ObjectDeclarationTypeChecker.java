package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.InterfaceImplementationChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResultBuilder;
import org.zwobble.shed.compiler.typegeneration.TypeStore;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.MembersBuilder;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ObjectDeclarationTypeChecker implements StatementTypeChecker<ObjectDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final InterfaceImplementationChecker interfaceImplementationChecker;
    private final TypeStore typeStore;
    private final StaticContext context;

    @Inject
    public ObjectDeclarationTypeChecker(BlockTypeChecker blockTypeChecker, InterfaceImplementationChecker interfaceImplementationChecker,
        TypeStore typeStore, StaticContext context) {
        this.blockTypeChecker = blockTypeChecker;
        this.interfaceImplementationChecker = interfaceImplementationChecker;
        this.typeStore = typeStore;
        this.context = context;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(ObjectDeclarationNode objectDeclaration, Option<Type> returnType) {
        TypeResultBuilder<StatementTypeCheckResult> result = typeResultBuilder(StatementTypeCheckResult.noReturn());

        TypeResult<StatementTypeCheckResult> blockResult = 
            blockTypeChecker.forwardDeclareAndTypeCheck(objectDeclaration.getStatements(), Option.<Type>none());
        result.addErrors(blockResult);
        
        if (blockResult.isSuccess()) {
            Interfaces interfaces = interfaceImplementationChecker.dereferenceInterfaces(objectDeclaration.getSuperTypes());
            Members members = buildMembers(objectDeclaration);
            
            InterfaceType type = (InterfaceType) typeStore.typeDeclaredBy(objectDeclaration);
            context.add(objectDeclaration, unassignableValue(type));
            context.addInfo(type, new ScalarTypeInfo(interfaces, members));
            
            result.addErrors(interfaceImplementationChecker.checkInterfaces(objectDeclaration, type));
        }
        
        return result.build();
    }

    private Members buildMembers(ObjectDeclarationNode objectDeclaration) {
        MembersBuilder membersBuilder = new MembersBuilder();

        for (StatementNode statement : objectDeclaration.getStatements()) {
            if (statement instanceof PublicDeclarationNode) {
                DeclarationNode declaration = ((PublicDeclarationNode) statement).getDeclaration();
                membersBuilder.add(declaration.getIdentifier(), context.get(declaration).getValueInfo());
            }
        }
        return membersBuilder.build();
    }
    
}
