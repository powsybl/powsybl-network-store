/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl;

import java.util.List;
import java.util.Optional;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.VariantInfos;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public final class StoreClientUtils {

    private StoreClientUtils() { }

    public static int findFistAvailableVariantNum(List<VariantInfos> variantsInfos) {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final int variantNum = i;
            if (variantsInfos.stream().noneMatch(infos -> infos.getNum() == variantNum)) {
                return variantNum;
            }
        }
        throw new PowsyblException("Max number of variant reached: " + Integer.MAX_VALUE);
    }

    public static Optional<VariantInfos> getVariant(String variantId, List<VariantInfos> variantsInfos) {
        return variantsInfos.stream()
            .filter(infos -> infos.getId().equals(variantId))
            .findFirst();
    }

    public static int getVariantNum(String variantId, List<VariantInfos> variantsInfos) {
        return getVariant(variantId, variantsInfos)
            .map(VariantInfos::getNum)
            .orElseThrow(() -> new PowsyblException("Variant '" + variantId + "' not found"));
    }
}
