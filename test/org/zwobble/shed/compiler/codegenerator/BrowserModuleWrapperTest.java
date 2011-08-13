package org.zwobble.shed.compiler.codegenerator;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatementNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptVariableDeclarationNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BrowserModuleWrapperTest {
    private final JavaScriptNodes js = new JavaScriptNodes(); 
    private final BrowserModuleWrapper wrapper = new BrowserModuleWrapper();
    
    @Test public void
    packageIsDefinedAndModuleIsWrappedInAnonymousFunction() {
        JavaScriptVariableDeclarationNode original = js.var("x", js.number("0"));
        JavaScriptNode wrapped = wrapper.wrap(js.statements(original));
        assertThat(wrapped, is(
            (JavaScriptNode)js.statements(
                js.expressionStatement(
                    js.call(
                        js.func(
                            Collections.<String>emptyList(),
                            asList((JavaScriptStatementNode)original)
                        )
                    )
                )
            )
        ));
    }
}
