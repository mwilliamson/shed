package org.zwobble.shed.compiler.parsing.nodes;

import org.zwobble.shed.compiler.parsing.nodes.structure.SyntaxNodeStructure;

public interface SyntaxNode extends Node {
    SyntaxNodeStructure describeStructure();
}
