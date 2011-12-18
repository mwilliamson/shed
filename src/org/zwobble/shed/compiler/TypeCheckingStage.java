package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.modules.Modules;
import org.zwobble.shed.compiler.parsing.nodes.EntireSourceNode;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.SourceTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerInjector;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typechecker.TypeResults;
import org.zwobble.shed.compiler.typegeneration.TypeStore;

import com.google.common.base.Function;
import com.google.inject.Injector;

import static com.google.common.collect.Iterables.transform;

public class TypeCheckingStage implements CompilerStage {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        SourceTypeChecker sourceTypeChecker = sourceTypeChecker(data);
        EntireSourceNode sourceNodes = data.get(CompilationDataKeys.sourceNodes);
        TypeResult<?> typeCheckResult = TypeResults.combine(transform(sourceNodes, typeCheck(sourceTypeChecker)));
        return CompilerStageResult.create(typeCheckResult.getErrors());
    }

    private Function<SourceNode, TypeResult<?>> typeCheck(final SourceTypeChecker sourceTypeChecker) {
        return new Function<SourceNode, TypeResult<?>>() {
            @Override
            public TypeResult<?> apply(SourceNode input) {
                return sourceTypeChecker.typeCheck(input);
            }
        };
    }

    private SourceTypeChecker sourceTypeChecker(CompilationData data) {
        TypeStore types = data.get(CompilationDataKeys.generatedTypes);
        MetaClasses metaClasses = data.get(CompilationDataKeys.metaClasses);
        StaticContext context = data.get(CompilationDataKeys.staticContext);
        References references = data.get(CompilationDataKeys.references);
        Modules modules = data.get(CompilationDataKeys.modules);
        Injector typeCheckerInjector = TypeCheckerInjector.build(types, metaClasses, context, references, modules);
        SourceTypeChecker sourceTypeChecker = typeCheckerInjector.getInstance(SourceTypeChecker.class);
        return sourceTypeChecker;
    }

}
