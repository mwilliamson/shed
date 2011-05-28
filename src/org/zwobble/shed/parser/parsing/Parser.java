package org.zwobble.shed.parser.parsing;

import java.util.List;

import org.zwobble.shed.parser.tokeniser.TokenPosition;

import com.google.common.collect.PeekingIterator;

import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.parsing.Rules.keyword;
import static org.zwobble.shed.parser.parsing.Rules.repeatedWithSeparator;
import static org.zwobble.shed.parser.parsing.Rules.sequence;
import static org.zwobble.shed.parser.parsing.Rules.symbol;
import static org.zwobble.shed.parser.parsing.Rules.then;
import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;
import static org.zwobble.shed.parser.parsing.Rules.whitespace;
import static org.zwobble.shed.parser.tokeniser.Keyword.PACKAGE;
import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;

public class Parser {
    public Result<SourceNode> source(PeekingIterator<TokenPosition> tokens) {
        Result<PackageDeclarationNode> packageDeclaration = parsePackageDeclaration().parse(tokens);
        if (packageDeclaration.anyErrors()) {
            return packageDeclaration.changeValue(null);
        }
        return success(new SourceNode(packageDeclaration.get()));
    }
    
    public Rule<PackageDeclarationNode> parsePackageDeclaration() {
        final Rule<List<String>> names;
        return then(
            sequence(
                keyword(PACKAGE),
                whitespace(),
                (names = repeatedWithSeparator(tokenOfType(IDENTIFIER), symbol("."))),
                symbol(";")
            ),
            new ParseAction<RuleValues, PackageDeclarationNode>() {
                @Override
                public Result<PackageDeclarationNode> apply(RuleValues result) {
                    return success(new PackageDeclarationNode(result.get(names)));
                }
            }
        );
    }
}
