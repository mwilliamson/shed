package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;

import java.util.Map;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.types.Type;

public class AllStatementsTypeChecker {
    public static AllStatementsTypeChecker build() {
        AllStatementsTypeChecker typeChecker = new AllStatementsTypeChecker();
        BlockTypeChecker blockTypeChecker = new BlockTypeChecker(typeChecker);
        ConditionTypeChecker conditionTypeChecker = new ConditionTypeChecker();
        typeChecker.add(VariableDeclarationNode.class, new VariableDeclarationTypeChecker());
        typeChecker.add(PublicDeclarationNode.class, new PublicDeclarationTypeChecker(typeChecker));
        typeChecker.add(ReturnNode.class, new ReturnStatementTypeChecker());
        typeChecker.add(ExpressionStatementNode.class, new ExpressionStatementTypeChecker());
        typeChecker.add(ObjectDeclarationNode.class, new ObjectDeclarationTypeChecker(blockTypeChecker));
        typeChecker.add(IfThenElseStatementNode.class, new IfThenElseTypeChecker(conditionTypeChecker, blockTypeChecker));
        typeChecker.add(WhileStatementNode.class, new WhileStatementTypeChecker(conditionTypeChecker, blockTypeChecker));
        typeChecker.add(FunctionDeclarationNode.class, new FunctionDeclarationTypeChecker());
        return typeChecker;
    }
    
    private final Map<Class<?>, StatementTypeChecker<?>> typeCheckers = new HashMap<Class<?>, StatementTypeChecker<?>>();
    
    private AllStatementsTypeChecker() {
    }
    
    private <T extends StatementNode> void add(Class<T> statementType, StatementTypeChecker<T> typeChecker) {
        typeCheckers.put(statementType, typeChecker);
    }

    public <T extends StatementNode> TypeResult<StatementTypeCheckResult> typeCheck(
        T statement, NodeLocations nodeLocations, StaticContext context, Option<Type> returnType
    ) {
        StatementTypeChecker<T> typeChecker = getTypeChecker(statement);
        return typeChecker.typeCheck(statement, nodeLocations, context, returnType);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public <T extends StatementNode> StatementTypeChecker<T> getTypeChecker(T statement) {
        return (StatementTypeChecker<T>) typeCheckers.get(statement.getClass());
    }
}
