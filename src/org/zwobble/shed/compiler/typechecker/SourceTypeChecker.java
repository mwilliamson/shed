package org.zwobble.shed.compiler.typechecker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class SourceTypeChecker {
    private final BlockTypeChecker blockTypeChecker;
    private final ImportStatementTypeChecker importStatementTypeChecker;

    @Inject
    public SourceTypeChecker(BlockTypeChecker blockTypeChecker, ImportStatementTypeChecker importStatementTypeChecker) {
        this.blockTypeChecker = blockTypeChecker;
        this.importStatementTypeChecker = importStatementTypeChecker;
    }
    
    public TypeResult<Void> typeCheck(SourceNode source) {
        List<CompilerError> errors = new ArrayList<CompilerError>();
        
        for (ImportNode importNode : source.getImports()) {
            TypeResult<Void> importTypeCheckResult = importStatementTypeChecker.typeCheck(importNode);
            errors.addAll(importTypeCheckResult.getErrors());
        }

        TypeResult<?> blockResult = blockTypeChecker.forwardDeclareAndTypeCheck(source.getStatements(), Option.<Type>none());
        errors.addAll(blockResult.getErrors());
        
        boolean seenPublicStatement = false;
        for (StatementNode statement : source.getStatements()) {
            if (statement instanceof PublicDeclarationNode) {
                if (seenPublicStatement) {
                    errors.add(error(statement, "A module may have no more than one public value"));
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
