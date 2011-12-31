package org.zwobble.shed.compiler.sourceordering;

import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class DirectedGraph<T> {
    public static <T> DirectedGraph<T> create(Iterable<T> nodes) {
        return new DirectedGraph<T>(nodes);
    }
    
    private final Iterable<T> nodes;
    private final SetMultimap<T, T> edges = HashMultimap.create();

    private DirectedGraph(Iterable<T> nodes) {
        this.nodes = nodes;
    }
    
    public void addEdge(T from, T to) {
        edges.put(from, to);
    }
    
    public Result topologicalSort() {
        Visitor visitor = new Visitor();
        visitor.topologicalSort();
        return visitor.getResult();
    }
    
    public class Result {
        private final List<List<T>> circularDependencies;
        private final Iterable<T> value;

        private Result(List<List<T>> circularDependencies, Iterable<T> value) {
            this.circularDependencies = circularDependencies;
            this.value = value;
        }
        
        public List<List<T>> getCircularDependencies() {
            return circularDependencies;
        }
        
        public Iterable<T> getValue() {
            return value;
        }

        public boolean isSuccess() {
            return circularDependencies.isEmpty();
        }
    }
    
    private class Visitor {
        private final Set<T> visited = Sets.newHashSet();
        private final List<T> visiting = Lists.newArrayList();
        private final List<T> ordered = Lists.newArrayList();
        private final List<List<T>> circularDependencies = Lists.newArrayList();

        public Result getResult() {
            return new Result(circularDependencies, ordered);
        }
        
        public void topologicalSort() {
            visitAll(nodes);
        }   
        
        private void visitAll(Iterable<T> nodes) {
            for (T node : nodes) {
                visit(node);
            }
        }
        
        private void visit(T node) {
            if (visited.contains(node)) {
                return;
            }
            if (visiting.contains(node)) {
                circularDependencies.add(ImmutableList.copyOf(visiting));
                return;
            }
            visiting.add(node);
            visitAll(edges.get(node));
            visiting.remove(node);
            visited.add(node);
            ordered.add(node);
        }
    }
}
