package org.zwobble.shed.compiler;

import org.zwobble.shed.compiler.codegenerator.javascript.JavaScriptNode;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.modules.Modules;
import org.zwobble.shed.compiler.naming.FullyQualifiedNames;
import org.zwobble.shed.compiler.parsing.NodeLocations;
import org.zwobble.shed.compiler.parsing.nodes.EntireSourceNode;
import org.zwobble.shed.compiler.referenceresolution.References;
import org.zwobble.shed.compiler.tokeniser.TokenisedSource;
import org.zwobble.shed.compiler.typechecker.BuiltIns;
import org.zwobble.shed.compiler.typechecker.StaticContext;
import org.zwobble.shed.compiler.typegeneration.TypeStore;

public class CompilationDataKeys {
    public static final CompilationDataKey<Iterable<String>> sourceStrings = CompilationDataKey.key();
    public static final CompilationDataKey<Iterable<TokenisedSource>> tokenisedSources = CompilationDataKey.key();
    public static final CompilationDataKey<NodeLocations> nodeLocations = CompilationDataKey.key();
    public static final CompilationDataKey<EntireSourceNode> unorderedSourceNodes = CompilationDataKey.key();
    public static final CompilationDataKey<EntireSourceNode> sourceNodes = CompilationDataKey.key();
    public static final CompilationDataKey<BuiltIns> builtIns = CompilationDataKey.key();
    public static final CompilationDataKey<StaticContext> staticContext = CompilationDataKey.key();
    public static final CompilationDataKey<References> references = CompilationDataKey.key();
    public static final CompilationDataKey<TypeStore> generatedTypes = CompilationDataKey.key();
    public static final CompilationDataKey<MetaClasses> metaClasses = CompilationDataKey.key();
    public static final CompilationDataKey<FullyQualifiedNames> fullyQualifiedNames = CompilationDataKey.key();
    public static final CompilationDataKey<Iterable<JavaScriptNode>> generatedJavaScript = CompilationDataKey.key();
    public static final CompilationDataKey<String> generatedJavaScriptAsString = CompilationDataKey.key();
    public static final CompilationDataKey<Modules> modules = CompilationDataKey.key();
}
