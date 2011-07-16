package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.typechecker.VariableDeclarationTypeChecker.typeCheckImmutableVariableDeclaration;
import static org.zwobble.shed.compiler.typechecker.ImportStatementTypeChecker.typeCheckImportStatement;
import static org.zwobble.shed.compiler.typechecker.ReturnStatementTypeChecker.typeCheckReturnStatement;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

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
        if (statement instanceof ImmutableVariableNode) {
            return typeCheckImmutableVariableDeclaration((ImmutableVariableNode)statement, nodeLocations, context);
        }
        if (statement instanceof ReturnNode) {
            return typeCheckReturnStatement((ReturnNode)statement, nodeLocations, context);
        }
        throw new RuntimeException("Cannot check type of statement: " + statement);
    }

}
