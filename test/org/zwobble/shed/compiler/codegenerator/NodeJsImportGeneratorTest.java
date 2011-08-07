package org.zwobble.shed.compiler.codegenerator;

import org.junit.Test;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NodeJsImportGeneratorTest {
    private final NodeJsImportGenerator generator = new NodeJsImportGenerator();
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Test public void
    importInSamePackageIsConvertedToRequireInSameDirectory() {
        assertGeneratedJavaScript(
            new PackageDeclarationNode(asList("shed", "example")),
            new ImportNode(asList("shed", "example", "Blah")),
            js.call(js.id("require"), js.string("./Blah"))
        );
    }
    
    @Test public void
    importInSubPackageIsConvertedToRequireInSubDirectory() {
        assertGeneratedJavaScript(
            new PackageDeclarationNode(asList("shed")),
            new ImportNode(asList("shed", "example", "time", "DateTime")),
            js.call(js.id("require"), js.string("./example/time/DateTime"))
        );
    }
    
    @Test public void
    importInSuperPackageIsConvertedToRequireInSuperDirectory() {
        assertGeneratedJavaScript(
            new PackageDeclarationNode(asList("shed", "example", "time")),
            new ImportNode(asList("shed", "List")),
            js.call(js.id("require"), js.string("../../List"))
        );
    }
    
    private void assertGeneratedJavaScript(
        PackageDeclarationNode packageDeclaration,
        ImportNode importNode,
        JavaScriptNode expectedJavaScript
    ) {
        assertThat(generator.generateExpression(packageDeclaration, importNode), is(expectedJavaScript));
    }
}
