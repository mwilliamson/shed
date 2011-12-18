package org.zwobble.shed.compiler.typechecker;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;

public class SourceTypeChecker {
    private final BlockTypeChecker blockTypeChecker;
    private final ImportStatementTypeChecker importStatementTypeChecker;

    @Inject
    public SourceTypeChecker(
        BlockTypeChecker blockTypeChecker,
        ImportStatementTypeChecker importStatementTypeChecker
    ) {
        this.blockTypeChecker = blockTypeChecker;
        this.importStatementTypeChecker = importStatementTypeChecker;
    }
    
    public TypeResult<Void> typeCheck(SourceNode source) {
        TypeResultBuilder<Void> result = typeResultBuilder();
        
        for (ImportNode importNode : source.getImports()) {
            TypeResult<Void> importTypeCheckResult = importStatementTypeChecker.typeCheck(importNode);
            result.addErrors(importTypeCheckResult);
        }

        TypeResult<?> blockResult = blockTypeChecker.forwardDeclareAndTypeCheck(source.getStatements(), Option.<Type>none());
        result.addErrors(blockResult);
        
        return result.build();
    }
}
