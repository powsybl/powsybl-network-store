/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.ResourceType;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
@Getter
@Builder
public class PreloadingStrategy {

    @Builder.Default
    private boolean ignored = false;

    @Builder.Default
    private boolean collection = false;

    @Builder.Default
    private List<PreloadingResource> resources = Collections.emptyList();

    public List<ResourceType> getResourceTypes() {
        return resources.stream().map(PreloadingResource::getType).toList();
    }

    public static PreloadingStrategy none() {
        return PreloadingStrategy.builder().ignored(true).build();
    }

    public static PreloadingStrategy collection() {
        return PreloadingStrategy.builder().collection(true).build();
    }

    public static PreloadingStrategy allCollectionsNeededForBusView() {
        return PreloadingStrategy.builder().resources(List.of(
            BasePreloadingResource.builder().type(ResourceType.SUBSTATION).build(),
            BasePreloadingResource.builder().type(ResourceType.VOLTAGE_LEVEL).build(),
            BasePreloadingResource.builder().type(ResourceType.LOAD).build(),
            BasePreloadingResource.builder().type(ResourceType.GENERATOR).build(),
            BasePreloadingResource.builder().type(ResourceType.BATTERY).build(),
            BasePreloadingResource.builder().type(ResourceType.SHUNT_COMPENSATOR).build(),
            BasePreloadingResource.builder().type(ResourceType.VSC_CONVERTER_STATION).build(),
            BasePreloadingResource.builder().type(ResourceType.LCC_CONVERTER_STATION).build(),
            BasePreloadingResource.builder().type(ResourceType.STATIC_VAR_COMPENSATOR).build(),
            BasePreloadingResource.builder().type(ResourceType.BUSBAR_SECTION).build(), // FIXME this should not be in the list but as connectable visitor also visit busbar sections we need to keep it
            BasePreloadingResource.builder().type(ResourceType.GROUND).build(),
            BasePreloadingResource.builder().type(ResourceType.TWO_WINDINGS_TRANSFORMER).build(),
            BasePreloadingResource.builder().type(ResourceType.THREE_WINDINGS_TRANSFORMER).build(),
            BasePreloadingResource.builder().type(ResourceType.LINE).build(),
            BasePreloadingResource.builder().type(ResourceType.HVDC_LINE).build(),
            BasePreloadingResource.builder().type(ResourceType.DANGLING_LINE).build(),
            BasePreloadingResource.builder().type(ResourceType.TIE_LINE).build())
        ).build();
    }

    public CompletableFuture<Void> loadResources(PreloadingNetworkStoreClient client, UUID networkUuid, int variantNum, Set<ResourceType> loadedResourceTypes) {
        return CompletableFuture.allOf(getResources()
            .stream()
            .map(preloadingResource -> preloadingResource.loadResource(client, networkUuid, variantNum, loadedResourceTypes))
            .toArray(CompletableFuture[]::new));
    }
}
