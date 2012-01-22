package org.zwobble.shed.compiler.parsing;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.zwobble.shed.compiler.parsing.ParserTesting.isSuccessWithNode;
import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

public class StatementsTest {
    @Test public void
    canDeclareImmutableVariables() {
        assertThat(
            Statements.immutableVariable().parse(tokens("val magic = 42;")),
            isSuccessWithNode(Nodes.immutableVar("magic", new NumberLiteralNode("42")))
        );
    }
    
    @Test public void
    canDeclareImmutableVariablesWithExplicitType() {
        assertThat(
            Statements.immutableVariable().parse(tokens("val magic : Integer = 42;")),
            isSuccessWithNode(Nodes.immutableVar("magic", new VariableIdentifierNode("Integer"), new NumberLiteralNode("42")))
        );
    }
    
    @Test public void
    canDeclareMutableVariables() {
        assertThat(
            Statements.mutableVariable().parse(tokens("var magic = 42;")),
            isSuccessWithNode(Nodes.mutableVar("magic", new NumberLiteralNode("42")))
        );
    }
    
    @Test public void
    canDeclareMutableVariablesWithExplicitType() {
        assertThat(
            Statements.mutableVariable().parse(tokens("var magic : Integer = 42;")),
            isSuccessWithNode(Nodes.mutableVar("magic", new VariableIdentifierNode("Integer"), new NumberLiteralNode("42")))
        );
    }
    
    @Test public void
    canUseExpressionsFollowedByStatementTerminatorAsStatements() {
        assertThat(
            Statements.statement().parse(tokens("alert(\"Full Circle\");")),
            isSuccessWithNode(Nodes.expressionStatement(Nodes.call(Nodes.id("alert"), Nodes.string("Full Circle"))))
        );
    }
    
    @Test public void
    canDeclareAnEmptyObject() {
        assertThat(
            Statements.statement().parse(tokens("object browser { }")),
            isSuccessWithNode(Nodes.object("browser", Nodes.block()))
        );
    }
    
    @Test public void
    canDeclareObjectWithStatements() {
        assertThat(
            Statements.statement().parse(tokens("object browser { val x = 1; }")),
            isSuccessWithNode(Nodes.object("browser", Nodes.block(Statements.statement().parse(tokens("val x = 1;")).get())))
        );
    }
    
    @Test public void
    canDeclareObjectWithSuperTypes() {
        assertThat(
            Statements.statement().parse(tokens("object browser : Openable { }")),
            isSuccessWithNode(Nodes.object("browser", asList((ExpressionNode)Nodes.id("Openable")), Nodes.block()))
        );
    }
    
    @Test public void
    canDeclareAnEmptyClass() {
        assertThat(
            Statements.statement().parse(tokens("class Browser() { }")),
            isSuccessWithNode(Nodes.clazz("Browser", Nodes.formalArguments(), Nodes.block()))
        );
    }
    
    @Test public void
    canDeclareClassWithArguments() {
        assertThat(
            Statements.statement().parse(tokens("class Browser(version: Double) { }")),
            isSuccessWithNode(Nodes.clazz("Browser", asList(Nodes.formalArgument("version", Nodes.id("Double"))), Nodes.block()))
        );
    }
    
    @Test public void
    canDeclareClassWithBody() {
        assertThat(
            Statements.statement().parse(tokens("class Browser() { val version = 42; }")),
            isSuccessWithNode(Nodes.clazz("Browser", Nodes.formalArguments(), Nodes.block(Nodes.immutableVar("version", Nodes.number("42")))))
        );
    }
    
    @Test public void
    canDeclareClassWithSuperTypes() {
        assertThat(
            Statements.statement().parse(tokens("class Mosaic() <: Browser { }")),
            isSuccessWithNode(Nodes.clazz("Mosaic", Nodes.formalArguments(), asList((ExpressionNode)Nodes.id("Browser")), Nodes.block()))
        );
    }
    
    @Test public void
    canDeclareClassWithFormalTypeParameters() {
        assertThat(
            Statements.statement().parse(tokens("class Dictionary[K, V]() { }")),
            isSuccessWithNode(Nodes.clazz(
                "Dictionary",
                Nodes.formalTypeParameters(Nodes.formalTypeParameter("K"), Nodes.formalTypeParameter("V")),
                Nodes.formalArguments(),
                Nodes.block()
            ))
        );
    }
    
    @Test public void
    canDeclarePublicObjects() {
        assertThat(
            Statements.statement().parse(tokens("public object browser { }")),
            isSuccessWithNode(Nodes.publik(Nodes.object("browser", Nodes.block())))
        );
    }
    
    @Test public void
    canDeclarePublicVariables() {
        assertThat(
            Statements.statement().parse(tokens("public val magic = 42;")),
            isSuccessWithNode(Nodes.publik(Nodes.immutableVar("magic", Nodes.number("42"))))
        );
    }
    
    @Test public void
    canParseIfElseStatement() {
        assertThat(
            Statements.statement().parse(tokens("if isMorning { eatCereal(); } else { eatPudding(); }")),
            isSuccessWithNode(Nodes.ifThenElse(
                Nodes.id("isMorning"),
                Nodes.block(Nodes.expressionStatement(Nodes.call(Nodes.id("eatCereal")))),
                Nodes.block(Nodes.expressionStatement(Nodes.call(Nodes.id("eatPudding"))))
            ))
        );
    }
    
    @Test public void
    canParseIfStatementWithoutElse() {
        assertThat(
            Statements.statement().parse(tokens("if isMorning { eatCereal(); }")),
            isSuccessWithNode(Nodes.ifThen(
                Nodes.id("isMorning"),
                Nodes.block(Nodes.expressionStatement(Nodes.call(Nodes.id("eatCereal"))))
            ))
        );
    }
    
    @Test public void
    canParseWhileStatement() {
        assertThat(
            Statements.statement().parse(tokens("while true { go(); }")),
            isSuccessWithNode(Nodes.whileLoop(
                Nodes.bool(true),
                Nodes.block(Nodes.expressionStatement(Nodes.call(Nodes.id("go"))))
            ))
        );
    }
    
    @Test public void
    canParseFunctionDeclarations() {
        assertThat(
            Statements.statement().parse(tokens("fun repeat(str: String, times: Double): String { return \"\"; }")),
            isSuccessWithNode(Nodes.func(
                "repeat",
                asList(Nodes.formalArgument("str", Nodes.id("String")), Nodes.formalArgument("times", Nodes.id("Double"))),
                Nodes.id("String"),
                Nodes.block(Nodes.returnStatement(Nodes.string("")))
            ))
        );
    }
    
    @Test public void
    canParseFunctionDeclarationsWithTypeParameters() {
        assertThat(
            Statements.statement().parse(tokens("fun first[T, U](t: T, u: U) : T { return t; }")),
            isSuccessWithNode(Nodes.func(
                "first",
                Nodes.formalTypeParameters(Nodes.formalTypeParameter("T"), Nodes.formalTypeParameter("U")),
                asList(Nodes.formalArgument("t", Nodes.id("T")), Nodes.formalArgument("u", Nodes.id("U"))),
                Nodes.id("T"),
                Nodes.block(Nodes.returnStatement(Nodes.id("t")))
            ))
        );
    }
    
    @Test public void
    canParseEmptyInterfaceDeclarations() {
        assertThat(
            Statements.statement().parse(tokens("interface Instrument { }")),
            isSuccessWithNode(Nodes.interfaceDeclaration("Instrument", Nodes.interfaceBody()))
        );
    }
    
    @Test public void
    canParseInterfaceDeclarationsWithFunctionSignatureDeclaration() {
        assertThat(
            Statements.statement().parse(tokens("interface Instrument { fun repeat(str: String, times: Double): String; }")),
            isSuccessWithNode(Nodes.interfaceDeclaration(
                "Instrument",
                Nodes.interfaceBody(Nodes.funcSignature(
                    "repeat",
                    asList(Nodes.formalArgument("str", Nodes.id("String")), Nodes.formalArgument("times", Nodes.id("Double"))),
                    Nodes.id("String")
                ))
            ))
        );
    }
}
