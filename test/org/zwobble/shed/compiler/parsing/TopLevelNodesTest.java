package org.zwobble.shed.compiler.parsing;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.compiler.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.compiler.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.parsing.nodes.TypeReferenceNode;

import static org.zwobble.shed.compiler.parsing.SourceRange.range;

import static org.zwobble.shed.compiler.parsing.SourcePosition.position;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.compiler.CompilerTesting.errorStrings;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.parsing.ParserTesting.isSuccessWithNode;
import static org.zwobble.shed.compiler.parsing.ParserTesting.tokens;

public class TopLevelNodesTest {
    @Test public void
    canParseEmptyString() {
        assertThat(TopLevelNodes.source().parse(tokens("")).anyErrors(),
                   is(true));
    }
    
    @Test public void
    packageDeclarationIsListOfIdentifiersJoinedByDots() {
        assertThat(
            TopLevelNodes.packageDeclaration().parse(tokens("package shed.util.collections;")),
            isSuccessWithNode(new PackageDeclarationNode(asList("shed", "util", "collections")))
        );
    }
    
    @Test public void
    sourceNodeCanHaveNoImportNodes() {
        assertThat(TopLevelNodes.source().parse(tokens("package shed.util.collections;\n\npublic List;\nval x = 1;")),
            isSuccessWithNode(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                Collections.<ImportNode>emptyList(),
                new PublicDeclarationNode(asList("List")),
                asList((StatementNode)new ImmutableVariableNode("x", none(TypeReferenceNode.class), new NumberLiteralNode("1")))
            ))
        );
    }
    
    @Test public void
    sourceNodeHasPackageDeclarationAndImportNodesAndPublicDeclarationAndStatements() {
        String source = "package shed.util.collections;\n\nimport shed.util;\npublic List, Set;" +
                "val x = 1; var y = 2;";
        assertThat(TopLevelNodes.source().parse(tokens(source)),
            isSuccessWithNode(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                asList(
                    new ImportNode(asList("shed", "util"))
                ),
                new PublicDeclarationNode(asList("List", "Set")),
                Arrays.<StatementNode>asList(
                    new ImmutableVariableNode("x", none(TypeReferenceNode.class), new NumberLiteralNode("1")),
                    new MutableVariableNode("y", none(TypeReferenceNode.class), new NumberLiteralNode("2"))
                )
            ))
        );
    }
    
    @Test public void
    errorsHaveLineNumbersAndCharacterNumbers() {
        assertThat(TopLevelNodes.source().parse(tokens("packag shed.util.collections; import shed import shed.collections;\nblah")).getErrors(),
            is(asList(
                new CompilerError(range(position(1, 1), position(1, 7)), "Expected keyword \"package\" but got identifier \"packag\""),
                new CompilerError(range(position(1, 43), position(1, 49)), "Expected symbol \";\" but got keyword \"import\""),
                new CompilerError(range(position(2, 1), position(2, 5)), "Expected keyword \"public\" but got identifier \"blah\""),
                new CompilerError(range(position(2, 5), position(2, 5)), "Expected statement but got end of source")
                
            ))
        );
    }
    
    @Test public void
    errorsAreCombinedIfPackageDeclarationAndImportsHaveErrors() {
        assertThat(errorStrings(TopLevelNodes.source().parse(tokens("packag shed.util.collections; import shed import shed.collections;\nblah"))),
            is(asList(
                "Expected keyword \"package\" but got identifier \"packag\"",
                "Expected symbol \";\" but got keyword \"import\"",
                "Expected keyword \"public\" but got identifier \"blah\"",
                "Expected statement but got end of source"
            ))
        );
    }
    
    @Test public void
    sourceNodeAttemptsToParseUpToEnd() {
        assertThat(
            errorStrings(TopLevelNodes.source().parse(tokens("package shed.util.collections; public x;\nval x = 1; a"))),
            is(asList(
                "Expected end of source but got identifier \"a\""
            ))
        );
    }
    
    @Test public void
    errorsIfImportIsMissingSemicolon() {
        assertThat(
            errorStrings(TopLevelNodes.source().parse(tokens("package shed.util.collections; import shed import shed.collections;public List;val x = 1;"))),
            is(asList("Expected symbol \";\" but got keyword \"import\""))
        );
    }
    
    @Test public void
    parserAttemptsToParseRestOfSourceFileIfErrorIsFound() {
        String source = "package shed.util.collections; import shed import shed.collections; import shed.stuff; val x; val y = 2;";
        assertThat(
            TopLevelNodes.source().parse(tokens(source)).get(),
            is(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                asList(new ImportNode(asList("shed", "stuff"))),
                null,
                asList((StatementNode)new ImmutableVariableNode("y", none(TypeReferenceNode.class), new NumberLiteralNode("2")))
            ))
        );
    }
    
    @Test public void
    errorIsRaisedIfPackageDeclarationDoesNotStartWithPackageKeyword() {
        assertThat(errorStrings(TopLevelNodes.packageDeclaration().parse(tokens("packag shed.util.collections;"))),
                   is(asList("Expected keyword \"package\" but got identifier \"packag\"")));
    }
    
    @Test public void
    errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfDot() {
        assertThat(errorStrings(TopLevelNodes.packageDeclaration().parse(tokens("package shed .util.collections;"))),
                   is(asList("Expected symbol \";\" but got symbol \".\"")));
    }
    
    @Test public void
    errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfIdentifier() {
        assertThat(errorStrings(TopLevelNodes.packageDeclaration().parse(tokens("package shed. util.collections;"))),
            is(asList(
                "Expected identifier but got whitespace \" \""
            ))
        );
    }
    
    @Test public void
    errorsInStatementsAreReported() {
        assertThat(errorStrings(TopLevelNodes.source().parse(tokens("package blah; public blah; val x = 2; val y 3;"))),
            is(asList(
                "Expected symbol \"=\" but got number \"3\""
            ))
        );
    }
    
    @Test public void
    canImportPackages() {
        assertThat(
            TopLevelNodes.importNode().parse(tokens("import shed.util.collections;")),
            isSuccessWithNode(new ImportNode(asList("shed", "util", "collections")))
        );
    }
    
    @Test public void
    canDeclarePublicVariables() {
        assertThat(
            TopLevelNodes.publicDeclaration().parse(tokens("public List, Set;")),
            isSuccessWithNode(new PublicDeclarationNode(asList("List", "Set")))
        );
    }
    
    @Test public void
    closingBraceEndsCurrentStatement() {
        assertThat(
            errorStrings(TopLevelNodes.source().parse(tokens("package blah; public x; val x = (x :Integer) : Integer => { return 4 }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \";\" but got symbol \"}\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    matchingClosingBraceEndsCurrentStatement() {
        assertThat(
            errorStrings(TopLevelNodes.source().parse(tokens("package blah; public x; val x = (x :Integer) : Integer => { return 4 {} }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \";\" but got symbol \"{\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    matchingClosingBraceEndsCurrentBlock() {
        assertThat(
            errorStrings(TopLevelNodes.source().parse(tokens("package blah; public x; val x = (x :Integer) : Integer => { {} }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \"}\" but got symbol \"{\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    closingBracketClosesAnyEnclosedBraces() {
        assertThat(
            errorStrings(TopLevelNodes.source().parse(tokens("package blah; public x; val x = (x :Integer)  : Integer => { { ({)  } }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \"}\" but got symbol \"{\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    closingBraceClosesAnyEnclosedBrackets() {
        assertThat(
            errorStrings(TopLevelNodes.source().parse(tokens("package blah; public x; val x = (x :Integer) : Integer => { {(} }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \"}\" but got symbol \"{\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    semicolonEndsStatementAndClosesAnyOpenParensInBlockScope() {
        assertThat(
            errorStrings(TopLevelNodes.source().parse(tokens("package blah; public x; val x = (x :Integer) : Integer => { ({(; )}; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \"}\" but got symbol \"(\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
}
