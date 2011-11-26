package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.InterfaceImplementationChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeLookup;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResultBuilder;
import org.zwobble.shed.compiler.typegeneration.TypeStore;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.Members;
import org.zwobble.shed.compiler.types.MembersBuilder;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;

public class ObjectDeclarationTypeChecker implements StatementTypeChecker<ObjectDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final InterfaceImplementationChecker interfaceImplementationChecker;
    private final TypeLookup typeLookup;
    private final TypeStore typeStore;
    private final StaticContext context;

    @Inject
    public ObjectDeclarationTypeChecker(BlockTypeChecker blockTypeChecker, InterfaceImplementationChecker interfaceImplementationChecker,
        TypeLookup typeLookup, TypeStore typeStore, StaticContext context) {
        this.blockTypeChecker = blockTypeChecker;
        this.interfaceImplementationChecker = interfaceImplementationChecker;
        this.typeLookup = typeLookup;
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
            Interfaces interfaces = dereferenceInterfaces(objectDeclaration);
            Members members = buildMembers(objectDeclaration);
            
            InterfaceType type = (InterfaceType) typeStore.typeDeclaredBy(objectDeclaration);
            context.add(objectDeclaration, unassignableValue(type));
            context.addInfo(type, new ScalarTypeInfo(interfaces, members));
            
            result.addErrors(interfaceImplementationChecker.checkInterfaces(objectDeclaration, type));
        }
        
        return result.build();
    }

    private Interfaces dereferenceInterfaces(ObjectDeclarationNode objectDeclaration) {
        // TODO: copied from ClassDeclarationTypeChecker
        return interfaces(transform(objectDeclaration.getSuperTypes(), lookupType()));
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
