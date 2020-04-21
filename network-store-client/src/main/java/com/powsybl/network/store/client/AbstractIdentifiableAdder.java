/**
 * Copyright (c) 2020, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.util.Identifiables;

/**
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
abstract class AbstractIdentifiableAdder<T extends AbstractIdentifiableAdder<T>> implements Validable {

    protected final NetworkObjectIndex index;

    private String id;

    private boolean ensureIdUnicity = false;

    private String name;

    private boolean fictitious = false;

    AbstractIdentifiableAdder(NetworkObjectIndex index) {
        this.index = index;
    }

    protected NetworkImpl getNetwork() {
        return index.getNetwork();
    }

    protected NetworkObjectIndex getIndex() {
        return index;
    }

    protected abstract String getTypeDescription();

    protected String getId() {
        return id;
    }

    public T setId(String id) {
        this.id = id;
        return (T) this;
    }

    public T setEnsureIdUnicity(boolean ensureIdUnicity) {
        this.ensureIdUnicity = ensureIdUnicity;
        return (T) this;
    }

    protected String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    protected boolean isFictitious() {
        return fictitious;
    }

    public T setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return (T) this;
    }

    protected String checkAndGetUniqueId() {
        if (id == null) {
            throw new PowsyblException(getTypeDescription() + " id is not set");
        }
        String uniqueId;
        if (ensureIdUnicity) {
            uniqueId = Identifiables.getUniqueId(id, getNetwork().getIndex()::contains);
        } else {
            if (getNetwork().getIndex().contains(id)) {
                Identifiable obj = getNetwork().getIndex().getIdentifiable(id);
                throw new PowsyblException("The network " + getNetwork().getId()
                        + " already contains an object '" + obj.getClass().getSimpleName()
                        + "' with the id '" + id + "'");
            }
            uniqueId = id;
        }
        return uniqueId;
    }

    @Override
    public String getMessageHeader() {
        return getTypeDescription() + " '" + id + "': ";
    }
}
