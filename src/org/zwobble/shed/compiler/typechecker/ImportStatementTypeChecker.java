package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.typechecker.errors.UnresolvedImportError;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.CompilerErrors.error;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ImportStatementTypeChecker {
    private final StaticContext context;

    @Inject
    public ImportStatementTypeChecker(StaticContext context) {
        this.context = context;
    }
    
    public TypeResult<Void>
    typeCheck(ImportNode importStatement) {
        List<String> identifiers = importStatement.getNames();
        Option<Type> importedValueType = context.lookupGlobal(fullyQualifiedName(identifiers));
        if (importedValueType.hasValue()) {
            context.add(importStatement, unassignableValue(importedValueType.get()));
            return success();
        } else {
            return failure(error(importStatement, new UnresolvedImportError(identifiers)));
        }
    }
}
