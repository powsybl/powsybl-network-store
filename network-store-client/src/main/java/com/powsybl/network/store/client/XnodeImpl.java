/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.entsoe.util.Xnode;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class XnodeImpl implements Xnode {

    DanglingLineImpl dl;

    public XnodeImpl(DanglingLineImpl dl) {
        this.dl = dl;
    }

    public XnodeImpl(DanglingLineImpl dl, String code) {
        this(dl);
        setCode(code);
    }

    @Override
    public DanglingLine getExtendable() {
        return dl;
    }

    @Override
    public void setExtendable(DanglingLine dl) {
        this.dl = (DanglingLineImpl) dl;
    }

    @Override
    public String getCode() {
        return dl.getUcteXnodeCode();
    }

    @Override
    public Xnode setCode(String code) {
        dl.setUcteXnodeCode(code);
        return this;
    }

}
