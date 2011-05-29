package org.zwobble.shed.parser.parsing;

import java.util.Collections;

import org.junit.Test;
import org.zwobble.shed.parser.parsing.nodes.ImmutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.ImportNode;
import org.zwobble.shed.parser.parsing.nodes.MutableVariableNode;
import org.zwobble.shed.parser.parsing.nodes.NumberLiteralNode;
import org.zwobble.shed.parser.parsing.nodes.PackageDeclarationNode;
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
    packageDeclarationIsListOfIdentifiersJoinedByDots() {
        assertThat(parser.packageDeclaration().parse(tokens("package shed.util.collections;")),
                   is(success(new PackageDeclarationNode(asList("shed", "util", "collections")))));
    }
    
    @Test public void
    sourceNodeBeginsWithPackageDeclaration() {
        assertThat(parser.source().parse(tokens("package shed.util.collections;")),
            is(success(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                Collections.<ImportNode>emptyList()
            )))
        );
    }
    
    @Test public void
    sourceNodeCanHaveImportsAfterPackageDeclaration() {
        assertThat(parser.source().parse(tokens("package shed.util.collections;\n\nimport shed.util; import shed;")),
            is(success(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                asList(
                    new ImportNode(asList("shed", "util")),
                    new ImportNode(asList("shed"))
                )
            )))
        );
    }
    
    @Test public void
    errorsIfImportIsMissingSemicolon() {
        assertThat(parser.source().parse(tokens("package shed.util.collections; import shed import shed.collections;")).getErrors(),
            is(asList(new Error(1, 43, "Expected symbol \";\" but got whitespace \" \"")))
        );
    }
    
    @Test public void
    parserAttemptsToParseRestOfSourceFileIfErrorIsFound() {
        String source = "package shed.util.collections; import shed import shed.collections; import shed.stuff;";
        assertThat(parser.source().parse(tokens(source)).get(),
            is(new SourceNode(
                new PackageDeclarationNode(asList("shed", "util", "collections")),
                asList(new ImportNode(asList("shed", "stuff")))
            ))
        );
    }
    
    @Test public void
    errorIsRaisedIfPackageDeclarationDoesNotStartWithPackageKeyword() {
        assertThat(parser.packageDeclaration().parse(tokens("packag shed.util.collections;")).getErrors(),
                   is(asList(new Error(1, 1, "Expected keyword \"package\" but got identifier \"packag\""))));
    }
    
    @Test public void
    errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfDot() {
        assertThat(parser.packageDeclaration().parse(tokens("package shed .util.collections;")).getErrors(),
                   is(asList(new Error(1, 13, "Expected symbol \";\" but got whitespace \" \""))));
    }
    
    @Test public void
    errorInPackageDeclarationIsRaisedIfWhitespaceIsEncounteredInsteadOfIdentifier() {
        assertThat(parser.packageDeclaration().parse(tokens("package shed. util.collections;")).getErrors(),
                   is(asList(new Error(1, 14, "Expected identifier but got whitespace \" \""))));
    }
    
    @Test public void
    canImportPackages() {
        assertThat(parser.importNode().parse(tokens("import shed.util.collections;")),
                   is(success(new ImportNode(asList("shed", "util", "collections")))));
    }
    
    @Test public void
    canDeclareImmutableVariables() {
        assertThat(parser.immutableVariable().parse(tokens("val magic = 42;")),
                   is(success(new ImmutableVariableNode("magic", new NumberLiteralNode("42")))));
    }
    
    @Test public void
    canDeclareMutableVariables() {
        assertThat(parser.mutableVariable().parse(tokens("var magic = 42;")),
                   is(success(new MutableVariableNode("magic", new NumberLiteralNode("42")))));
    }
    
    private TokenIterator tokens(String input) {
        return new TokenIterator(tokeniser.tokenise(input));
    }
}
