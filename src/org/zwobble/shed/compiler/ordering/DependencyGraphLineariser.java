package org.zwobble.shed.compiler.ordering;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.zwobble.shed.compiler.CompilerError;
import org.zwobble.shed.compiler.ordering.errors.CircularDependencyError;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.zwobble.shed.compiler.Eager.transform;

public class DependencyGraphLineariser {

    public TypeResult<Iterable<StatementNode>> linearise(DependencyGraph graph, NodeLocations nodeLocations) {
        return new Visitor(graph, nodeLocations).visitAll();
    }
    
    private static class Visitor {
        private final List<StatementNode> ordered = new ArrayList<StatementNode>();
        private final Set<Identity<StatementNode>> declared = Sets.newHashSet();
        private final Deque<Identity<StatementNode>> declaring = Lists.newLinkedList();
        private final DependencyGraph graph;
        private TypeResult<Void> result = TypeResult.success();
        private final NodeLocations nodeLocations;
        
        public Visitor(DependencyGraph graph, NodeLocations nodeLocations) {
            this.graph = graph;
            this.nodeLocations = nodeLocations;
        }
        
        public TypeResult<Iterable<StatementNode>> visitAll() {
            for (StatementNode statement : graph.getStatements()) {
                visit(statement);
            }
            return TypeResult.<Iterable<StatementNode>>success(ordered).withErrorsFrom(result);
        }
        
        private void visit(StatementNode statement) {
            if (declared.contains(identity(statement))) {
                return;
            }
            if (declaring.contains(identity(statement))) {
                result = result.withErrorsFrom(TypeResult.failure(new CompilerError(
                    nodeLocations.locate(statement),
                    new CircularDependencyError(transform(declaring, toStatement()))
                )));
                return;
            }
            declaring.add(identity(statement));
            for (StatementNode dependency : graph.dependenciesOf(statement)) {
                visit(dependency);
            }
            declared.add(identity(statement));
            ordered.add(statement);
        }

        private Function<Identity<StatementNode>, StatementNode> toStatement() {
            return new Function<Identity<StatementNode>, StatementNode>() {
                @Override
                public StatementNode apply(Identity<StatementNode> input) {
                    return input.get();
                }
            };
        }
    }

    private static Identity<StatementNode> identity(StatementNode statement) {
        return new Identity<StatementNode>(statement);
    }
}
