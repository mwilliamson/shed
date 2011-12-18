package org.zwobble.shed.compiler.typechecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.ToString;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.metaclassgeneration.MetaClasses;
import org.zwobble.shed.compiler.naming.FullyQualifiedName;
import org.zwobble.shed.compiler.parsing.nodes.Declaration;
import org.zwobble.shed.compiler.parsing.nodes.Identity;
import org.zwobble.shed.compiler.types.ClassType;
import org.zwobble.shed.compiler.types.CoreTypes;
import org.zwobble.shed.compiler.types.InterfaceType;
import org.zwobble.shed.compiler.types.Interfaces;
import org.zwobble.shed.compiler.types.ScalarType;
import org.zwobble.shed.compiler.types.ScalarTypeInfo;
import org.zwobble.shed.compiler.types.Type;
import org.zwobble.shed.compiler.types.TypeApplication;
import org.zwobble.shed.compiler.types.TypeInfoTypeReplacer;
import org.zwobble.shed.compiler.types.TypeReplacer;
import org.zwobble.shed.compiler.util.ShedMaps;

import com.google.common.base.Function;

import static com.google.common.collect.Lists.newArrayList;
import static org.zwobble.shed.compiler.typechecker.ShedTypeValue.shedTypeValue;
import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
import static org.zwobble.shed.compiler.types.Members.members;

@ToString
public class StaticContext {
    private final Map<FullyQualifiedName, Type> global = new HashMap<FullyQualifiedName, Type>();
    private final Map<Identity<Declaration>, ValueInfo> types = new HashMap<Identity<Declaration>, ValueInfo>();
    private final Map<ScalarType, ScalarTypeInfo> scalarTypeInfo = new HashMap<ScalarType, ScalarTypeInfo>();
    private final MetaClasses metaClasses;
    
    public StaticContext(MetaClasses metaClasses) {
        this.metaClasses = metaClasses;
    }
    
    public void add(Declaration declaration, ValueInfo type) {
        types.put(new Identity<Declaration>(declaration), type);
    }

    public Option<ValueInfo> getValueInfoFor(Declaration declaration) {
        Identity<Declaration> key = new Identity<Declaration>(declaration);
        return ShedMaps.getOrNone(types, key);
    }
    
    public Option<Type> getTypeOf(Declaration declaration) {
        return getValueInfoFor(declaration).map(toType());
    }

    @Deprecated
    public void addGlobal(FullyQualifiedName name, Type type) {
        global.put(name, type);
    }
    
    public Option<Type> lookupGlobal(FullyQualifiedName name) {
        return ShedMaps.getOrNone(global, name);
    }
    
    public void addInfo(ScalarType type, ScalarTypeInfo typeInfo) {
        scalarTypeInfo.put(type, typeInfo);
    }
    
    public ScalarTypeInfo getInfo(ScalarType scalarType) {
        if (scalarType instanceof TypeApplication) {
            return getTypeApplicationInfo((TypeApplication)scalarType);
        } else {
            if (!scalarTypeInfo.containsKey(scalarType)) {
                throw new RuntimeException("No type info for " + scalarType);
            }
            return scalarTypeInfo.get(scalarType);
        }
    }

    private ScalarTypeInfo getTypeApplicationInfo(TypeApplication typeApplication) {
        return new TypeInfoTypeReplacer(new TypeReplacer()).buildTypeInfo(typeApplication, getInfo(typeApplication.getParameterisedType().getBaseType()));
    }

    public void addClass(Declaration declaration, ClassType type, Iterable<Type> classParameters, ScalarTypeInfo classTypeInfo) {
        List<Type> functionTypeParameters = newArrayList(classParameters);
        functionTypeParameters.add(type);
        addScalarType(declaration, type, classTypeInfo, interfaces(CoreTypes.functionTypeOf(functionTypeParameters), CoreTypes.CLASS));
    }

    public void addClass(Declaration declaration, ClassType type, ScalarTypeInfo typeInfo) {
        addScalarType(declaration, type, typeInfo, interfaces(CoreTypes.CLASS));
    }

    public void addInterface(Declaration declaration, InterfaceType type, ScalarTypeInfo typeInfo) {
        addScalarType(declaration, type, typeInfo, interfaces(CoreTypes.CLASS));
    }
    
    private void addScalarType(Declaration declaration, ScalarType type, ScalarTypeInfo typeInfo, Interfaces interfaces) {
        ClassType metaClass = metaClasses.metaClassOf(type);
        ScalarTypeInfo metaClassTypeInfo = new ScalarTypeInfo(interfaces, members());
        
        add(declaration, ValueInfo.unassignableValue(metaClass, shedTypeValue(type)));
        addInfo(type, typeInfo);
        addInfo(metaClass, metaClassTypeInfo);
    }

    private Function<ValueInfo, Type> toType() {
        return new Function<ValueInfo, Type>() {
            @Override
            public Type apply(ValueInfo input) {
                return input.getType();
            }
        };
    }
}
