package org.zwobble.shed.parser.parsing;

import java.util.List;

import static org.zwobble.shed.parser.parsing.Rules.guard;

import static org.zwobble.shed.parser.parsing.Rules.optional;

import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.parsing.Rules.keyword;
import static org.zwobble.shed.parser.parsing.Rules.oneOrMoreWithSeparator;
import static org.zwobble.shed.parser.parsing.Rules.sequence;
import static org.zwobble.shed.parser.parsing.Rules.symbol;
import static org.zwobble.shed.parser.parsing.Rules.then;
import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;
import static org.zwobble.shed.parser.parsing.Rules.whitespace;
import static org.zwobble.shed.parser.parsing.Rules.zeroOrMoreWithSeparator;
import static org.zwobble.shed.parser.tokeniser.Keyword.IMPORT;
import static org.zwobble.shed.parser.tokeniser.Keyword.PACKAGE;
import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;

public class Parser {
    public Rule<SourceNode> source() {
        final Rule<PackageDeclarationNode> packageDeclaration;
        final Rule<List<ImportNode>> imports;
        return then(
            sequence(
                packageDeclaration = packageDeclaration(),
                optional(whitespace()),
                imports = zeroOrMoreWithSeparator(importNode(), whitespace())
            ),
            new ParseAction<RuleValues, SourceNode>() {
                @Override
                public Result<SourceNode> apply(RuleValues result) {
                    return success(new SourceNode(result.get(packageDeclaration), result.get(imports)));
                }
            }
        );
    }
    
    public Rule<PackageDeclarationNode> packageDeclaration() {
        final Rule<List<String>> names;
        return then(
            sequence(
                guard(keyword(PACKAGE)),
                whitespace(),
                names = dotSeparatedIdentifiers(),
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

    public Rule<ImportNode> importNode() {
        final Rule<List<String>> names;
        return then(
            sequence(
                guard(keyword(IMPORT)),
                whitespace(),
                (names = dotSeparatedIdentifiers()),
                symbol(";")
            ),
            new ParseAction<RuleValues, ImportNode>() {
                @Override
                public Result<ImportNode> apply(RuleValues result) {
                    return success(new ImportNode(result.get(names)));
                }
            }
        );
    }
    
    private Rule<List<String>> dotSeparatedIdentifiers() {
        return oneOrMoreWithSeparator(tokenOfType(IDENTIFIER), symbol("."));
    }
}
