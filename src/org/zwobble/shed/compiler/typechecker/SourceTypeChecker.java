package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.types.Type;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singleton;
import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.TypeResultBuilder.typeResultBuilder;

public class SourceTypeChecker {
    private final BlockTypeChecker blockTypeChecker;
    private final ImportStatementTypeChecker importStatementTypeChecker;
    private final StaticContext context;

    @Inject
    public SourceTypeChecker(
        BlockTypeChecker blockTypeChecker,
        ImportStatementTypeChecker importStatementTypeChecker,
        StaticContext context
    ) {
        this.blockTypeChecker = blockTypeChecker;
        this.importStatementTypeChecker = importStatementTypeChecker;
        this.context = context;
    }
    
    public TypeResult<Void> typeCheck(SourceNode source) {
        TypeResultBuilder<Void> result = typeResultBuilder();
        
        for (ImportNode importNode : source.getImports()) {
            TypeResult<Void> importTypeCheckResult = importStatementTypeChecker.typeCheck(importNode);
            result.addErrors(importTypeCheckResult);
        }

        TypeResult<?> blockResult = blockTypeChecker.forwardDeclareAndTypeCheck(source.getStatements(), Option.<Type>none());
        result.addErrors(blockResult);
        
        boolean seenPublicStatement = false;
        for (StatementNode statement : source.getStatements()) {
            if (statement instanceof PublicDeclarationNode) {
                if (seenPublicStatement) {
                    result.addError(error(statement, "A module may have no more than one public value"));
                }
                seenPublicStatement = true;
                DeclarationNode declaration = ((PublicDeclarationNode) statement).getDeclaration();
                List<String> packageNames = source.getPackageDeclaration().getPackageNames();
                FullyQualifiedName name = fullyQualifiedName(copyOf(concat(packageNames, singleton(declaration.getIdentifier()))));
                context.addGlobal(name, context.get(declaration).getType());
            }
        }
        
        return result.build();
    }
}
