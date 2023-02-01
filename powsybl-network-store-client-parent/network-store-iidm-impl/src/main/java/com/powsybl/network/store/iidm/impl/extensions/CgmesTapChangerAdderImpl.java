/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangerAdder;
import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.CgmesTapChangerAttributes;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CgmesTapChangerAdderImpl implements CgmesTapChangerAdder {

    private final CgmesTapChangersImpl<?> tapChangers;

    private String id;
    private String combinedTapChangerId;
    private String type;
    private boolean hidden = false;
    private Integer step;
    private String controlId;

    public CgmesTapChangerAdderImpl(CgmesTapChangersImpl<?> tapChangers) {
        this.tapChangers = Objects.requireNonNull(tapChangers);
    }

    @Override
    public CgmesTapChangerAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setCombinedTapChangerId(String combinedTapChangerId) {
        this.combinedTapChangerId = combinedTapChangerId;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setHiddenStatus(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setStep(int step) {
        this.step = step;
        return this;
    }

    @Override
    public CgmesTapChangerAdder setControlId(String controlId) {
        this.controlId = controlId;
        return this;
    }

    @Override
    public CgmesTapChanger add() {
        if (id == null) {
            throw new PowsyblException("Tap changer ID should not be null");
        }
        if (!hidden) {
            if (step != null) {
                throw new PowsyblException("Non-hidden tap changers step positions can be directly found on the tap changer" +
                        " and should not be forced");
            }
            if (combinedTapChangerId != null) {
                throw new PowsyblException("Non-hidden tap changers do not have a different ID for the combined tap changer");
            }
        }
        if (hidden) {
            if (step == null) {
                throw new PowsyblException("Hidden tap changers step positions should be explicit");
            }
            if (combinedTapChangerId == null) {
                throw new PowsyblException("Hidden tap changers should have an ID for the combined tap changer");
            }
        }
        CgmesTapChangerAttributes attributes = CgmesTapChangerAttributes.builder()
                .id(id)
                .combinedTapChangerId(combinedTapChangerId)
                .type(type)
                .hidden(hidden)
                .step(step)
                .controlId(controlId)
                .build();

        var tapChanger = new CgmesTapChangerImpl(attributes);
        tapChangers.putTapChanger(tapChanger);
        return tapChanger;
    }
}
