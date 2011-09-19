package org.zwobble.shed.compiler.codegenerator;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptExpressionNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatements;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.zwobble.shed.compiler.codegenerator.JavaScriptGenerator.CORE_VALUES_OBJECT_NAME;

public class BrowserModuleWrapper implements JavaScriptModuleWrapper {
    private final JavaScriptNodes js = new JavaScriptNodes();

    @Override
    public JavaScriptNode wrap(PackageDeclarationNode packageDeclaration, Iterable<ImportNode> imports, JavaScriptStatements module, JavaScriptNamer namer) {
        List<JavaScriptExpressionNode> requireArguments = new ArrayList<JavaScriptExpressionNode>();
        requireArguments.add(js.string("shed.core"));
        for (ImportNode importNode : imports) {
            requireArguments.add(js.string(Joiner.on(".").join(importNode.getNames())));
        }
        ArrayList<String> importNames = newArrayList(Iterables.concat(
            singletonList(CORE_VALUES_OBJECT_NAME),
            Iterables.transform(imports, toImportName(namer))
        ));
        requireArguments.add(js.func(importNames, module.getStatements()));
        return js.statements(
            js.expressionStatement(
                js.call(
                    js.id("SHED.require"),
                    requireArguments
                )
            )
        );
    }

    private Function<ImportNode, String> toImportName(final JavaScriptNamer namer) {
        return new Function<ImportNode, String>() {
            @Override
            public String apply(ImportNode input) {
                return namer.javaScriptIdentifierFor(input);
            }
        };
    }
}
