/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model.utils;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public final class Views {

    private Views() { }

    // ---------------------------------------------------
    // some specific views containing a subpart of the data
    // contains only p and q of the element
    public interface SvView {
    }

    // view containing only operational limits groups
    public interface Limits {
    }

    // ---------------------------------------------------
    // view containing main data
    // contains all direct attributes of the element
    // attributes containing large list and objects are not included
    public interface Other {

    }

    public interface Standard extends SvView, Other {
    }

    // ---------------------------------------------------
    // combining some specific views with basic view
    // contains all basic attributes plus operational limits groups
    public interface WithLimits extends Standard, Limits {
    }
}
