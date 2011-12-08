package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.SourceNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.tokeniser.TokenisedSource;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typegeneration.TypeStore;

public class CompilationDataKeys {
    public static final CompilationDataKey<String> sourceString = CompilationDataKey.key();
    public static final CompilationDataKey<TokenisedSource> tokenisedSource = CompilationDataKey.key();
    public static final CompilationDataKey<NodeLocations> nodeLocations = CompilationDataKey.key();
    public static final CompilationDataKey<SourceNode> sourceNode = CompilationDataKey.key();
    public static final CompilationDataKey<StaticContext> staticContext = CompilationDataKey.key();
    public static final CompilationDataKey<References> references = CompilationDataKey.key();
    public static final CompilationDataKey<TypeStore> generatedTypes = CompilationDataKey.key();
    public static final CompilationDataKey<MetaClasses> metaClasses = CompilationDataKey.key();
    public static final CompilationDataKey<FullyQualifiedNames> fullyQualifiedNames = CompilationDataKey.key();
    public static final CompilationDataKey<JavaScriptNode> generatedJavaScript = CompilationDataKey.key();
    public static final CompilationDataKey<String> generatedJavaScriptAsString = CompilationDataKey.key();
}
