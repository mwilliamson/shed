package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.typechecker.SourceTypeChecker;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typechecker.TypeCheckerInjector;
import org.zwobble.shed.compiler.typechecker.TypeResult;
import org.zwobble.shed.compiler.typegeneration.TypeStore;

import com.google.inject.Injector;

public class TypeCheckingStage implements CompilerStage {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        SourceTypeChecker sourceTypeChecker = sourceTypeChecker(data);
        SourceNode sourceNode = data.get(CompilationDataKeys.sourceNode);
        TypeResult<Void> typeCheckResult = sourceTypeChecker.typeCheck(sourceNode);
        return CompilerStageResult.create(typeCheckResult.getErrors());
    }

    private SourceTypeChecker sourceTypeChecker(CompilationData data) {
        TypeStore types = data.get(CompilationDataKeys.generatedTypes);
        MetaClasses metaClasses = data.get(CompilationDataKeys.metaClasses);
        StaticContext context = data.get(CompilationDataKeys.staticContext);
        References references = data.get(CompilationDataKeys.references);
        Injector typeCheckerInjector = TypeCheckerInjector.build(types, metaClasses, context, references);
        SourceTypeChecker sourceTypeChecker = typeCheckerInjector.getInstance(SourceTypeChecker.class);
        return sourceTypeChecker;
    }

}
