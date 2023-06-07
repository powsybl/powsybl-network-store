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

    private String danglingLine1;
    private String danglingLine2;

    public TieLineAdderImpl(NetworkObjectIndex index) {
        super(index);
    }

    @Override
    public TieLineAdder setDanglingLine1(String danglingLine1) {
        this.danglingLine1 = danglingLine1;
        return this;
    }

    @Override
    public TieLineAdder setDanglingLine2(String danglingLine2) {
        this.danglingLine2 = danglingLine2;
        return this;
    }

    @Override
    public TieLine add() {
        String id = checkAndGetUniqueId();
        if (danglingLine1 == null || danglingLine2 == null) {
            throw new ValidationException(this, "undefined dangling line");
        }

        DanglingLineImpl dl1 = index.getDanglingLine(danglingLine1).orElseThrow(() -> new ValidationException(this, danglingLine1 + " are not dangling lines in the network"));
        DanglingLineImpl dl2 = index.getDanglingLine(danglingLine2).orElseThrow(() -> new ValidationException(this, danglingLine2 + " are not dangling lines in the network"));

        Resource<TieLineAttributes> resource = Resource.tieLineBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(TieLineAttributes.builder()
                        .danglingLine1Id(dl1.getId())
                        .danglingLine2Id(dl2.getId())
                        .build()).build();
        getIndex().createTieLine(resource);
        TieLineImpl tieLine = new TieLineImpl(getIndex(), resource);
        dl1.setTieLine(tieLine);
        dl2.setTieLine(tieLine);
        return tieLine;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.TIE_LINE.getDescription();
    }
}
