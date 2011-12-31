package org.zwobble.shed.compiler.sourceordering;

import java.util.List;

import javax.inject.Inject;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.errors.CompilerError;
import org.zwobble.shed.compiler.errors.CompilerErrorWithSyntaxNode;
import org.zwobble.shed.compiler.modules.Module;
import org.zwobble.shed.compiler.modules.SourceModule;
import org.zwobble.shed.compiler.modules.Modules;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.EntireSourceNode;
import org.zwobble.shed.compiler.parsing.nodes.ImportNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.typechecker.TypeResult;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static com.google.common.collect.Iterables.getLast;
import static org.zwobble.shed.compiler.Option.none;
import static org.zwobble.shed.compiler.Option.some;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.typechecker.TypeResults.failure;
import static org.zwobble.shed.compiler.typechecker.TypeResults.success;

public class SourceOrderer {
    private final Modules modules;

    @Inject
    public SourceOrderer(Modules modules) {
        this.modules = modules;
    }
    
    public TypeResult<EntireSourceNode> reorder(EntireSourceNode sourceNodes) {
        DirectedGraph<SourceNode> graph = DirectedGraph.create(sourceNodes);
        for (SourceNode source : sourceNodes) {
            addDependencies(sourceNodes, graph, source);
        }
        DirectedGraph<SourceNode>.Result sortResult = graph.topologicalSort();
        if (sortResult.isSuccess()) {
            return success(new EntireSourceNode(sortResult.getValue()));
        } else {
            return failure(Lists.transform(sortResult.getCircularDependencies(), toCompilerError()));
        }
    }

    private Function<List<SourceNode>, CompilerError> toCompilerError() {
        return new Function<List<SourceNode>, CompilerError>() {
            @Override
            public CompilerError apply(List<SourceNode> input) {
                return new CompilerErrorWithSyntaxNode(getLast(input), CircularModuleDependency.create(Lists.transform(input, toName())));
            }
        };
    }

    private Function<SourceNode, FullyQualifiedName> toName() {
        return new Function<SourceNode, FullyQualifiedName>() {
            @Override
            public FullyQualifiedName apply(SourceNode input) {
                return input.name();
            }
        };
    }

    private void addDependencies(EntireSourceNode sourceNodes, DirectedGraph<SourceNode> graph, SourceNode source) {
        for (ImportNode importNode : source.getImports()) {
            addDependency(sourceNodes, graph, source, importNode);
        }
    }

    private void addDependency(EntireSourceNode sourceNodes, DirectedGraph<SourceNode> graph, SourceNode source, ImportNode importNode) {
        Option<SourceNode> dependency = findSource(sourceNodes, importNode);
        if (dependency.hasValue()) {
            graph.addEdge(source, dependency.get());
        }
    }

    private Option<SourceNode> findSource(EntireSourceNode sourceNodes, ImportNode importNode) {
        Option<Module> module = modules.lookup(fullyQualifiedName(importNode.getNames()));
        if (module.hasValue()) {
            // TODO: handle other types of module
            return some(((SourceModule)module.get()).getSource());
        } else {
            return none();
        }
    }
}
