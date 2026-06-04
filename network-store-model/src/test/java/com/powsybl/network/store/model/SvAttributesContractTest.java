/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
class SvAttributesContractTest {

    @Test
    void jsonViewSvFieldsShouldMatchDedicatedSvAttributes() {
        for (ResourceType resourceType : ResourceType.values()) {
            // Get SV attributes for resource type
            Class<? extends Attributes> svAttributesType = ResourceDeserializer.getTypeClass(resourceType, AttributeFilter.SV);

            // Get FULL attributes for resource type
            Class<? extends Attributes> fullAttributesType = ResourceDeserializer.getTypeClass(resourceType, AttributeFilter.FULL);

            // Continue if there is no @JsonView(AttributeFilter.JsonViews.OnlySv.class) in the FULL attribute
            Set<String> jsonViewSvAnnotatedFieldNames = getJsonViewSvAnnotatedFieldNames(fullAttributesType);
            if (jsonViewSvAnnotatedFieldNames.isEmpty()) {
                continue;
            }

            // Check that an attribute with @JsonView(AttributeFilter.JsonViews.OnlySv.class) fields has an SV attributes class
            assertNotEquals(
                    fullAttributesType,
                    svAttributesType,
                    () -> resourceType + " declares OnlySv fields in " + fullAttributesType.getSimpleName()
                            + " but has no dedicated SV attributes class"
            );

            // Check that all annotated @JsonView(AttributeFilter.JsonViews.OnlySv.class) fields in the FULL view are present in the SV attributes and reciprocally
            assertEquals(
                    jsonViewSvAnnotatedFieldNames,
                    getFieldNames(svAttributesType),
                    () -> resourceType + " should map " + fullAttributesType.getSimpleName()
                            + " OnlySv fields to " + svAttributesType.getSimpleName()
            );
        }
    }

    private static Set<String> getJsonViewSvAnnotatedFieldNames(Class<?> attributesType) {
        return Arrays.stream(attributesType.getDeclaredFields())
                .filter(SvAttributesContractTest::isJsonViewSvAnnotatedField)
                .map(Field::getName)
                .collect(Collectors.toSet());
    }

    private static boolean isJsonViewSvAnnotatedField(Field field) {
        JsonView jsonView = field.getAnnotation(JsonView.class);
        return jsonView != null && Arrays.asList(jsonView.value()).contains(AttributeFilter.JsonViews.OnlySv.class);
    }

    private static Set<String> getFieldNames(Class<?> attributesType) {
        return Arrays.stream(attributesType.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }
}
