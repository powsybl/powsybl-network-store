/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.commons.PowsyblException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class AttributesSpyer {

    private static final EnumMap<ResourceType, Class> ATTRIBUTES_CLASSES = new EnumMap<>(ResourceType.class);

    private static final String UPDATE_FIELD = "update";

    private AttributesSpyer() {
    }

    /*
     * Construct an instance object of a subclass of T, dynamically generated with ByteBuddy, for each resource type,
     * and with all setters intercepted to set after the dirty field to true
     */
    public static <T extends IdentifiableAttributes> T spy(T attributes, ResourceType resourceType) {
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(resourceType);

        T instance;
        try {
            Class subClass;
            if (!ATTRIBUTES_CLASSES.containsKey(resourceType)) {
                // create dynamically the subclass
                subClass = new ByteBuddy()
                        .subclass(attributes.getClass())
                        .defineField(UPDATE_FIELD, attributes.getClass(), Modifier.PRIVATE)
                        .method(ElementMatchers.isSetter()
                                .and(ElementMatchers.not(ElementMatchers.named("setResource"))))
                        .intercept(SuperMethodCall.INSTANCE
                                .andThen(MethodDelegation.toField(UPDATE_FIELD))
                                .andThen(MethodCall.invoke(attributes.getClass().getMethod("updateResource"))
                        )).make()
                        .load(attributes.getClass().getClassLoader())
                        .getLoaded();
                ATTRIBUTES_CLASSES.put(resourceType, subClass);
            } else {
                subClass = ATTRIBUTES_CLASSES.get(resourceType);
            }

            // create the new instance of this subclass and copy field from original instance
            instance = (T) subClass.getDeclaredConstructor().newInstance();
            Field[] fields = attributes.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(instance, field.get(attributes));
            }

            // set update field
            Field field = subClass.getDeclaredField(UPDATE_FIELD);
            field.setAccessible(true);
            IdentifiableAttributes update = attributes.getClass().getDeclaredConstructor().newInstance();
            field.set(instance, update);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            throw new PowsyblException(e);
        }

        return instance;
    }

    public static <T extends IdentifiableAttributes> T getUpdate(T attributes) {
        Objects.requireNonNull(attributes);
        try {
            Field field = attributes.getClass().getDeclaredField(UPDATE_FIELD);
            field.setAccessible(true);
            return (T) field.get(attributes);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new PowsyblException(e);
        }
    }
}
