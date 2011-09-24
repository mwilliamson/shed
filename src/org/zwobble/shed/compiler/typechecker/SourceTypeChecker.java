package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.ImportStatementTypeChecker.typeCheckImportStatement;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class SourceTypeChecker {
    private final BlockTypeChecker blockTypeChecker;

    public SourceTypeChecker(BlockTypeChecker blockTypeChecker) {
        this.blockTypeChecker = blockTypeChecker;
    }
    
    public TypeResult<Void> typeCheck(SourceNode source, NodeLocations nodeLocations, StaticContext staticContext) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        for (ImportNode importNode : source.getImports()) {
            TypeResult<Void> importTypeCheckResult = typeCheckImportStatement(importNode, nodeLocations, staticContext);
            errors.addAll(importTypeCheckResult.getErrors());
        }

        TypeResult<?> blockResult = blockTypeChecker.typeCheckBlock(source.getStatements(), nodeLocations, staticContext, Option.<Type>none());
        errors.addAll(blockResult.getErrors());
        
        boolean seenPublicStatement = false;
        for (StatementNode statement : source.getStatements()) {
            if (statement instanceof PublicDeclarationNode) {
                if (seenPublicStatement) {
                    errors.add(CompilerError.error(nodeLocations.locate(statement), "A module may have no more than one public value"));
                }
                seenPublicStatement = true;
            }
        }
        
        if (errors.isEmpty()) {
            return success(null);
        } else {
            return failure(errors);
        }
    }
}
