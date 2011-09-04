package org.zwobble.shed.compiler.codegenerator;

import org.junit.Test;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BrowserImportGeneratorTest {
    private final BrowserImportGenerator generator = new BrowserImportGenerator();
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Test public void
    importsAreRetrievedFromGlobalShedObject() {
        assertThat(
            // FIXME: should use __shed
            // TODO: convert to callback style
            generator.generateExpression(null, new ImportNode(asList("shed", "List"))),
            is((JavaScriptNode)js.call(js.id("SHED.require"), js.string("shed.List")))
        );
    }
}
