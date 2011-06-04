package org.zwobble.shed.parser.parsing;

import java.util.List;

import org.zwobble.shed.parser.parsing.nodes.ImportNode;
import org.zwobble.shed.parser.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.parser.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.parser.parsing.nodes.SourceNode;
import org.zwobble.shed.parser.parsing.nodes.StatementNode;
import org.zwobble.shed.parser.tokeniser.Keyword;
import org.zwobble.shed.parser.tokeniser.Token;

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
import static org.zwobble.shed.parser.parsing.Separator.hardSeparator;
import static org.zwobble.shed.parser.parsing.Separator.softSeparator;
import static org.zwobble.shed.parser.parsing.Statements.aStatement;
import static org.zwobble.shed.parser.parsing.Statements.statement;
import static org.zwobble.shed.parser.tokeniser.Keyword.IMPORT;
import static org.zwobble.shed.parser.tokeniser.Keyword.PACKAGE;
import static org.zwobble.shed.parser.tokeniser.TokenType.IDENTIFIER;

public class Parser {
    public Rule<SourceNode> source() {
        final Rule<PackageDeclarationNode> packageDeclaration;
        final Rule<List<ImportNode>> imports;
        final Rule<PublicDeclarationNode> publicDeclaration;
        final Rule<List<StatementNode>> statements;
        return then(
            sequence(OnError.CONTINUE,
                packageDeclaration = packageDeclaration(),
                optional(whitespace()),
                imports = zeroOrMoreWithSeparator(importNode(), softSeparator(whitespace())),
                optional(whitespace()),
                publicDeclaration = publicDeclaration(),
                optional(whitespace()),
                statements = oneOrMoreWithSeparator(statement(), softSeparator(whitespace())),
                optional(whitespace()),
                Rules.token(Token.end())
            ),
            new ParseAction<RuleValues, SourceNode>() {
                @Override
                public Result<SourceNode> apply(RuleValues result) {
                    return success(new SourceNode(
                        result.get(packageDeclaration),
                        result.get(imports),
                        result.get(publicDeclaration),
                        result.get(statements)
                    ));
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
    
    public Rule<PublicDeclarationNode> publicDeclaration() {
        Rule<RuleValues> comma = sequence(OnError.FINISH, optional(whitespace()), symbol(","), optional(whitespace()));
        final Rule<List<String>> identifiers = oneOrMoreWithSeparator(tokenOfType(IDENTIFIER), hardSeparator(comma));
        return then( 
            aStatement(OnError.FINISH,
                keyword(Keyword.PUBLIC),
                whitespace(),
                identifiers
            ),
            new ParseAction<RuleValues, PublicDeclarationNode>() {
                @Override
                public Result<PublicDeclarationNode> apply(RuleValues result) {
                    return success(new PublicDeclarationNode(result.get(identifiers)));
                }
            }
        );
    }
    
    private Rule<List<String>> dotSeparatedIdentifiers() {
        return oneOrMoreWithSeparator(tokenOfType(IDENTIFIER), hardSeparator(symbol(".")));
    }
    
    // TODO: implicit whitespace in sequence
    // TODO: pass to Rule.parse a value indicating whether the rule is required to finish
    //       (so that it can decide whether to return NO_MATCH if failure on a guard rule)
}
