package org.zwobble.shed.compiler.codegenerator;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.Option.none;

public class JavaScriptGeneratorTest {
    private final JavaScriptGenerator generator = new JavaScriptGenerator(null, new IdentityModuleWrapper());
    private final JavaScriptNodes js = new JavaScriptNodes();
    
    @Test public void
    booleanLiteralsAreConvertedToBoxedBooleans() {
        booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(true);
        booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(false);
    }
    
    private void booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(boolean value) {
        BooleanLiteralNode source = new BooleanLiteralNode(value);
        assertGeneratedJavaScript(source, js.call(js.id("__shed.Boolean"), js.bool(value)));
    }
    
    @Test public void
    numberLiteralsAreConvertedToBoxedNumbers() {
        NumberLiteralNode source = new NumberLiteralNode("4.2");
        assertGeneratedJavaScript(source, js.call(js.id("__shed.Number"), js.number("4.2")));
    }
    
    @Test public void
    stringLiteralsAreConvertedToBoxedStrings() {
        StringLiteralNode source = new StringLiteralNode("Stop giving me verses");
        assertGeneratedJavaScript(source, js.call(js.id("__shed.String"), js.string("Stop giving me verses")));
    }
    
    @Test public void
    immutableVariableNodesAreConvertedToVariableDeclarations() {
        ImmutableVariableNode source = new ImmutableVariableNode("x", none(TypeReferenceNode.class), new BooleanLiteralNode(true));
        assertGeneratedJavaScript(source, js.var("x", generator.generate(new BooleanLiteralNode(true))));
    }
    
    @Test public void
    mutableVariableNodesAreConvertedToVariableDeclarations() {
        MutableVariableNode source = new MutableVariableNode("x", none(TypeReferenceNode.class), new BooleanLiteralNode(true));
        assertGeneratedJavaScript(source, js.var("x", generator.generate(new BooleanLiteralNode(true))));
    }
    
    @Test public void
    shortLambdaExpressionWithoutArgumentsIsConvertedIntoJavaScriptAnonymousFunction() {
        ShortLambdaExpressionNode source = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(TypeReferenceNode.class),
            new BooleanLiteralNode(true)
        );
        assertGeneratedJavaScript(source, js.func(Collections.<String>emptyList(), asList(generator.generate(new BooleanLiteralNode(true)))));
    }
    
    @Test public void
    shortLambdaExpressionWithArgumentsIsConvertedIntoJavaScriptAnonymousFunction() {
        ShortLambdaExpressionNode source = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new TypeIdentifierNode("String")),
                new FormalArgumentNode("age", new TypeIdentifierNode("Number"))
            ),
            none(TypeReferenceNode.class),
            new BooleanLiteralNode(true)
        );
        assertGeneratedJavaScript(source, js.func(asList("name", "age"), asList(generator.generate(new BooleanLiteralNode(true)))));
    }
    
    @Test public void
    returnStatementIsConvertedToJavaScriptReturn() {
        ReturnNode source = new ReturnNode(new BooleanLiteralNode(true));
        assertGeneratedJavaScript(source, js.ret(generator.generate(new BooleanLiteralNode(true))));
    }
    
    @Test public void
    longLambdaExpressionIsConvertedIntoJavaScriptAnonymousFunction() {
        ImmutableVariableNode variableNode = new ImmutableVariableNode("x", none(TypeReferenceNode.class), new BooleanLiteralNode(true));
        ReturnNode returnNode = new ReturnNode(new NumberLiteralNode("42"));
        LongLambdaExpressionNode source = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new TypeIdentifierNode("String")),
                new FormalArgumentNode("age", new TypeIdentifierNode("Number"))
            ),
            new TypeIdentifierNode("Number"),
            asList(variableNode, returnNode)
        );
        assertGeneratedJavaScript(
            source,
            js.func(asList("name", "age"), asList(generator.generate(variableNode), generator.generate(returnNode)))
        );
    }
    
    @Test public void
    canGenerateJavaScriptForSourceFile() {
        JavaScriptImportGenerator importGenerator = new NodeJsImportGenerator();
        JavaScriptGenerator generator = new JavaScriptGenerator(importGenerator, new IdentityModuleWrapper());
        
        PackageDeclarationNode packageDeclaration = new PackageDeclarationNode(asList("shed", "example"));
        ImportNode importNode = new ImportNode(asList("shed", "DateTime"));
        StatementNode statement = new ImmutableVariableNode("magic", Option.none(TypeReferenceNode.class), new NumberLiteralNode("42"));
        SourceNode source = new SourceNode(packageDeclaration, asList(importNode), asList(statement));
        assertThat(
            generator.generate(source),
            is((JavaScriptNode)js.statements(
                js.var("__shed", importGenerator.generateExpression(packageDeclaration, JavaScriptGenerator.CORE_TYPES_IMPORT_NODE)),
                js.var("DateTime", importGenerator.generateExpression(packageDeclaration, importNode)),
                generator.generate(statement)
            ))
        );
    }
    
    @Test public void
    moduleIsWrappedUsingModuleWrapper() {
        JavaScriptModuleWrapper wrapper = new JavaScriptModuleWrapper() {
            @Override
            public JavaScriptNode wrap(JavaScriptNode module) {
                return js.func(Collections.<String>emptyList(), asList(module));
            }
        };
        
        JavaScriptImportGenerator importGenerator = new NodeJsImportGenerator();
        JavaScriptGenerator generator = new JavaScriptGenerator(importGenerator, wrapper);
        
        PackageDeclarationNode packageDeclaration = new PackageDeclarationNode(asList("shed", "example"));
        StatementNode statement = new ImmutableVariableNode("magic", Option.none(TypeReferenceNode.class), new NumberLiteralNode("42"));
        SourceNode source = new SourceNode(packageDeclaration, Collections.<ImportNode>emptyList(), asList(statement));
        assertThat(
            generator.generate(source),
            is((JavaScriptNode)js.func(
                Collections.<String>emptyList(),
                asList((JavaScriptNode)js.statements(
                    js.var("__shed", importGenerator.generateExpression(packageDeclaration, JavaScriptGenerator.CORE_TYPES_IMPORT_NODE)),
                    generator.generate(statement)
                ))
            ))
        );
    }
    
    private void assertGeneratedJavaScript(SyntaxNode source, JavaScriptNode expectedJavaScript) {
        assertThat(generator.generate(source), is(expectedJavaScript));
    }
}
