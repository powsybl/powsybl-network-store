/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.Lists;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.entsoe.util.EntsoeArea;
import com.powsybl.entsoe.util.EntsoeAreaImpl;
import com.powsybl.entsoe.util.EntsoeGeographicalCode;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.EntsoeAreaAttributes;
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
        Country oldValue = resource.getAttributes().getCountry();
        resource.getAttributes().setCountry(country);
        index.notifyUpdate(this, "country", oldValue, country);
        return this;
    }

    @Override
    public String getTso() {
        return resource.getAttributes().getTso();
    }

    @Override
    public Substation setTso(String tso) {
        String oldValue = resource.getAttributes().getTso();
        resource.getAttributes().setTso(tso);
        index.notifyUpdate(this, "tso", oldValue, tso);
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
        if (tag == null) {
            throw new ValidationException(this, "geographical tag is null");
        }
        resource.getAttributes().getGeographicalTags().add(tag);
        index.notifyElementAdded(this, "geographicalTags", tag);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder newTwoWindingsTransformer() {
        return new TwoWindingsTransformerAdderImpl(index, this);
    }

    @Override
    public List<TwoWindingsTransformer> getTwoWindingsTransformers() {
        Set<TwoWindingsTransformer> twoWindingsTransformers = new LinkedHashSet<>();
        for (VoltageLevel vl : getVoltageLevels()) {
            twoWindingsTransformers.addAll(index.getTwoWindingsTransformers(vl.getId()));
        }
        return new ArrayList<>(twoWindingsTransformers);
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
        return new ThreeWindingsTransformerAdderImpl(index, this);
    }

    @Override
    public List<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        Set<ThreeWindingsTransformer> threeWindingsTransformers = new LinkedHashSet<>();
        for (VoltageLevel vl : getVoltageLevels()) {
            threeWindingsTransformers.addAll(index.getThreeWindingsTransformers(vl.getId()));
        }
        return new ArrayList<>(threeWindingsTransformers);

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
        return resource.getAttributes().getGeographicalTags();
    }

    @Override
    public <E extends Extension<Substation>> void addExtension(Class<? super E> type, E extension) {
        if (type == EntsoeArea.class) {
            EntsoeArea entsoeArea = (EntsoeArea) extension;
            resource.getAttributes().setEntsoeArea(
                    EntsoeAreaAttributes.builder()
                            .code(entsoeArea.getCode().toString())
                            .build());
        }
        super.addExtension(type, extension);
    }

    @Override
    public <E extends Extension<Substation>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createEntsoeArea();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<Substation>> E getExtension(Class<? super E> type) {
        if (type == EntsoeArea.class) {
            return (E) createEntsoeArea();
        }
        return super.getExtension(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<Substation>> E getExtensionByName(String name) {
        if (name.equals("entsoeArea")) {
            return (E) createEntsoeArea();
        }
        return super.getExtensionByName(name);
    }

    private <E extends Extension<Substation>> E createEntsoeArea() {
        E extension = null;
        if (resource.getAttributes().getEntsoeArea() != null) {
            extension = (E) new EntsoeAreaImpl(this,
                    EntsoeGeographicalCode.valueOf(resource.getAttributes().getEntsoeArea().getCode()));
        }
        return extension;
    }

    @Override
    public void remove() {
        SubstationUtil.checkRemovability(this);

        for (VoltageLevel vl : getVoltageLevels()) {
            // Remove all branches, transformers and HVDC lines
            List<Connectable> connectables = Lists.newArrayList(vl.getConnectables());
            for (Connectable connectable : connectables) {
                ConnectableType type = connectable.getType();
                if (VoltageLevelUtil.MULTIPLE_TERMINALS_CONNECTABLE_TYPES.contains(type)) {
                    connectable.remove();
                } else if (type == ConnectableType.HVDC_CONVERTER_STATION) {
                    HvdcLine hvdcLine = getNetwork().getHvdcLine((HvdcConverterStation) connectable);
                    if (hvdcLine != null) {
                        hvdcLine.remove();
                    }
                }
            }

            // Then remove the voltage level (bus, switches and injections) from the network
            vl.remove();
        }

        // Remove this substation from the network
        index.removeSubstation(getId());

        index.notifyRemoval(this);

    }

    @Override
    protected String getTypeDescription() {
        return "Substation";
    }
}
