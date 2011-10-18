package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.typechecker.errors.UnresolvedImportError;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;

public class ImportStatementTypeChecker {
    private final NodeLocations nodeLocations;
    private final StaticContext context;

    @Inject
    public ImportStatementTypeChecker(NodeLocations nodeLocations, StaticContext context) {
        this.nodeLocations = nodeLocations;
        this.context = context;
    }
    
    public TypeResult<Void>
    typeCheck(ImportNode importStatement) {
        List<String> identifiers = importStatement.getNames();
        Option<Type> importedValueType = context.lookupGlobal(identifiers);
        if (importedValueType.hasValue()) {
            context.add(importStatement, unassignableValue(importedValueType.get()));
            return success();
        } else {
            return failure(new CompilerError(nodeLocations.locate(importStatement), new UnresolvedImportError(identifiers)));
        }
    }
}
