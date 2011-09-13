package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.SimpleCompilerError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.types.Type;

import com.google.common.base.Joiner;

import static java.util.Arrays.asList;
import static org.zwobble.shed.compiler.typechecker.TypeResult.failure;

public class ImportStatementTypeChecker {
    public static TypeResult<Void>
    typeCheckImportStatement(ImportNode importStatement, NodeLocations nodeLocations, StaticContext context) {
        List<String> identifiers = importStatement.getNames();
        String identifier = identifiers.get(identifiers.size() - 1);
        Option<Type> importedValueType = context.lookupGlobal(identifiers);
        if (importedValueType.hasValue()) {
            return StaticContexts.tryAdd(context, identifier, importedValueType.get(), nodeLocations.locate(importStatement));
        } else {
            return failure(asList(new SimpleCompilerError(
                nodeLocations.locate(importStatement),
                "The import \"" + Joiner.on(".").join(identifiers) + "\" cannot be resolved"
            )));
        }
    }
}
