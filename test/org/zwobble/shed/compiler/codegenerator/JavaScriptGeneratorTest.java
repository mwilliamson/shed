package org.zwobble.shed.compiler.codegenerator;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptExpressionNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNodes;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatementNode;
import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptStatements;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.BooleanLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.CallNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.IfThenElseStatementNode;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.LiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.MemberAccessNode;
import org.zwobble.shed.compiler.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.ObjectDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.StringLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeApplicationNode;
import org.zwobble.shed.compiler.parsing.nodes.UnitLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.parsing.nodes.WhileStatementNode;
import org.zwobble.shed.compiler.referenceresolution.ReferenceResolver;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.referenceresolution.ReferencesBuilder;
import org.zwobble.shed.compiler.typechecker.SimpleNodeLocations;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.codegenerator.JavaScriptGenerator.CORE_VALUES_OBJECT_NAME;

public class JavaScriptGeneratorTest {
    private final JavaScriptNodes js = new JavaScriptNodes();
    private final ReferenceResolver referenceResolver = new ReferenceResolver();
    private final NodeLocations nodeLocations = new SimpleNodeLocations();
    
    @Test public void
    booleanLiteralsAreConvertedToBoxedBooleans() {
        booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(true);
        booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(false);
    }
    
    private void booleanLiteralIsConvertedToBoxedBooleansWhenBooleanIs(boolean value) {
        BooleanLiteralNode source = new BooleanLiteralNode(value);
        assertGeneratedJavaScript(source, js.call(js.id("__core.Boolean"), js.bool(value)));
    }
    
    @Test public void
    numberLiteralsAreConvertedToBoxedNumbers() {
        NumberLiteralNode source = new NumberLiteralNode("4.2");
        assertGeneratedJavaScript(source, js.call(js.id("__core.Number"), js.number("4.2")));
    }
    
    @Test public void
    stringLiteralsAreConvertedToBoxedStrings() {
        StringLiteralNode source = new StringLiteralNode("Stop giving me verses");
        assertGeneratedJavaScript(source, js.call(js.id("__core.String"), js.string("Stop giving me verses")));
    }
    
    @Test public void
    unitLiteralsAreConvertedToUnitValue() {
        UnitLiteralNode source = Nodes.unit();
        assertGeneratedJavaScript(source, js.call(js.id("__core.Unit")));
    }
    
    @Test public void
    variableIdentifiersAreConvertedToJavaScriptIdentifier() {
        VariableIdentifierNode reference = Nodes.id("blah");
        ReferencesBuilder references = new ReferencesBuilder();
        references.addReference(reference, new GlobalDeclarationNode("blah"));
        assertGeneratedJavaScript(references.build(), reference, js.id("blah"));
    }
    
    @Test public void
    immutableVariableNodesAreConvertedToVariableDeclarations() {
        ImmutableVariableNode source = new ImmutableVariableNode("x", none(ExpressionNode.class), new BooleanLiteralNode(true));
        assertGeneratedJavaScript(source, js.var("x__1", generateLiteral(new BooleanLiteralNode(true))));
    }
    
    @Test public void
    mutableVariableNodesAreConvertedToVariableDeclarations() {
        MutableVariableNode source = new MutableVariableNode("x", none(ExpressionNode.class), new BooleanLiteralNode(true));
        assertGeneratedJavaScript(source, js.var("x__1", generateLiteral(new BooleanLiteralNode(true))));
    }
    
    @Test public void
    functionCallsWithNoArgumentsAreConverted() {
        VariableIdentifierNode reference = Nodes.id("now");
        ReferencesBuilder references = new ReferencesBuilder();
        references.addReference(reference, new GlobalDeclarationNode("now"));
        CallNode source = Nodes.call(reference);
        assertGeneratedJavaScript(references.build(), source, js.call(js.id("now")));
    }
    
    @Test public void
    functionCallsWithArgumentsAreConverted() {
        VariableIdentifierNode reference = Nodes.id("max");
        ReferencesBuilder references = new ReferencesBuilder();
        references.addReference(reference, new GlobalDeclarationNode("max"));
        
        NumberLiteralNode firstArgument = Nodes.number("2");
        NumberLiteralNode secondArgument = Nodes.number("8");
        CallNode source = Nodes.call(reference, firstArgument, secondArgument);
        assertGeneratedJavaScript(
            references.build(),
            source,
            js.call(js.id("max"), generateLiteral(firstArgument), generateLiteral(secondArgument))
        );
    }
    
    @Test public void
    shortLambdaExpressionWithoutArgumentsIsConvertedIntoJavaScriptAnonymousFunction() {
        ShortLambdaExpressionNode source = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        assertGeneratedJavaScript(
            source,
            js.func(
                Collections.<String>emptyList(),
                asList((JavaScriptStatementNode)js.ret(generateLiteral(new BooleanLiteralNode(true))))
            )
        );
    }
    
    @Test public void
    shortLambdaExpressionWithArgumentsIsConvertedIntoJavaScriptAnonymousFunction() {
        ShortLambdaExpressionNode source = new ShortLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("String")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Number"))
            ),
            none(ExpressionNode.class),
            new BooleanLiteralNode(true)
        );
        assertGeneratedJavaScript(
            source, 
            js.func(
                asList("name__1", "age__1"), 
                asList((JavaScriptStatementNode)js.ret(generateLiteral(new BooleanLiteralNode(true))))
            )
        );
    }
    
    @Test public void
    returnStatementIsConvertedToJavaScriptReturn() {
        ReturnNode source = new ReturnNode(new BooleanLiteralNode(true));
        assertGeneratedJavaScript(source, js.ret(generateLiteral(new BooleanLiteralNode(true))));
    }
    
    @Test public void
    longLambdaExpressionIsConvertedIntoJavaScriptAnonymousFunction() {
        ImmutableVariableNode variableNode = Nodes.immutableVar("x", new BooleanLiteralNode(true));
        ReturnNode returnNode = new ReturnNode(Nodes.number("42"));
        LongLambdaExpressionNode source = new LongLambdaExpressionNode(
            asList(
                new FormalArgumentNode("name", new VariableIdentifierNode("String")),
                new FormalArgumentNode("age", new VariableIdentifierNode("Number"))
            ),
            new VariableIdentifierNode("Number"),
            Nodes.block(variableNode, returnNode)
        );
        assertGeneratedJavaScript(
            source,
            js.func(asList("name__1", "age__1"), asList(
                js.var("x__1", generateLiteral(Nodes.bool(true))),
                js.ret(generateLiteral(Nodes.number("42")))
            ))
        );
    }
    
    @Test public void
    canGenerateJavaScriptForSourceFile() {
        PackageDeclarationNode packageDeclaration = new PackageDeclarationNode(asList("shed", "example"));
        ImportNode importNode = new ImportNode(asList("shed", "DateTime"));
        StatementNode statement = new ImmutableVariableNode("magic", Option.none(ExpressionNode.class), new NumberLiteralNode("42"));
        SourceNode source = new SourceNode(packageDeclaration, asList(importNode), asList(statement));
        
        JavaScriptGenerator generator = new JavaScriptGenerator(new IdentityModuleWrapper(), resolveReferences(source));
        
        assertThat(
            generator.generate(source, asList("String", "Number")),
            is((JavaScriptNode)js.statements(
                js.var("String", js.id(CORE_VALUES_OBJECT_NAME + ".String")),
                js.var("Number", js.id(CORE_VALUES_OBJECT_NAME + ".Number")),
                generator.generateStatement(statement)
            ))
        );
    }
    
    @Test public void
    publicDeclarationsAreExported() {
        PackageDeclarationNode packageDeclaration = new PackageDeclarationNode(asList("shed", "example"));
        StatementNode statement = Nodes.publik(Nodes.immutableVar("magic", Nodes.number("42")));
        SourceNode source = new SourceNode(packageDeclaration, Collections.<ImportNode>emptyList(), asList(statement));
        
        JavaScriptGenerator generator = new JavaScriptGenerator(new IdentityModuleWrapper(), resolveReferences(source));
        
        assertThat(
            generator.generate(source, Collections.<String>emptyList()),
            is((JavaScriptNode)js.statements(
                js.var("magic__1", generateLiteral(Nodes.number("42"))),
                js.expressionStatement(js.call(js.id("SHED.exportValue"), js.string("shed.example.magic"), js.id("magic__1")))
            ))
        );
    }
    
    @Test public void
    moduleIsWrappedUsingModuleWrapper() {
        JavaScriptModuleWrapper wrapper = new JavaScriptModuleWrapper() {
            @Override
            public JavaScriptNode wrap(
                PackageDeclarationNode packageDeclaration, Iterable<ImportNode> imports, JavaScriptStatements module, JavaScriptNamer namer
            ) {
                return js.func(Collections.<String>emptyList(), module.getStatements());
            }
        };
        
        PackageDeclarationNode packageDeclaration = new PackageDeclarationNode(asList("shed", "example"));
        StatementNode statement = new ImmutableVariableNode("magic", Option.none(ExpressionNode.class), new NumberLiteralNode("42"));
        SourceNode source = new SourceNode(packageDeclaration, Collections.<ImportNode>emptyList(), asList(statement));
        
        JavaScriptGenerator generator = new JavaScriptGenerator(wrapper, resolveReferences(source));
        
        assertThat(
            generator.generate(source, Collections.<String>emptyList()),
            is((JavaScriptNode)js.func(
                Collections.<String>emptyList(),
                asList(
                    generator.generateStatement(statement)
                )
            ))
        );
    }
    
    @Test public void
    expressionStatementsAreConvertedToJavaScriptExpressionStatements() {
        VariableIdentifierNode reference = Nodes.id("go");
        ReferencesBuilder references = new ReferencesBuilder();
        references.addReference(reference, new GlobalDeclarationNode("go"));
        
        ExpressionStatementNode source = Nodes.expressionStatement(Nodes.call(reference));
        assertGeneratedJavaScript(references.build(), source, js.expressionStatement(js.call(js.id("go"))));
    }
    
    @Test public void
    objectIsExpressedAsObjectLiteralReturnedFromImmediatelyCalledAnonymousFunction() {
        ImmutableVariableNode nameDeclaration = Nodes.immutableVar("name", Nodes.string("Bob"));
        ImmutableVariableNode ageDeclaration = Nodes.immutableVar("age", Nodes.number("22"));
        ObjectDeclarationNode source = new ObjectDeclarationNode(
            "person",
            Nodes.block(
                Nodes.publik(nameDeclaration),
                ageDeclaration
            )
        );
        assertGeneratedJavaScript(
            source, 
            js.var("person__1", js.call(
                js.func(
                    Collections.<String>emptyList(),
                    asList(
                        js.var("name__1", generateLiteral(Nodes.string("Bob"))),
                        js.var("age__1", generateLiteral(Nodes.number("22"))),
                        js.ret(js.object(ImmutableMap.<String, JavaScriptExpressionNode>of("name", js.id("name__1"))))
                    )
                )
            ))
        );
    }
    
    @Test public void
    memberAccessIsConvertedToJavaScriptPropertyAccess() {
        VariableIdentifierNode reference = Nodes.id("ball");
        MemberAccessNode source = Nodes.member(reference, "confusion");
        ReferencesBuilder references = new ReferencesBuilder();
        references.addReference(reference, new GlobalDeclarationNode("ball"));
        assertGeneratedJavaScript(references.build(), source, js.propertyAccess(js.id("ball"), "confusion"));
    }
    
    @Test public void
    typeApplicationIsConvertedToFunctionCall() {
        VariableIdentifierNode functionReference = Nodes.id("Function1");
        VariableIdentifierNode numberReference = Nodes.id("Number");
        VariableIdentifierNode stringReference = Nodes.id("String");
        ReferencesBuilder references = new ReferencesBuilder();
        references.addReference(functionReference, new GlobalDeclarationNode("Function1"));
        references.addReference(numberReference, new GlobalDeclarationNode("Number"));
        references.addReference(stringReference, new GlobalDeclarationNode("String"));
        
        TypeApplicationNode source = Nodes.typeApply(functionReference, numberReference, stringReference);
        assertGeneratedJavaScript(references.build(), source, js.call(js.id("Function1"), js.id("Number"), js.id("String")));
    }
    
    @Test public void
    ifElseIsConvertedToIfElse() {
        StatementNode ifTrue = Nodes.returnStatement(Nodes.number("6"));
        StatementNode ifFalse = Nodes.returnStatement(Nodes.number("8"));
        IfThenElseStatementNode source =  Nodes.ifThenElse(Nodes.bool(true), Nodes.block(ifTrue), Nodes.block(ifFalse));
        assertGeneratedJavaScript(source, js.ifThenElse(
            generateLiteral(Nodes.bool(true)),
            Arrays.<JavaScriptStatementNode>asList(js.ret(generateLiteral(Nodes.number("6")))),
            Arrays.<JavaScriptStatementNode>asList(js.ret(generateLiteral(Nodes.number("8"))))
        ));
    }
    
    @Test public void
    variablesAreRenamedToBeUnique() {
        IfThenElseStatementNode source =  Nodes.ifThenElse(
            Nodes.bool(true), 
            Nodes.block(Nodes.immutableVar("x", Nodes.number("5"))),
            Nodes.block(Nodes.immutableVar("x", Nodes.number("8")))
        );
        assertGeneratedJavaScript(source, js.ifThenElse(
            generateLiteral(Nodes.bool(true)),
            Arrays.<JavaScriptStatementNode>asList(js.var("x__1", generateLiteral(Nodes.number("5")))),
            Arrays.<JavaScriptStatementNode>asList(js.var("x__2", generateLiteral(Nodes.number("8"))))
        ));
    }
    
    @Test public void
    whileLoopIsConvertedToWhileLoopWithBodyAsFunction() {
        StatementNode body = Nodes.returnStatement(Nodes.number("8"));
        WhileStatementNode source =  Nodes.whileLoop(Nodes.bool(true), Nodes.block(body));
        assertGeneratedJavaScript(source, js.statements(
            js.var("__tmp_loopBody__1", js.func(
                Collections.<String>emptyList(),
                Arrays.<JavaScriptStatementNode>asList(js.ret(generateLiteral(Nodes.number("8"))))
            )),
            js.whileLoop(
                generateLiteral(Nodes.bool(true)),
                js.var("__tmp_loopBodyResult__1", js.call(js.id("__tmp_loopBody__1"))),
                js.ifThen(
                    js.operator("!==", js.id("__tmp_loopBodyResult__1"), js.undefined()),
                    js.ret(js.id("__tmp_loopBodyResult__1"))
                )
            )
        ));
    }
    
    private void assertGeneratedJavaScript(ExpressionNode source, JavaScriptNode expectedJavaScript) {
        assertGeneratedJavaScript(resolveReferences(source), source, expectedJavaScript);
    }
    
    private void assertGeneratedJavaScript(References references, ExpressionNode source, JavaScriptNode expectedJavaScript) {
        assertThat(generator(references).generateExpression(source), is(expectedJavaScript));
    }
    
    private void assertGeneratedJavaScript(StatementNode source, JavaScriptNode expectedJavaScript) {
        assertGeneratedJavaScript(resolveReferences(source), source, expectedJavaScript);
    }
    
    private void assertGeneratedJavaScript(References references, StatementNode source, JavaScriptNode expectedJavaScript) {
        assertThat(generator(references).generateStatement(source), is(expectedJavaScript));
    }
    
    private References resolveReferences(SyntaxNode source) {
        return referenceResolver
            .resolveReferences(source, nodeLocations, Collections.<String, GlobalDeclarationNode>emptyMap())
            .getReferences();
    }
    
    private JavaScriptGenerator generator(References references) {
        return new JavaScriptGenerator(new IdentityModuleWrapper(), references);
    }
    
    private JavaScriptExpressionNode generateLiteral(LiteralNode literalNode) {
        return generator(null).generateExpression(literalNode);
    }
}
