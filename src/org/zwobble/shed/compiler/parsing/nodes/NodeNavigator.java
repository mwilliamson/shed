package org.zwobble.shed.compiler.parsing.nodes;


public class NodeNavigator {
    public static Iterable<? extends SyntaxNode> children(SyntaxNode node) {
        return node.describeStructure().getChildren();
    }
}
