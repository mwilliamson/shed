package org.zwobble.shed.compiler.codegenerator;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptExpressionNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatementNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JavaScriptWriterTest {
    private final JavaScriptWriter writer = new JavaScriptWriter();
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Test public void
    booleanIsWrittenAsTrueOrFalseKeywords() {
        assertThat(writer.write(js.bool(true)), is("true"));
        assertThat(writer.write(js.bool(false)), is("false"));
    }
    
    @Test public void
    numberIsWrittenAsValue() {
        assertThat(writer.write(js.number("-4.2")), is("-4.2"));
    }
    
    @Test public void
    stringIsEnclosedInDoubleQuotes() {
        assertThat(writer.write(js.string("Dustbowl Dance")), is("\"Dustbowl Dance\""));
    }
    
    @Test public void
    stringsAreEscaped() {
        assertThat(writer.write(js.string("\"Invisible\nLight\"")), is("\"\\\"Invisible\\nLight\\\"\""));
    }
    
    @Test public void
    identifiersWriteOutIdentifiersVerbatim() {
        assertThat(writer.write(js.id("this.is.the.life")), is("this.is.the.life"));
    }
    
    @Test public void
    functionCallsIncludeObjectAndArguments() {
        assertThat(
            writer.write(js.call(js.id("_.map"), js.id("cushions"), js.id("embroidered"))),
            is("_.map(cushions, embroidered)")
        );
    }
    
    @Test public void
    anonymousFunctionsAreWrappedInParenthesesBeforeCall() {
        JavaScriptExpressionNode node = js.func(Collections.<String>emptyList(), Collections.<JavaScriptStatementNode>emptyList());
        assertThat(
            writer.write(js.call(node)),
            is("(function() {\n})()")
        );
    }
    
    @Test public void
    returnHasReturnKeywordWithValue() {
        assertThat(
            writer.write(js.ret(js.bool(true))),
            is("return true;")
        );
    }
    
    @Test public void
    variableDeclarationsIncludeVariableNameAndInitialValue() {
        assertThat(
            writer.write(js.var("theLoneliestNumber", js.number("1"))),
            is("var theLoneliestNumber = 1;")
        );
    }
    
    @Test public void
    listOfStatementsIsConvertedWithNewlinesBetweenStatements() {
        assertThat(
            writer.write(js.statements(js.var("it", js.string("what")), js.ret(js.bool(true)))),
            is("var it = \"what\";\nreturn true;")
        );
    }
    
    @Test public void
    functionDeclarationsIncludeArgumentsAndClosingBraceOnNewLine() {
        assertThat(
            writer.write(js.func(asList("telegraph", "road"), Collections.<JavaScriptStatementNode>emptyList())),
            is("function(telegraph, road) {\n}")
        );
    }
    
    @Test public void
    bodiesOfFunctionsAreIndented() {
        assertThat(
            writer.write(js.func(Collections.<String>emptyList(), asList(
                js.var("listeningTo", js.func(Collections.<String>emptyList(), asList((JavaScriptStatementNode)js.ret(js.string("Boys of Summer"))))),
                js.ret(js.call(js.id("listeningTo")))
            ))),
            is("function() {\n" +
               "    var listeningTo = function() {\n" +
               "        return \"Boys of Summer\";\n" +
               "    };\n" +
               "    return listeningTo();\n" +
               "}")
        );
    }
    
    @Test public void
    expressionStatementsAreAsExpressionButWithTrailingSemiColon() {
        assertThat(
            writer.write(js.expressionStatement(js.bool(true))),
            is("true;")
        );
    }
}
