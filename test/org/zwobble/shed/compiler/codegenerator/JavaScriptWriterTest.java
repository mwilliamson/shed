package org.zwobble.shed.compiler.codegenerator;

import org.junit.Test;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptBooleanLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptIdentifierNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNumberLiteralNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStringLiteralNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JavaScriptWriterTest {
    private final JavaScriptWriter writer = new JavaScriptWriter();
    
    @Test public void
    booleanIsWrittenAsTrueOrFalseKeywords() {
        assertThat(writer.write(new JavaScriptBooleanLiteralNode(true)), is("true"));
        assertThat(writer.write(new JavaScriptBooleanLiteralNode(false)), is("false"));
    }
    
    @Test public void
    numberIsWrittenAsValue() {
        assertThat(writer.write(new JavaScriptNumberLiteralNode("-4.2")), is("-4.2"));
    }
    
    @Test public void
    stringIsEnclosedInDoubleQuotes() {
        assertThat(writer.write(new JavaScriptStringLiteralNode("Dustbowl Dance")), is("\"Dustbowl Dance\""));
    }
    
    @Test public void
    stringsAreEscaped() {
        assertThat(writer.write(new JavaScriptStringLiteralNode("\"Invisible\nLight\"")), is("\"\\\"Invisible\\nLight\\\"\""));
    }
    
    @Test public void
    identifiersWriteOutIdentifiersVerbatim() {
        assertThat(writer.write(new JavaScriptIdentifierNode("this.is.the.life")), is("this.is.the.life"));
    }
}
