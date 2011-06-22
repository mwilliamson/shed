package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Joiner;

import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.typechecker.ImmutableVariableDeclarationTypeChecker.typeCheckImmutableVariableDeclaration;
import static org.zwobble.shed.compiler.typechecker.ReturnStatementTypeChecker.typeCheckReturnStatement;
import static org.zwobble.shed.compiler.typechecker.TypeErrors.duplicateIdentifierError;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class TypeChecker {
    public static TypeResult<Void> typeCheck(SourceNode source, NodeLocations nodeLocations, StaticContext staticContext) {
        staticContext.enterNewScope(none(Type.class));
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        for (ImportNode importNode : source.getImports()) {
            List<String> identifiers = importNode.getNames();
            String identifier = identifiers.get(identifiers.size() - 1);
            Option<Type> importedValueType = staticContext.lookupGlobal(identifiers);
            if (importedValueType.hasValue()) {
                if (staticContext.isDeclaredInCurrentScope(identifier)) {
                    errors.add(duplicateIdentifierError(identifier, nodeLocations.locate(importNode)));
                } else {
                    staticContext.add(identifier, importedValueType.get());                    
                }
            } else {
                errors.add(new CompilerError(
                    nodeLocations.locate(importNode),
                    "The import \"" + Joiner.on(".").join(identifiers) + "\" cannot be resolved"
                ));
            }
        }
        
        for (StatementNode statement : source.getStatements()) {
            TypeResult<Void> result = typeCheckStatement(statement, nodeLocations, staticContext);
            errors.addAll(result.getErrors());
        }
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
