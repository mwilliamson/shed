package org.zwobble.shed.compiler.typechecker.statements;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.types.Type;

public class PublicDeclarationTypeChecker implements DeclarationTypeChecker<PublicDeclarationNode> {
    private final AllStatementsTypeChecker statementsTypeChecker;

    @Inject
    public PublicDeclarationTypeChecker(AllStatementsTypeChecker statementsTypeChecker) {
        this.statementsTypeChecker = statementsTypeChecker;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        PublicDeclarationNode statement, StaticContext context, Option<Type> returnType
    ) {
        return statementsTypeChecker.typeCheck(statement.getDeclaration(), context, returnType);
    }

    @Override
    public TypeResult<?> forwardDeclare(PublicDeclarationNode statement, StaticContext context) {
        return statementsTypeChecker.forwardDeclare(statement.getDeclaration(), context);
    }
}
