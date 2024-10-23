/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
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
        Class<? extends ExtensionAttributes> subType = ExtensionLoaders.findLoaderByName(id).getAttributesType();
        return context.constructSpecializedType(superType, subType);
    }
}
