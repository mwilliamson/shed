package org.zwobble.shed.parser.parsing;

import java.util.List;

import org.zwobble.shed.parser.parsing.nodes.ImportNode;
import org.zwobble.shed.parser.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.parser.parsing.nodes.SourceNode;

import static org.zwobble.shed.parser.parsing.Result.success;
import static org.zwobble.shed.parser.parsing.Rules.guard;
import static org.zwobble.shed.parser.parsing.Rules.keyword;
import static org.zwobble.shed.parser.parsing.Rules.oneOrMoreWithSeparator;
import static org.zwobble.shed.parser.parsing.Rules.optional;
import static org.zwobble.shed.parser.parsing.Rules.sequence;
import static org.zwobble.shed.parser.parsing.Rules.symbol;
import static org.zwobble.shed.parser.parsing.Rules.then;
import static org.zwobble.shed.parser.parsing.Rules.tokenOfType;
import static org.zwobble.shed.parser.parsing.Rules.whitespace;
import static org.zwobble.shed.parser.parsing.Rules.zeroOrMoreWithSeparator;
import static org.zwobble.shed.parser.parsing.Statements.aStatement;
import static org.zwobble.shed.parser.tokeniser.Keyword.IMPORT;
import static org.zwobble.shed.parser.tokeniser.Keyword.PACKAGE;
import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;

public class Parser {
    public Rule<SourceNode> source() {
        final Rule<PackageDeclarationNode> packageDeclaration;
        final Rule<List<ImportNode>> imports;
        return then(
            sequence(OnError.CONTINUE,
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
            aStatement(OnError.FINISH,
                keyword(PACKAGE),
                whitespace(),
                names = dotSeparatedIdentifiers()
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
            aStatement(OnError.FINISH,
                guard(keyword(IMPORT)),
                whitespace(),
                (names = dotSeparatedIdentifiers())
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
    
    // TODO: implicit whitespace in sequence
    // TODO: pass to Rule.parse a value indicating whether the rule is required to finish
    //       (so that it can decide whether to return NO_MATCH if failure on a guard rule)
}
