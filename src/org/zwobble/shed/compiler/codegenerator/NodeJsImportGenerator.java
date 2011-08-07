package org.zwobble.shed.compiler.codegenerator;

import java.util.List;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class NodeJsImportGenerator implements JavaScriptImportGenerator {
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Override
    public JavaScriptNode generateExpression(PackageDeclarationNode packageDeclaration, ImportNode importNode) {
        List<String> packageNames = packageDeclaration.getPackageNames();
        List<String> importNames = importNode.getNames();
        int commonPathLength = findCommonPathLength(packageNames, importNames);
        int upDistance = packageNames.size()  - commonPathLength;
        String requirePath;
        if (upDistance == 0) {
            requirePath = "./";
        } else {
            requirePath = Strings.repeat("../", upDistance);
        }
        requirePath += Joiner.on("/").join(Iterables.skip(importNames, commonPathLength));
        return js.call(js.id("require"), js.string(requirePath));
    }

    private int findCommonPathLength(List<String> first, List<String> second) {
        int minLength = Math.min(first.size(), second.size());
        for (int i = 0; i < minLength; i++) {
            if (!first.get(i).equals(second.get(i))) {
                return i;
            }
        }
        return minLength;
    }
}
