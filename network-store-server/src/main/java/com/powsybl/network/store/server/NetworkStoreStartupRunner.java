/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.tools.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Component
public class NetworkStoreStartupRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkStoreStartupRunner.class);

    @Override
    public void run(ApplicationArguments args) {
        LOGGER.info(Version.getTableString());
    }
}
