package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

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

import com.google.inject.Guice;
import com.google.inject.Injector;

public class AllStatementsTypeChecker {
    public static AllStatementsTypeChecker build() {
        return new AllStatementsTypeChecker(Guice.createInjector());
    }
    
    private final Map<Class<?>, Class<? extends StatementTypeChecker<?>>> typeCheckers = new HashMap<Class<?>, Class<? extends StatementTypeChecker<?>>>();
    private final Injector injector;
    
    @Inject
    public AllStatementsTypeChecker(Injector injector) {
        this.injector = injector;
        add(VariableDeclarationNode.class, VariableDeclarationTypeChecker.class);
        add(PublicDeclarationNode.class, PublicDeclarationTypeChecker.class);
        add(ReturnNode.class, ReturnStatementTypeChecker.class);
        add(ExpressionStatementNode.class, ExpressionStatementTypeChecker.class);
        add(ObjectDeclarationNode.class, ObjectDeclarationTypeChecker.class);
        add(IfThenElseStatementNode.class, IfThenElseTypeChecker.class);
        add(WhileStatementNode.class, WhileStatementTypeChecker.class);
        add(FunctionDeclarationNode.class, FunctionDeclarationTypeChecker.class);
    }
    
    private <T extends StatementNode> void add(Class<T> statementType, Class<? extends StatementTypeChecker<T>> typeChecker) {
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
        Class<? extends StatementTypeChecker<?>> typeCheckerClass = typeCheckers.get(statement.getClass());
        return (StatementTypeChecker<T>) injector.getInstance(typeCheckerClass);
    }
}
