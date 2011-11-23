package org.zwobble.shed.compiler.typechecker.statements;

import java.util.Map;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.nodes.InterfaceDeclarationNode;
import org.zwobble.shed.compiler.typechecker.BlockTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class InterfaceDeclarationTypeChecker implements DeclarationTypeChecker<InterfaceDeclarationNode> {
    private final BlockTypeChecker blockTypeChecker;
    private final MembersBuilder membersBuilder;
    private final FullyQualifiedNames fullyQualifiedNames;
    private final StaticContext context;
    
    @Inject
    public InterfaceDeclarationTypeChecker(BlockTypeChecker blockTypeChecker, MembersBuilder membersBuilder, FullyQualifiedNames fullyQualifiedNames, StaticContext context) {
        this.blockTypeChecker = blockTypeChecker;
        this.membersBuilder = membersBuilder;
        this.fullyQualifiedNames = fullyQualifiedNames;
        this.context = context;
    }
    
    @Override
    public TypeResult<?> forwardDeclare(InterfaceDeclarationNode declaration) {
        TypeResult<?> result = forwardDeclareBody(declaration);
        FullyQualifiedName name = fullyQualifiedNames.fullyQualifiedNameOf(declaration);
        context.addInterface(declaration, new InterfaceType(name), new ScalarTypeInfo(Interfaces.interfaces(), buildMembers(declaration)));
        return result;
    }

    private TypeResult<?> forwardDeclareBody(InterfaceDeclarationNode declaration) {
        return blockTypeChecker.forwardDeclare(declaration.getBody());
    }
    
    private Map<String, ValueInfo> buildMembers(InterfaceDeclarationNode declaration) {
        return membersBuilder.buildMembers(declaration.getBody());
    }

    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(InterfaceDeclarationNode statement, Option<Type> returnType) {
        return success(StatementTypeCheckResult.noReturn());
    }

}
