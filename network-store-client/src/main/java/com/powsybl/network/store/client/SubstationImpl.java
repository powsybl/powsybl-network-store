/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SubstationAttributes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SubstationImpl extends AbstractIdentifiableImpl<Substation, SubstationAttributes> implements Substation {

    public SubstationImpl(NetworkObjectIndex index, Resource<SubstationAttributes> resource) {
        super(index, resource);
    }

    static SubstationImpl create(NetworkObjectIndex index, Resource<SubstationAttributes> resource) {
        return new SubstationImpl(index, resource);
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.SUBSTATION;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public NetworkImpl getNetwork() {
        return index.getNetwork();
    }

    @Override
    public Optional<Country> getCountry() {
        return Optional.ofNullable(resource.getAttributes().getCountry());
    }

    @Override
    public Country getNullableCountry() {
        return resource.getAttributes().getCountry();
    }

    @Override
    public Substation setCountry(Country country) {
        resource.getAttributes().setCountry(country);
        return this;
    }

    @Override
    public String getTso() {
        return resource.getAttributes().getTso();
    }

    @Override
    public Substation setTso(String tso) {
        resource.getAttributes().setTso(tso);
        return this;
    }

    @Override
    public VoltageLevelAdder newVoltageLevel() {
        return new VoltageLevelAdderImpl(index, resource);
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return index.getVoltageLevels(resource.getId()).stream();
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return getVoltageLevelStream().collect(Collectors.toList());
    }

    @Override
    public Substation addGeographicalTag(String tag) {
        // TODO
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder newTwoWindingsTransformer() {
        return new TwoWindingsTransformerAdderImpl(index);
    }

    @Override
    public List<TwoWindingsTransformer> getTwoWindingsTransformers() {
        List<TwoWindingsTransformer> twoWindingsTransformers = new ArrayList<>();
        for (VoltageLevel vl : getVoltageLevels()) {
            twoWindingsTransformers.addAll(index.getTwoWindingsTransformers(vl.getId()));
        }
        return twoWindingsTransformers;
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getTwoWindingsTransformers().stream();
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return getTwoWindingsTransformers().size();
    }

    @Override
    public ThreeWindingsTransformerAdder newThreeWindingsTransformer() {
        return new ThreeWindingsTransformerAdderImpl(index);
    }

    @Override
    public List<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        List<ThreeWindingsTransformer> threeWindingsTransformers = new ArrayList<>();
        for (VoltageLevel vl : getVoltageLevels()) {
            threeWindingsTransformers.addAll(index.getThreeWindingsTransformers(vl.getId()));
        }
        return threeWindingsTransformers;

    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getThreeWindingsTransformers().stream();
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return getThreeWindingsTransformers().size();
    }

    @Override
    public Set<String> getGeographicalTags() {
        return Collections.emptySet();
    }
}
