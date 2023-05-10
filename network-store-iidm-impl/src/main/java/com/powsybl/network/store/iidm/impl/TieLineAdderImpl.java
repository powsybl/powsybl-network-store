/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class TieLineAdderImpl extends AbstractIdentifiableAdder<TieLineAdderImpl> implements TieLineAdder {

    String half1;
    String half2;

    public TieLineAdderImpl(NetworkObjectIndex index) {
        super(index);
    }

    @Override
    public TieLineAdder setDanglingLine1(String danglingLine1) {
        half1 = danglingLine1;
        return this;
    }

    @Override
    public TieLineAdder setDanglingLine2(String danglingLine2) {
        half2 = danglingLine2;
        return this;
    }

    @Override
    public TieLine add() {
        String id = checkAndGetUniqueId();
        if (half1 == null || half2 == null) {
            throw new ValidationException(this, "undefined dangling line");
        }

        DanglingLineImpl dl1 = index.getDanglingLine(half1).orElseThrow(() -> new ValidationException(this, half1 + " are not dangling lines in the network"));
        DanglingLineImpl dl2 = index.getDanglingLine(half2).orElseThrow(() -> new ValidationException(this, half2 + " are not dangling lines in the network"));

        Resource<TieLineAttributes> resource = Resource.tieLineBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(TieLineAttributes.builder()
                        .half1Id(dl1.getId())
                        .half2Id(dl2.getId())
                        .build()).build();
        getIndex().createTieLine(resource);
        TieLineImpl tieLine = new TieLineImpl(getIndex(), resource);
        dl1.setParent(tieLine, Branch.Side.ONE);
        dl2.setParent(tieLine, Branch.Side.TWO);
        return tieLine;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.TIE_LINE.getDescription();
    }
}
