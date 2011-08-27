package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.typechecker.ImportStatementTypeChecker.typeCheckImportStatement;
import static org.zwobble.shed.compiler.typechecker.ReturnStatementTypeChecker.typeCheckReturnStatement;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.VariableDeclarationTypeChecker.typeCheckVariableDeclaration;

public class TypeChecker {
    public static TypeResult<Void> typeCheck(SourceNode source, NodeLocations nodeLocations, StaticContext staticContext) {
        staticContext.enterNewScope(none(Type.class));
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        for (ImportNode importNode : source.getImports()) {
            TypeResult<Void> importTypeCheckResult = typeCheckImportStatement(importNode, nodeLocations, staticContext);
            errors.addAll(importTypeCheckResult.getErrors());
        }
        
        for (StatementNode statement : source.getStatements()) {
            TypeResult<Void> result = typeCheckStatement(statement, nodeLocations, staticContext);
            errors.addAll(result.getErrors());
        }
        staticContext.exitScope();
        if (errors.isEmpty()) {
            return success(null);
        } else {
            return failure(errors);
        }
    }
    
    public static TypeResult<Void> typeCheckStatement(StatementNode statement, NodeLocations nodeLocations, StaticContext context) {
        if (statement instanceof VariableDeclarationNode) {
            return typeCheckVariableDeclaration((VariableDeclarationNode)statement, nodeLocations, context);
        }
        if (statement instanceof ReturnNode) {
            return typeCheckReturnStatement((ReturnNode)statement, nodeLocations, context);
        }
        if (statement instanceof ExpressionStatementNode) {
            TypeResult<Type> result = TypeInferer.inferType(((ExpressionStatementNode) statement).getExpression(), nodeLocations, context);
            return TypeResult.<Void>success(null).withErrorsFrom(result);
        }
        if (statement instanceof ObjectDeclarationNode) {
            return typeCheckObjectDeclaration((ObjectDeclarationNode)statement, nodeLocations, context);
        }
        throw new RuntimeException("Cannot check type of statement: " + statement);
    }

    public static TypeResult<Void> typeCheckObjectDeclaration(
        ObjectDeclarationNode objectDeclaration,
        NodeLocations nodeLocations,
        StaticContext context)
    {
        TypeResult<Void> result = TypeResult.success(null);
        context.enterNewScope(none(Type.class));

        for (StatementNode statement : objectDeclaration.getStatements()) {
            TypeResult<Void> statementResult = typeCheckStatement(statement, nodeLocations, context);
            result = result.withErrorsFrom(statementResult);
        }
        context.exitScope();
        TypeResult<Void> addResult = StaticContexts.tryAdd(context, objectDeclaration.getName(), CoreTypes.OBJECT, nodeLocations.locate(objectDeclaration));
        return result.withErrorsFrom(addResult);
    }

}
