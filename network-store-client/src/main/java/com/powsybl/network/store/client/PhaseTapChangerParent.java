package com.powsybl.network.store.client;

import com.powsybl.iidm.network.PhaseTapChanger;

/**
 * @author Abdelsalem HEDHILI <abdelsalem.hedhili at rte-france.com>
 */
interface PhaseTapChangerParent {

    String getTapChangerAttribute();

    void setPhaseTapChanger(PhaseTapChangerImpl ratioTapChanger);

    PhaseTapChanger getPhaseTapChanger();
}
