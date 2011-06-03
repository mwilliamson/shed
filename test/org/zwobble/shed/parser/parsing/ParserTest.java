package org.zwobble.shed.parser.parsing;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.parser.parsing.nodes.ImportNode;
import org.zwobble.shed.parser.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.parser.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.parser.parsing.nodes.SourceNode;
import org.zwobble.shed.parser.tokeniser.Tokeniser;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zwobble.shed.parser.parsing.Result.success;

public class ParserTest {
    private final Tokeniser tokeniser = new Tokeniser();
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
        assertThat(parser.source().parse(tokens("package shed.util.collections;\n\npublic List;")),
            is(success(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                Collections.<ImportNode>emptyList(),
                new PublicDeclarationNode(asList("List"))
            )))
        );
    }
    
    @Test public void
    sourceNodeHasPackageDeclarationAndImportNodesAndPublicDeclaration() {
        assertThat(parser.source().parse(tokens("package shed.util.collections;\n\nimport shed.util;\npublic List, Set;")),
            is(success(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                asList(
                    new ImportNode(asList("shed", "util"))
                ),
                new PublicDeclarationNode(asList("List", "Set"))
            )))
        );
    }
    
    @Test public void
    errorsAreCombinedIfPackageDeclarationAndImportsHaveErrors() {
        assertThat(parser.source().parse(tokens("packag shed.util.collections; import shed import shed.collections;\nblah")).getErrors(),
            is(asList(
                new CompilerError(1, 1, "Expected keyword \"package\" but got identifier \"packag\""),
                new CompilerError(1, 42, "Expected symbol \";\" but got whitespace \" \""),
                new CompilerError(2, 1, "Expected keyword \"public\" but got identifier \"blah\"")
            ))
        );
    }
    
    @Test public void
    errorsIfImportIsMissingSemicolon() {
        assertThat(parser.source().parse(tokens("package shed.util.collections; import shed import shed.collections;public List;")).getErrors(),
            is(asList(new CompilerError(1, 43, "Expected symbol \";\" but got whitespace \" \"")))
        );
    }
    
    @Test public void
    parserAttemptsToParseRestOfSourceFileIfErrorIsFound() {
        String source = "package shed.util.collections; import shed import shed.collections; import shed.stuff;";
        assertThat(parser.source().parse(tokens(source)).get(),
            is(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                asList(new ImportNode(asList("shed", "stuff"))),
                null
            ))
        );
    }
    
    @Test public void
    errorIsRaisedIfPackageDeclarationDoesNotStartWithPackageKeyword() {
        assertThat(parser.packageDeclaration().parse(tokens("packag shed.util.collections;")).getErrors(),
                   is(asList(new CompilerError(1, 1, "Expected keyword \"package\" but got identifier \"packag\""))));
    }
    
    @Test public void
    errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfDot() {
        assertThat(parser.packageDeclaration().parse(tokens("package shed .util.collections;")).getErrors(),
                   is(asList(new CompilerError(1, 13, "Expected symbol \";\" but got whitespace \" \""))));
    }
    
    @Test public void
    errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfIdentifier() {
        assertThat(parser.packageDeclaration().parse(tokens("package shed. util.collections;")).getErrors(),
            is(asList(
                new CompilerError(1, 14, "Expected identifier but got whitespace \" \"")
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
    
    private TokenIterator tokens(String input) {
        return new TokenIterator(tokeniser.tokenise(input));
    }
}
