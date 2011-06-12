package org.zwobble.shed.parser.parsing;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.parser.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.ImportNode;
import org.zwobble.shed.parser.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.parser.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.parser.parsing.nodes.SourceNode;
import org.zwobble.shed.parser.parsing.nodes.StatementNode;
import org.zwobble.shed.parser.parsing.nodes.TypeReferenceNode;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.zwobble.shed.parser.Option.none;
import static org.zwobble.shed.parser.parsing.ParserTesting.errorStrings;
import static org.zwobble.shed.parser.parsing.ParserTesting.tokens;
import static org.zwobble.shed.parser.parsing.Result.success;

public class ParserTest {
    private final Parser parser = new Parser();
    
    @Test public void
    canParseEmptyString() {
        assertThat(parser.source().parse(tokens("")).anyErrors(),
                   is(true));
    }
    
    @Test public void
    packageDeclarationIsListOfIdentifiersJoinedByDots() {
        assertThat(parser.packageDeclaration().parse(tokens("package shed.util.collections;")),
                   is(success(new PackageDeclarationNode(asList("shed", "util", "collections")))));
    }
    
    @Test public void
    sourceNodeCanHaveNoImportNodes() {
        assertThat(parser.source().parse(tokens("package shed.util.collections;\n\npublic List;\nval x = 1;")),
            is(success(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                Collections.<ImportNode>emptyList(),
                new PublicDeclarationNode(asList("List")),
                asList((StatementNode)new ImmutableVariableNode("x", none(TypeReferenceNode.class), new NumberLiteralNode("1")))
            )))
        );
    }
    
    @Test public void
    sourceNodeHasPackageDeclarationAndImportNodesAndPublicDeclarationAndStatements() {
        assertThat(parser.source().parse(tokens("package shed.util.collections;\n\nimport shed.util;\npublic List, Set;" +
                "val x = 1; var y = 2;")),
            is(success(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                asList(
                    new ImportNode(asList("shed", "util"))
                ),
                new PublicDeclarationNode(asList("List", "Set")),
                asList(
                    new ImmutableVariableNode("x", none(TypeReferenceNode.class), new NumberLiteralNode("1")),
                    new MutableVariableNode("y", none(TypeReferenceNode.class), new NumberLiteralNode("2"))
                )
            )))
        );
    }
    
    @Test public void
    errorsHaveLineNumbersAndCharacterNumbers() {
        assertThat(parser.source().parse(tokens("packag shed.util.collections; import shed import shed.collections;\nblah")).getErrors(),
            is(asList(
                new CompilerError(new SourcePosition(1, 1), new SourcePosition(1, 7), "Expected keyword \"package\" but got identifier \"packag\""),
                new CompilerError(new SourcePosition(1, 43), new SourcePosition(1, 49), "Expected symbol \";\" but got keyword \"import\""),
                new CompilerError(new SourcePosition(2, 1), new SourcePosition(2, 5), "Expected keyword \"public\" but got identifier \"blah\""),
                new CompilerError(new SourcePosition(2, 5), new SourcePosition(2, 5), "Expected statement but got end of source")
                
            ))
        );
    }
    
    @Test public void
    errorsAreCombinedIfPackageDeclarationAndImportsHaveErrors() {
        assertThat(errorStrings(parser.source().parse(tokens("packag shed.util.collections; import shed import shed.collections;\nblah"))),
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
        assertThat(errorStrings(parser.source().parse(tokens("package shed.util.collections; public x;\nval x = 1; a"))),
            is(asList(
                "Expected end of source but got identifier \"a\""
            ))
        );
    }
    
    @Test public void
    errorsIfImportIsMissingSemicolon() {
        assertThat(
            errorStrings(parser.source().parse(tokens("package shed.util.collections; import shed import shed.collections;public List;val x = 1;"))),
            is(asList("Expected symbol \";\" but got keyword \"import\""))
        );
    }
    
    @Test public void
    parserAttemptsToParseRestOfSourceFileIfErrorIsFound() {
        String source = "package shed.util.collections; import shed import shed.collections; import shed.stuff; val x; val y = 2;";
        assertThat(parser.source().parse(tokens(source)).get(),
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
        assertThat(errorStrings(parser.packageDeclaration().parse(tokens("packag shed.util.collections;"))),
                   is(asList("Expected keyword \"package\" but got identifier \"packag\"")));
    }
    
    @Test public void
    errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfDot() {
        assertThat(errorStrings(parser.packageDeclaration().parse(tokens("package shed .util.collections;"))),
                   is(asList("Expected symbol \";\" but got symbol \".\"")));
    }
    
    @Test public void
    errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfIdentifier() {
        assertThat(errorStrings(parser.packageDeclaration().parse(tokens("package shed. util.collections;"))),
            is(asList(
                "Expected identifier but got whitespace \" \""
            ))
        );
    }
    
    @Test public void
    errorsInStatementsAreReported() {
        assertThat(errorStrings(parser.source().parse(tokens("package blah; public blah; val x = 2; val y 3;"))),
            is(asList(
                "Expected symbol \"=\" but got number \"3\""
            ))
        );
    }
    
    @Test public void
    canImportPackages() {
        assertThat(parser.importNode().parse(tokens("import shed.util.collections;")),
                   is(success(new ImportNode(asList("shed", "util", "collections")))));
    }
    
    @Test public void
    canDeclarePublicVariables() {
        assertThat(
            parser.publicDeclaration().parse(tokens("public List, Set;")),
            is(success(new PublicDeclarationNode(asList("List", "Set"))))
        );
    }
    
    @Test public void
    closingBraceEndsCurrentStatement() {
        assertThat(
            errorStrings(parser.source().parse(tokens("package blah; public x; val x = (x :Integer) => { return 4 }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \";\" but got symbol \"}\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    matchingClosingBraceEndsCurrentStatement() {
        assertThat(
            errorStrings(parser.source().parse(tokens("package blah; public x; val x = (x :Integer) => { return 4 {} }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \";\" but got symbol \"{\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    matchingClosingBraceEndsCurrentBlock() {
        assertThat(
            errorStrings(parser.source().parse(tokens("package blah; public x; val x = (x :Integer) => { {} }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \"}\" but got symbol \"{\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    closingBracketClosesAnyEnclosedBraces() {
        assertThat(
            errorStrings(parser.source().parse(tokens("package blah; public x; val x = (x :Integer) => { { ({)  } }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \"}\" but got symbol \"{\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    closingBraceClosesAnyEnclosedBrackets() {
        assertThat(
            errorStrings(parser.source().parse(tokens("package blah; public x; val x = (x :Integer) => { {(} }; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \"}\" but got symbol \"{\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
    
    @Test public void
    semicolonEndsStatementAndClosesAnyOpenParensInBlockScope() {
        assertThat(
            errorStrings(parser.source().parse(tokens("package blah; public x; val x = (x :Integer) => { ({(; )}; va y = 4;"))),
            containsInAnyOrder(
                "Expected symbol \"}\" but got symbol \"(\"",
                "Expected end of source but got identifier \"va\""
            )
        );
    }
}
