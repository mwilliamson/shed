package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.Map;

import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.SourceRange;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.Node;
import org.zwobble.shed.compiler.util.Pair;

import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;
import static org.zwobble.shed.compiler.util.Pair.pair;

public class SimpleNodeLocations implements NodeLocations {
    public static NodeLocations combine(Iterable<? extends NodeLocations> allLocations) {
        SimpleNodeLocations combinedLocations = new SimpleNodeLocations();
        for (NodeLocations locations : allLocations) {
            for (Pair<Node, SourceRange> location : locations.all()) {
                combinedLocations.put(location.getFirst(), location.getSecond());
            }
        }
        return combinedLocations;
    }
    
    private final Map<Identity<Node>, SourceRange> locations = new HashMap<Identity<Node>, SourceRange>();
    
    public void put(Node node, SourceRange location) {
        locations.put(new Identity<Node>(node), location);
    }
    
    @Override
    public SourceRange locate(Node node) {
        return locations.get(new Identity<Node>(node));
    }
    
    @Override
    public Iterable<Pair<Node, SourceRange>> all() {
        return transform(locations.entrySet(), toPair());
    }
    
    private Function<Map.Entry<Identity<Node>, SourceRange>, Pair<Node, SourceRange>> toPair() {
        return new Function<Map.Entry<Identity<Node>,SourceRange>, Pair<Node,SourceRange>>() {
            @Override
            public Pair<Node, SourceRange> apply(Map.Entry<Identity<Node>, SourceRange> input) {
                return pair(input.getKey().get(), input.getValue());
            }
        };
    }
}
