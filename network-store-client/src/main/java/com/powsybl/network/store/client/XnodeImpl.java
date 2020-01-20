package com.powsybl.network.store.client;

import com.powsybl.entsoe.util.Xnode;
import com.powsybl.iidm.network.DanglingLine;

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
