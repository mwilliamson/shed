package org.zwobble.shed.compiler.modules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;

public class ModuleGeneratorTest {
    private final List<ImportNode> noImports = Collections.emptyList();
    private final ModuleGenerator moduleGenerator = new ModuleGenerator();
    
    @Test public void
    moduleGeneratedForEachSourceWithPublicNode() {
        assertThat(
            generate(
                Nodes.source(
                    Nodes.packageDeclaration("shed", "example"),
                    noImports,
                    asList(
                        Nodes.immutableVar("x", Nodes.bool(false)),
                        Nodes.publik(Nodes.func("go", Nodes.noFormalArguments(), Nodes.id("Unit"), Nodes.block()))
                    )
                )
            ),
            is(Modules.build(Module.create(fullyQualifiedName("shed", "example", "go"))))
        );
    }
    
    @Test public void
    noModuleGeneratedIfNoPublicValues() {
        assertThat(
            generate(
                Nodes.source(
                    Nodes.packageDeclaration("shed", "example"),
                    noImports,
                    Arrays.<StatementNode>asList(
                        Nodes.immutableVar("x", Nodes.bool(false)),
                        Nodes.func("go", Nodes.noFormalArguments(), Nodes.id("Unit"), Nodes.block())
                    )
                )
            ),
            is(Modules.build())
        );
    }
    
    private Modules generate(SourceNode... sourceNodes) {
        return moduleGenerator.generateModules(asList(sourceNodes));
    }
}
