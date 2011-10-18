package org.zwobble.shed.compiler.typechecker.statements;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ClassDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.types.Type;

import com.google.inject.Injector;

public class AllStatementsTypeChecker {
    private final Map<Class<?>, Class<? extends StatementTypeChecker<?>>> typeCheckers = 
        new HashMap<Class<?>, Class<? extends StatementTypeChecker<?>>>();
    private final Map<Class<?>, Class<? extends StatementForwardDeclarer<?>>> forwardDeclarers =
        new HashMap<Class<?>, Class<? extends StatementForwardDeclarer<?>>>();
    private final Injector injector;
    
    @Inject
    public AllStatementsTypeChecker(Injector injector) {
        this.injector = injector;
        addTypeChecker(ReturnNode.class, ReturnStatementTypeChecker.class);
        addTypeChecker(ExpressionStatementNode.class, ExpressionStatementTypeChecker.class);
        addTypeChecker(ObjectDeclarationNode.class, ObjectDeclarationTypeChecker.class);
        addTypeChecker(IfThenElseStatementNode.class, IfThenElseTypeChecker.class);
        addTypeChecker(WhileStatementNode.class, WhileStatementTypeChecker.class);
        
        addDeclarationTypeChecker(PublicDeclarationNode.class, PublicDeclarationTypeChecker.class);
        addDeclarationTypeChecker(VariableDeclarationNode.class, VariableDeclarationTypeChecker.class);
        addDeclarationTypeChecker(FunctionDeclarationNode.class, FunctionDeclarationTypeChecker.class);
        addDeclarationTypeChecker(ClassDeclarationNode.class, ClassDeclarationTypeChecker.class);
    }
    
    private <T extends StatementNode> void addTypeChecker(Class<T> statementType, Class<? extends StatementTypeChecker<T>> typeChecker) {
        typeCheckers.put(statementType, typeChecker);
    }
    
    private <T extends StatementNode> void addForwardDeclarer(Class<T> statementType, Class<? extends StatementForwardDeclarer<T>> declarer) {
        forwardDeclarers.put(statementType, declarer);
    }
    
    private <T extends StatementNode> void addDeclarationTypeChecker(
        Class<T> statementType, Class<? extends DeclarationTypeChecker<T>> typeChecker
    ) {
        addTypeChecker(statementType, typeChecker);
        addForwardDeclarer(statementType, typeChecker);
    }

    public <T extends StatementNode> TypeResult<StatementTypeCheckResult> typeCheck(T statement, Option<Type> returnType) {
        return getTypeChecker(statement).typeCheck(statement, returnType);
    }

    public TypeResult<?> forwardDeclare(StatementNode statement) {
        return getForwardDeclarer(statement).forwardDeclare(statement);
    }

    @SuppressWarnings("unchecked")
    private <T extends StatementNode> StatementTypeChecker<T> getTypeChecker(T statement) {
        Class<? extends StatementTypeChecker<?>> typeCheckerClass = typeCheckers.get(statement.getClass());
        return (StatementTypeChecker<T>) injector.getInstance(typeCheckerClass);
    }

    @SuppressWarnings("unchecked")
    private <T extends StatementNode> StatementForwardDeclarer<T> getForwardDeclarer(T statement) {
        if (forwardDeclarers.containsKey(statement.getClass())) {
            Class<? extends StatementForwardDeclarer<?>> typeCheckerClass = forwardDeclarers.get(statement.getClass());
            return (StatementForwardDeclarer<T>) injector.getInstance(typeCheckerClass);
        } else {
            return new NoOpForwardDeclarer<T>();
        }
    }
}
