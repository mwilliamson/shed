package org.zwobble.shed.compiler.errors;

import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.SyntaxNode;

public class CompilerErrors {
    public static CompilerError error(SyntaxNode node, String description) {
        return error(node, new SimpleErrorDescription(description));
    }

    public static CompilerError error(SyntaxNode node, CompilerErrorDescription description) {
        return new CompilerErrorWithSyntaxNode(node, description);
    }

    public static CompilerError error(SourceRange location, String description) {
        return new CompilerErrorWithLocation(location, new SimpleErrorDescription(description));
    }
}
