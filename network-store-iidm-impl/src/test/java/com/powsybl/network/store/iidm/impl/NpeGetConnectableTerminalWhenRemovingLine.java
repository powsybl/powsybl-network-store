package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DefaultTopologyVisitor;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.Test;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NpeGetConnectableTerminalWhenRemovingLine {

    @Test
    public void test() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.getLine("LINE_S2S3").remove();
        VoltageLevel vl = network.getVoltageLevel("S2VL1");
        for (Bus b : vl.getBusView().getBuses()) {
            b.visitConnectedEquipments(new DefaultTopologyVisitor());
        }
    }
}
