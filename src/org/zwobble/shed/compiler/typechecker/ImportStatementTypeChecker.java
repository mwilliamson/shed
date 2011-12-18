package org.zwobble.shed.compiler.typechecker;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.modules.Module;
import org.zwobble.shed.compiler.modules.Modules;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.typechecker.errors.UnresolvedImportError;
import org.zwobble.shed.compiler.typechecker.errors.UntypedReferenceError;
import org.zwobble.shed.compiler.types.Type;

import static org.zwobble.shed.compiler.errors.CompilerErrors.error;

import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;

import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.ValueInfo.unassignableValue;

public class ImportStatementTypeChecker {
    private final StaticContext context;
    private final Modules modules;

    @Inject
    public ImportStatementTypeChecker(StaticContext context, Modules modules) {
        this.context = context;
        this.modules = modules;
    }
    
    public TypeResult<Void>
    typeCheck(ImportNode importStatement) {
        List<String> identifiers = importStatement.getNames();
        FullyQualifiedName name = fullyQualifiedName(identifiers);
        Option<Type> importedValueType = context.lookupGlobal(name);
        if (importedValueType.hasValue()) {
            context.add(importStatement, unassignableValue(importedValueType.get()));
            return success();
        } else {
            Option<Module> module = modules.lookup(name);
            if (module.hasValue()) {
                Option<Type> typeOption = context.getTypeOf(module.get().getDeclaration());
                if (typeOption.hasValue()) {
                    context.add(importStatement, unassignableValue(typeOption.get()));
                    return success();
                } else {
                    return failure(error(importStatement, new UntypedReferenceError(name.asString())));
                }
            } else {
                return failure(error(importStatement, new UnresolvedImportError(identifiers)));
            }
        }
    }
}
