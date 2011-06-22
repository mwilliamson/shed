package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.CompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Joiner;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeErrors.duplicateIdentifierError;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResult.success;

public class ImportStatementTypeChecker {
    public static TypeResult<Void>
    typeCheckImportStatement(ImportNode importStatement, NodeLocations nodeLocations, StaticContext context) {
        List<String> identifiers = importStatement.getNames();
        String identifier = identifiers.get(identifiers.size() - 1);
        Option<Type> importedValueType = context.lookupGlobal(identifiers);
        if (importedValueType.hasValue()) {
            if (context.isDeclaredInCurrentScope(identifier)) {
                return failure(asList(duplicateIdentifierError(identifier, nodeLocations.locate(importStatement))));
            } else {
                context.add(identifier, importedValueType.get());
                return success(null);
            }
        } else {
            return failure(asList(new CompilerError(
                nodeLocations.locate(importStatement),
                "The import \"" + Joiner.on(".").join(identifiers) + "\" cannot be resolved"
            )));
        }
    }
}
