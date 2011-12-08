package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.typebinding.TypeBinder;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typegeneration.TypeStore;

public class TypeBindingStage implements CompilerStage  {
    @Override
    public CompilerStageResult execute(CompilationData data) {
        MetaClasses metaClasses = data.get(CompilationDataKeys.metaClasses);
        TypeBinder typeBinder = new TypeBinder(metaClasses);
        TypeStore generatedTypes = data.get(CompilationDataKeys.generatedTypes);
        StaticContext context = data.get(CompilationDataKeys.staticContext);
        typeBinder.bindTypes(generatedTypes, context);
        return CompilerStageResult.create();
    }
}
