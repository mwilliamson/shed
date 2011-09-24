package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.types.Type;

public class PublicDeclarationTypeChecker implements StatementTypeChecker<PublicDeclarationNode> {
    private final AllStatementsTypeChecker statementsTypeChecker;

    @Inject
    public PublicDeclarationTypeChecker(AllStatementsTypeChecker statementsTypeChecker) {
        this.statementsTypeChecker = statementsTypeChecker;
    }
    
    @Override
    public TypeResult<StatementTypeCheckResult> typeCheck(
        PublicDeclarationNode statement, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    ) {
        return statementsTypeChecker.typeCheck(statement.getDeclaration(), nodeLocations, context, returnType);
    }
}
