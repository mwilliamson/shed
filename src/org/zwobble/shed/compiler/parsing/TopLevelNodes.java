package org.zwobble.shed.compiler.parsing;

import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.PackageDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.tokeniser.Token;

import static org.zwobble.shed.compiler.parsing.Rules.guard;
import static org.zwobble.shed.compiler.parsing.Rules.keyword;
import static org.zwobble.shed.compiler.parsing.Rules.oneOrMore;
import static org.zwobble.shed.compiler.parsing.Rules.oneOrMoreWithSeparator;
import static org.zwobble.shed.compiler.parsing.Rules.sequence;
import static org.zwobble.shed.compiler.parsing.Rules.symbol;
import static org.zwobble.shed.compiler.parsing.Rules.then;
import static org.zwobble.shed.compiler.parsing.Rules.tokenOfType;
import static org.zwobble.shed.compiler.parsing.Rules.zeroOrMore;
import static org.zwobble.shed.compiler.parsing.Separator.hardSeparator;
import static org.zwobble.shed.compiler.parsing.Statements.aStatement;
import static org.zwobble.shed.compiler.parsing.Statements.statement;
import static org.zwobble.shed.compiler.tokeniser.Keyword.IMPORT;
import static org.zwobble.shed.compiler.tokeniser.Keyword.PACKAGE;
import static org.zwobble.shed.compiler.tokeniser.TokenType.IDENTIFIER;


public class TopLevelNodes {
    public static Rule<SourceNode> source() {
        final Rule<PackageDeclarationNode> packageDeclaration;
        final Rule<List<ImportNode>> imports;
        final Rule<List<StatementNode>> statements;
        return then(
            sequence(OnError.CONTINUE,
                packageDeclaration = packageDeclaration(),
                imports = zeroOrMore(importNode()),
                statements = oneOrMore(statement()),
                Rules.token(Token.end())
            ),
            new SimpleParseAction<RuleValues, SourceNode>() {
                @Override
                public SourceNode apply(RuleValues result) {
                    return new SourceNode(
                        result.get(packageDeclaration),
                        result.get(imports),
                        result.get(statements)
                    );
                }
            }
        );
    }
    
    public static Rule<PackageDeclarationNode> packageDeclaration() {
        final Rule<List<String>> names;
        return then(
            aStatement(
                keyword(PACKAGE),
                names = dotSeparatedIdentifiers()
            ),
            new SimpleParseAction<RuleValues, PackageDeclarationNode>() {
                @Override
                public PackageDeclarationNode apply(RuleValues result) {
                    return new PackageDeclarationNode(result.get(names));
                }
            }
        );
    }

    public static Rule<ImportNode> importNode() {
        final Rule<List<String>> names;
        return then(
            aStatement(
                guard(keyword(IMPORT)),
                (names = dotSeparatedIdentifiers())
            ),
            new SimpleParseAction<RuleValues, ImportNode>() {
                @Override
                public ImportNode apply(RuleValues result) {
                    return new ImportNode(result.get(names));
                }
            }
        );
    }
    
    private static Rule<List<String>> dotSeparatedIdentifiers() {
        return oneOrMoreWithSeparator(tokenOfType(IDENTIFIER), hardSeparator(symbol(".")));
    }
    
    // TODO: pass to Rule.parse a value indicating whether the rule is required to finish
    //       (so that it can decide whether to return NO_MATCH if failure on a guard rule)
}
