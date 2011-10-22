package org.zwobble.shed.compiler.referenceresolution;

import java.util.Arrays;
import java.util.Collections;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.CompilerTesting;
import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.BlockNode;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.FormalArgumentNode;
import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.LongLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.Nodes;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.ReturnNode;
import org.zwobble.shed.compiler.parsing.nodes.ShortLambdaExpressionNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.VariableIdentifierNode;
import org.zwobble.shed.compiler.typechecker.CoreModule;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.isFailureWithErrors;
import static org.zwobble.shed.compiler.CompilerTesting.isSuccess;

public class ReferenceResolverTest {
    private static final References EMPTY_REFERENCES = new References(ImmutableMap.<Identity<VariableIdentifierNode>, Identity<Declaration>>of());
    private static final ReferenceResolverResult EMPTY_RESULT = ReferenceResolverResult.build(EMPTY_REFERENCES, Collections.<CompilerError>emptyList());
    private final ReferenceResolver resolver = new ReferenceResolver();
    
    @Test public void
    booleanLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.bool(true)), is(EMPTY_RESULT));
    }
    
    @Test public void
    stringLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.string("blah")), is(EMPTY_RESULT));
    }
    
    @Test public void
    numberLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.number("42")), is(EMPTY_RESULT));
    }
    
    @Test public void
    unitLiteralNodeAddsNothingToScope() {
        assertThat(resolveReferences(Nodes.unit()), is(EMPTY_RESULT));
    }
    
    @Test public void
    canReferToImmutableReferencesInSameBlock() {
        VariableIdentifierNode reference = Nodes.id("x");
        DeclarationNode declaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(declaration, Nodes.expressionStatement(reference));
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }
    
    @Test public void
    referencesInVariableIntialisersAreResolved() {
        VariableIdentifierNode reference = Nodes.id("x");
        DeclarationNode declaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(declaration, Nodes.immutableVar("y", reference));
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }
    
    @Test public void
    referencesInPublicVariableIntialisersAreResolved() {
        VariableIdentifierNode reference = Nodes.id("x");
        DeclarationNode declaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(declaration, Nodes.publik(Nodes.immutableVar("y", reference)));
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }
    
    @Test public void
    referencesInFunctionCallAreResolved() {
        DeclarationNode functionDeclaration = Nodes.immutableVar("func", Nodes.string("go"));
        DeclarationNode argumentDeclaration = Nodes.immutableVar("x", Nodes.number("42"));

        VariableIdentifierNode functionReference = Nodes.id("func");
        VariableIdentifierNode argumentReference = Nodes.id("x");
        
        SyntaxNode source = Nodes.block(
            functionDeclaration, 
            argumentDeclaration, 
            Nodes.expressionStatement(Nodes.call(functionReference, argumentReference))
        );
        assertThat(resolveReferences(source), hasReference(functionReference, functionDeclaration));
        assertThat(resolveReferences(source), hasReference(argumentReference, argumentDeclaration));
    }
    
    @Test public void
    referencesInMemberAccessAreResolved() {
        VariableIdentifierNode reference = Nodes.id("x");
        DeclarationNode declaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(declaration, Nodes.expressionStatement(Nodes.member(reference, "abs")));
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }

    @Test public void
    referringToVariablesNotInScopeAddsError() {
        VariableIdentifierNode reference = Nodes.id("height");
        assertThat(resolveReferences(reference), isFailureWithErrors(new VariableNotInScopeError("height")));
    }

    @Test public void
    declaringVariableWithSameNameInSameScopeAddsError() {
        DeclarationNode firstDeclaration = Nodes.immutableVar("x", Nodes.number("42"));
        DeclarationNode secondDeclaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(firstDeclaration, secondDeclaration);
        assertThat(resolveReferences(source), isFailureWithErrors(new DuplicateIdentifierError("x")));
    }

    @Test public void
    shortLambdaExpressionAddsArgumentsToScope() {
        FormalArgumentNode firstArgument = new FormalArgumentNode("first", Nodes.id("String"));
        VariableIdentifierNode reference = Nodes.id("first");
        SyntaxNode source = new ShortLambdaExpressionNode(
            asList(firstArgument, new FormalArgumentNode("second", Nodes.id("Double"))),
            Option.<ExpressionNode>none(),
            reference
        );
        assertThat(resolveReferences(source), hasReference(reference, firstArgument));
    }

    @Test public void
    longLambdaExpressionAddsArgumentsToScope() {
        FormalArgumentNode firstArgument = new FormalArgumentNode("first", Nodes.id("String"));
        VariableIdentifierNode reference = Nodes.id("first");
        SyntaxNode source = new LongLambdaExpressionNode(
            asList(firstArgument, new FormalArgumentNode("second", Nodes.id("Double"))),
            Nodes.id("String"),
            Nodes.block(Nodes.returnStatement(reference))
        );
        assertThat(resolveReferences(source), hasReference(reference, firstArgument));
    }

    @Test public void
    canReferToVariableInParentScope() {
        VariableIdentifierNode reference = Nodes.id("theNight");
        VariableDeclarationNode declaration = Nodes.immutableVar("theNight", Nodes.string("feelsMySoul"));
        SyntaxNode source = Nodes.block(
            declaration,
            Nodes.expressionStatement(new ShortLambdaExpressionNode(
                asList(new FormalArgumentNode("first", Nodes.id("String")), new FormalArgumentNode("second", Nodes.id("Double"))),
                Option.<ExpressionNode>none(),
                reference
            ))
        );
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }

    @Test public void
    canOverrideDeclarationOfVariableInSubScope() {
        SyntaxNode source = Nodes.block(
            Nodes.immutableVar("wind", Nodes.string("decide")),
            Nodes.expressionStatement(new LongLambdaExpressionNode(
                Collections.<FormalArgumentNode>emptyList(),
                Nodes.id("String"),
                Nodes.block(
                    Nodes.immutableVar("wind", Nodes.bool(false))
                )
            ))
        );
        assertThat(resolveReferences(source), isSuccess());
    }

    @Test public void
    alwaysUseDeclarationInCurrentScopeEvenIfDeclarationIsLaterThanReference() {
        VariableIdentifierNode reference = Nodes.id("wind");
        VariableDeclarationNode declaration = Nodes.immutableVar("wind", Nodes.bool(false));
        SyntaxNode source = Nodes.block(
            Nodes.immutableVar("wind", Nodes.string("decide")),
            Nodes.expressionStatement(new LongLambdaExpressionNode(
                Collections.<FormalArgumentNode>emptyList(),
                Nodes.id("String"),
                Nodes.block(
                    Nodes.expressionStatement(reference),
                    declaration
                )
            ))
        );
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }

    @Test public void
    typeExpressionsOfFormalArgumentsAreLookedUp() {
        VariableIdentifierNode typeReference = Nodes.id("String");
        SyntaxNode source = new ShortLambdaExpressionNode(
            asList(new FormalArgumentNode("first", typeReference), new FormalArgumentNode("second", Nodes.id("Double"))),
            Option.<ExpressionNode>none(),
            Nodes.id("first")
        );
        assertThat(resolveReferences(source), hasReference(typeReference, CoreModule.GLOBAL_DECLARATIONS.get("String")));
    }

    @Test public void
    typeExpressionsOfShortLambdaReturnTypeAreLookedUp() {
        VariableIdentifierNode typeReference = Nodes.id("String");
        SyntaxNode source = new ShortLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            Option.<ExpressionNode>some(typeReference),
            Nodes.string("Falling from your mouth")
        );
        assertThat(resolveReferences(source), hasReference(typeReference, CoreModule.GLOBAL_DECLARATIONS.get("String")));
    }

    @Test public void
    typeExpressionsOfLongLambdaReturnTypeAreLookedUp() {
        VariableIdentifierNode typeReference = Nodes.id("String");
        SyntaxNode source = new LongLambdaExpressionNode(
            Collections.<FormalArgumentNode>emptyList(),
            typeReference,
            Nodes.block(Nodes.returnStatement(Nodes.string("Falling from your mouth")))
        );
        assertThat(resolveReferences(source), hasReference(typeReference, CoreModule.GLOBAL_DECLARATIONS.get("String")));
    }

    @Test public void
    canDefineVariableWithSameNameInBothBranchesOfIfElse() {
        SyntaxNode source = Nodes.ifThenElse(
            Nodes.bool(true),
            Nodes.block(Nodes.immutableVar("singItFor", Nodes.string("The boys"))),
            Nodes.block(Nodes.immutableVar("singItFor", Nodes.string("The girls")))
        );
        assertThat(resolveReferences(source), isSuccess());
    }

    @Test public void
    referencesAreResolvedForConditionAndBranchesOfIfElse() {
        VariableDeclarationNode booleanDeclaration = Nodes.immutableVar("go", Nodes.bool(true));
        VariableDeclarationNode ifTrueDeclaration = Nodes.immutableVar("boys", Nodes.string("The boys"));
        VariableDeclarationNode ifFalseDeclaration = Nodes.immutableVar("girls", Nodes.string("The girls"));
        
        VariableIdentifierNode booleanReference = Nodes.id("go");
        VariableIdentifierNode ifTrueReference = Nodes.id("boys");
        VariableIdentifierNode ifFalseReference = Nodes.id("girls");
        
        SyntaxNode source = Nodes.block(
            booleanDeclaration,
            ifTrueDeclaration,
            ifFalseDeclaration,
            Nodes.ifThenElse(
                booleanReference,
                Nodes.block(Nodes.expressionStatement(ifTrueReference)),
                Nodes.block(Nodes.expressionStatement(ifFalseReference))
            )
        );
        ReferenceResolverResult references = resolveReferences(source);
        assertThat(references, hasReference(booleanReference, booleanDeclaration));
        assertThat(references, hasReference(ifTrueReference, ifTrueDeclaration));
        assertThat(resolveReferences(source), hasReference(ifFalseReference, ifFalseDeclaration));
    }

    @Test public void
    branchOfIfElseStatementCannotDeclareVariableDeclaredInParentBlock() {
        SyntaxNode source = Nodes.block(
            Nodes.immutableVar("go", Nodes.bool(true)),
            Nodes.ifThenElse(
                Nodes.id("go"),
                Nodes.block(Nodes.immutableVar("go", Nodes.string("The boys"))),
                Nodes.block()
            )
        );
        assertThat(resolveReferences(source), isFailureWithErrors(new DuplicateIdentifierError("go")));
    }

    @Test public void
    bodyOfSourceNodeCanReferToImports() {
        ImportNode declaration = new ImportNode(asList("shed", "collections", "List"));
        VariableIdentifierNode reference = Nodes.id("List");
        SyntaxNode source = new SourceNode(
            new PackageDeclarationNode(asList("shed", "example")),
            asList(declaration),
            Arrays.<StatementNode>asList(Nodes.expressionStatement(reference))
        );
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }
    
    @Test public void
    referencesInObjectDeclarationAreResolved() {
        VariableIdentifierNode reference = Nodes.id("x");
        DeclarationNode declaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(declaration, Nodes.object("bob", Nodes.block(Nodes.expressionStatement(reference))));
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }
    
    @Test public void
    objectDeclarationBodyIsInNewScope() {
        DeclarationNode declaration = Nodes.immutableVar("x", Nodes.number("42"));
        SyntaxNode source = Nodes.block(declaration, Nodes.object("bob", Nodes.block(Nodes.immutableVar("x", Nodes.number("43")))));
        assertThat(resolveReferences(source), isSuccess());
    }
    
    @Test public void
    objectDeclarationAddsItselfToScope() {
        DeclarationNode declaration = Nodes.object("bob", Nodes.block());
        VariableIdentifierNode reference = Nodes.id("bob");
        SyntaxNode source = Nodes.block(declaration, Nodes.expressionStatement(reference));
        assertThat(resolveReferences(source), hasReference(reference, declaration));
    }
    
    @Test public void
    referencesInTypeAplicationAreResolved() {
        DeclarationNode listDeclaration = Nodes.object("List", Nodes.block());
        VariableIdentifierNode listReference = Nodes.id("List");
        VariableIdentifierNode numberReference = Nodes.id("Double");
        SyntaxNode source = Nodes.block(listDeclaration, Nodes.expressionStatement(Nodes.typeApply(listReference, numberReference)));
        assertThat(resolveReferences(source), hasReference(listReference, listDeclaration));
        assertThat(resolveReferences(source), hasReference(numberReference, CoreModule.GLOBAL_DECLARATIONS.get("Double")));
    }
    
    @Test public void
    referencesAreResolvedForConditionAndBodyOfWhileLoop() {
        VariableDeclarationNode booleanDeclaration = Nodes.immutableVar("go", Nodes.bool(true));
        VariableDeclarationNode bodyDeclaration = Nodes.immutableVar("boys", Nodes.string("The boys"));
        
        VariableIdentifierNode booleanReference = Nodes.id("go");
        VariableIdentifierNode bodyReference = Nodes.id("boys");
        
        SyntaxNode source = Nodes.block(
            booleanDeclaration,
            Nodes.whileLoop(
                booleanReference,
                Nodes.block(bodyDeclaration, Nodes.expressionStatement(bodyReference))
            )
        );
        ReferenceResolverResult references = resolveReferences(source);
        assertThat(references, hasReference(booleanReference, booleanDeclaration));
        assertThat(references, hasReference(bodyReference, bodyDeclaration));
    }
    
    @Test public void
    referencesAreResolvedForBothSidesOfAnAssignment() {
        VariableDeclarationNode leftDeclaration = Nodes.immutableVar("x", Nodes.number("347"));
        VariableDeclarationNode rightDeclaration = Nodes.immutableVar("y", Nodes.number("348"));
        
        VariableIdentifierNode leftReference = Nodes.id("x");
        VariableIdentifierNode rightReference = Nodes.id("y");
        
        SyntaxNode source = Nodes.block(
            leftDeclaration, rightDeclaration,
            Nodes.expressionStatement(Nodes.assign(leftReference, rightReference))
        );
        ReferenceResolverResult references = resolveReferences(source);
        assertThat(references, hasReference(leftReference, leftDeclaration));
        assertThat(references, hasReference(rightReference, rightDeclaration));
    }
    
    @Test public void
    functionDeclarationAddsArgumentsToScope() {
        FormalArgumentNode firstArgument = new FormalArgumentNode("first", Nodes.id("String"));
        VariableIdentifierNode reference = Nodes.id("first");
        SyntaxNode source = new FunctionDeclarationNode(
            "go",
            asList(firstArgument, new FormalArgumentNode("second", Nodes.id("Double"))),
            Nodes.id("String"),
            Nodes.block(Nodes.returnStatement(reference))
        );
        assertThat(resolveReferences(source), hasReference(reference, firstArgument));
    }
    
    @Test public void
    functionDeclarationCanReferToItself() {
        VariableIdentifierNode functionReference = Nodes.id("now");
        FunctionDeclarationNode functionDeclaration = new FunctionDeclarationNode(
            "now",
            Collections.<FormalArgumentNode>emptyList(),
            Nodes.id("Double"),
            Nodes.block(Nodes.returnStatement(functionReference))
        );
        assertThat(resolveReferences(functionDeclaration), hasReference(functionReference, functionDeclaration));
    }
    
    @Test public void
    functionDeclarationsArePulledToTheTopOfBlock() {
        ReturnNode returnNode = new ReturnNode(Nodes.number("42"));
        FunctionDeclarationNode functionDeclaration = new FunctionDeclarationNode(
            "magic",
            Collections.<FormalArgumentNode>emptyList(),
            new VariableIdentifierNode("Double"),
            Nodes.block(returnNode)
        );
        VariableIdentifierNode functionReference = Nodes.id("magic");
        StatementNode functionCall = Nodes.expressionStatement(Nodes.call(functionReference));
        BlockNode source = Nodes.block(functionCall, functionDeclaration);
        assertThat(resolveReferences(source), hasReference(functionReference, functionDeclaration));
    }

    private ReferenceResolverResult resolveReferences(SyntaxNode node) {
        return resolver.resolveReferences(node, CoreModule.GLOBAL_DECLARATIONS);
    }
    
    private Matcher<ReferenceResolverResult> hasReference(final VariableIdentifierNode reference, final Declaration declaration) {
        return new TypeSafeDiagnosingMatcher<ReferenceResolverResult>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(reference.getIdentifier() + " should refer to " + declaration);
            }

            @Override
            protected boolean matchesSafely(ReferenceResolverResult result, Description mismatchDescription) {
                if (result.isSuccess()) {
                    References references = result.getReferences();
                    Declaration actualReferent = references.findReferent(reference);
                    if (actualReferent == declaration) {
                        return true;
                    } else {
                        mismatchDescription.appendText("referent was " + actualReferent + " [references: " + references + "]");
                        return false;
                    }
                } else {
                    mismatchDescription.appendText("result was failure: " + CompilerTesting.errorDescriptions(result));
                    return false;
                }
            }
        };
    }
}
