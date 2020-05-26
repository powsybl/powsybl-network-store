/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class AttributesSpyer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributesSpyer.class);

    private static final EnumMap<ResourceType, Class> ATTRIBUTES_CLASSES = new EnumMap<>(ResourceType.class);

    private AttributesSpyer() {
    }

    /*
     * Construct an instance object of a subclass of T, dynamically generated with ByteBuddy, for each resource type,
     * and with all setters intercepted to set after the dirty field to true
     */
    public static<T extends IdentifiableAttributes> T spy(T attributes, ResourceType resourceType) {
        T instance = null;
        try {
            Class subClass;
            if (!ATTRIBUTES_CLASSES.containsKey(resourceType)) {
                // create dynamically the subclass
                subClass = new ByteBuddy()
                        .subclass(attributes.getClass())
                        .method(ElementMatchers.isSetter()
                                .and(ElementMatchers.not(ElementMatchers.named("setDirty")))
                                .and(ElementMatchers.not(ElementMatchers.named("setResource"))))
                        .intercept(SuperMethodCall.INSTANCE.andThen(
                                MethodCall.invoke(attributes.getClass().getMethod("setDirty"))
                        )).make()
                        .load(attributes.getClass().getClassLoader())
                        .getLoaded();
                ATTRIBUTES_CLASSES.put(resourceType, subClass);
            } else {
                subClass = ATTRIBUTES_CLASSES.get(resourceType);
            }

            // create the new instance of this subclass
            instance = (T) (subClass.getDeclaredConstructor(attributes.getClass()).newInstance(attributes));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error(e.toString(), e);
        }

        return instance;
    }
}
