package org.zwobble.shed.compiler.modules;

import java.util.List;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.parsing.nodes.PublicDeclarationNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.singleton;
import static org.zwobble.shed.compiler.naming.FullyQualifiedName.fullyQualifiedName;
import static org.zwobble.shed.compiler.util.ShedIterables.firstOrNone;

public class ModuleGenerator {
    public Modules generateModules(Iterable<SourceNode> sourceNodes) {
        return Modules.build(concat(transform(sourceNodes, sourceToModule())));
    }
    
    private Option<Module> toModule(SourceNode sourceNode) {
        List<String> packageNames = sourceNode.getPackageDeclaration().getPackageNames();
        Option<PublicDeclarationNode> publicDeclaration = firstOrNone(filter(sourceNode.getStatements(), PublicDeclarationNode.class));
        return publicDeclaration.map(publicDeclarationToModule(packageNames));
    }
    
    private Module toModule(PublicDeclarationNode publicDeclaration, List<String> packageNames) {
        String identifier = publicDeclaration.getDeclaration().getIdentifier();
        return Module.create(fullyQualifiedName(ImmutableList.copyOf(concat(packageNames, singleton(identifier)))));
    }

    private Function<SourceNode, Option<Module>> sourceToModule() {
        return new Function<SourceNode, Option<Module>>() {
            @Override
            public Option<Module> apply(SourceNode input) {
                return toModule(input);
            }
        };
    }

    private Function<PublicDeclarationNode, Module> publicDeclarationToModule(final List<String> packageNames) {
        return new Function<PublicDeclarationNode, Module>() {
            @Override
            public Module apply(PublicDeclarationNode input) {
                return toModule(input, packageNames);
            }
        };
    }
}
