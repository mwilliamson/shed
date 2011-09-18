package org.zwobble.shed.compiler.codegenerator.javascript;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JavaScriptNodesTest {
    @Test public void
    scopeCreatesAnonymousFunctionAndCallsIt() {
        JavaScriptNodes js = new JavaScriptNodes();
        List<JavaScriptStatementNode> statements = Arrays.<JavaScriptStatementNode>asList(js.expressionStatement(js.bool(true)));
        JavaScriptStatementNode result = js.scope(statements);
        assertThat(result, is((Object)js.expressionStatement(js.call(js.func(Collections.<String>emptyList(), statements)))));
    }
}
