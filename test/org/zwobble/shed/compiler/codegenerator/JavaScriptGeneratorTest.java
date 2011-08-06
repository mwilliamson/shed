package org.zwobble.shed.compiler.codegenerator;

import org.junit.Test;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JavaScriptGeneratorTest {
    private final JavaScriptGenerator generator = new JavaScriptGenerator();
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Test public void
    booleanLiteralsAreConvertedToBoxedBooleans() {
        booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(true);
        booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(false);
    }
    
    private void booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(boolean value) {
        BooleanLiteralNode source = new BooleanLiteralNode(value);
        assertGeneratedJavaScript(source, js.call(js.id("SHED.shed.lang.Boolean"), js.bool(value)));
    }
    
    @Test public void
    numberLiteralsAreConvertedToBoxedNumbers() {
        NumberLiteralNode source = new NumberLiteralNode("4.2");
        assertGeneratedJavaScript(source, js.call(js.id("SHED.shed.lang.Number"), js.number("4.2")));
    }
    
    private void assertGeneratedJavaScript(SyntaxNode source, JavaScriptNode expectedJavaScript) {
        assertThat(generator.generate(source), is(expectedJavaScript));
    }
}
