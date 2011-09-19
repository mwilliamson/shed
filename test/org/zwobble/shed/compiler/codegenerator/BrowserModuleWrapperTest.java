package org.zwobble.shed.compiler.codegenerator;

import org.junit.Test;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatementNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptVariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BrowserModuleWrapperTest {
    private final JavaScriptNodes js = new JavaScriptNodes(); 
    private final BrowserModuleWrapper wrapper = new BrowserModuleWrapper();
    
    @Test public void
    packageIsDefinedAndModuleIsWrappedInRequireCall() {
        JavaScriptVariableDeclarationNode original = js.var("x", js.number("0"));
        JavaScriptNode wrapped = wrapper.wrap(
            null, 
            asList(new ImportNode(asList("shed", "blah")), new ImportNode(asList("shed", "example"))), 
            js.statements(original),
            new JavaScriptNamer(new ReferencesBuilder().build())
        );
        assertThat(wrapped, is(
            (JavaScriptNode)js.statements(
                js.expressionStatement(
                    js.call(
                        js.id("SHED.require"),
                        js.string("shed.core"),
                        js.string("shed.blah"),
                        js.string("shed.example"),
                        js.func(
                            asList(JavaScriptGenerator.CORE_VALUES_OBJECT_NAME, "blah__1", "example__1"),
                            asList((JavaScriptStatementNode)original)
                        )
                    )
                )
            )
        ));
    }
}
