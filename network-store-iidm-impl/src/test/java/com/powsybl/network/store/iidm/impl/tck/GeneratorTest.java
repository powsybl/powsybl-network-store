/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.tck.AbstractGeneratorTest;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ExtendWith(ExcludeTestsExtension.class)
class GeneratorTest extends AbstractGeneratorTest {

    private Generator createGenerator(VoltageLevel voltageLevel, String id, EnergySource source, double maxP, double minP, double ratedS,
                                      double activePowerSetpoint, double reactivePowerSetpoint, boolean regulatorOn, double voltageSetpoint) {
        return createGeneratorAdder(voltageLevel, id, source, maxP, minP, ratedS, activePowerSetpoint, reactivePowerSetpoint, regulatorOn, voltageSetpoint)
                .add();
    }

    private GeneratorAdder createGeneratorAdder(VoltageLevel voltageLevel, String id, EnergySource source, double maxP, double minP, double ratedS,
                                                double activePowerSetpoint, double reactivePowerSetpoint, boolean regulatorOn, double voltageSetpoint) {
        return voltageLevel.newGenerator()
                .setId(id)
                .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(regulatorOn)
                .add()
                .setEnergySource(source)
                .setMaxP(maxP)
                .setMinP(minP)
                .setRatedS(ratedS)
                .setTargetP(activePowerSetpoint)
                .setLocalTargetQ(reactivePowerSetpoint)
                .setNode(1)
                .setLocalTargetV(voltageSetpoint);
    }

    //FIXME: Override of testNewVoltageRegulationInMultiVariants(). To be removed when testNewVoltageRegulationInMultiVariants() asserts on values, not reference
    @Test
    void testNewVoltageRegulationInMultiVariantsByValues() {
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("C");
        Generator generator = createGenerator(voltageLevel, "testMultiVariant", EnergySource.HYDRO, 20.0, 11., 2.0,
                15.0, 40.0, true, 2.0);

        VoltageRegulationTestUtils.assertNewVoltageRegulationInMultiVariants(network, generator);
    }
}
