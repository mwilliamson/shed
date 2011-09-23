package org.zwobble.shed.compiler.parsing.nodes;

import java.util.ArrayList;
import java.util.List;

import org.zwobble.shed.compiler.parsing.nodes.structure.ScopedNodes;

import com.google.common.collect.Iterables;


public class NodeNavigator {
    public static Iterable<? extends SyntaxNode> children(SyntaxNode node) {
        List<SyntaxNode> nodes = new ArrayList<SyntaxNode>();
        for (ScopedNodes scopedNodes : node.describeStructure().getChildren()) {
            Iterables.addAll(nodes, scopedNodes);
        }
        return nodes;
    }
}
