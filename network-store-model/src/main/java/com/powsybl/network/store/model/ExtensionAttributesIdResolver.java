package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class ExtensionAttributesIdResolver extends TypeIdResolverBase {
    private JavaType superType;

    @Override
    public void init(JavaType baseType) {
        superType = baseType;
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }

    @Override
    public String idFromValue(Object obj) {
        return idFromValueAndType(obj, obj.getClass());
    }

    @Override
    public String idFromValueAndType(Object obj, Class<?> subType) {
        return ExtensionLoaders.findLoaderByAttributes(subType).getName();
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        Class<? extends ExtensionAttributes> subType = ExtensionLoaders.findLoader(id).getAttributesType();
        return context.constructSpecializedType(superType, subType);
    }
}
