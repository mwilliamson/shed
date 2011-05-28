package org.zwobble.shed.parser.parsing;

import java.util.List;

import org.junit.Test;
import org.zwobble.shed.parser.tokeniser.Token;
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
        assertThat(parser.parsePackageDeclaration(tokens("package shed.util.collections;")),
                   is(success(new PackageDeclarationNode(asList("shed", "util", "collections")))));
    }
    
    @Test public void
    sourceNodeBeginsWithPackageDeclaration() {
        assertThat(parser.source(tokens("package shed.util.collections;")),
                   is(success(new SourceNode(new PackageDeclarationNode(asList("shed", "util", "collections"))))));
    }
    @Test public void
    errorIsRaisedIfPackageDeclarationDoesNotStartWithPackageKeyword() {
        assertThat(parser.parsePackageDeclaration(tokens("packag shed.util.collections;")),
                   is(Result.<PackageDeclarationNode>failure(asList(new Error(1, 1, "Expected keyword \"package\" but got identifier \"packag\"")))));
    }
    
    private List<Token> tokens(String input) {
        return tokeniser.tokenise(input);
    }
}
