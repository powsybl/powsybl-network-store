package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubnetworkImpl extends AbstractNetwork<SubnetworkAttributes> {

    SubnetworkImpl(NetworkObjectIndex index, Resource<SubnetworkAttributes> resource) {
        super(index, resource);
    }

    private boolean contains(Identifiable<?> identifiable) {
        AbstractIdentifiableImpl abstractIdentifiable = (AbstractIdentifiableImpl) identifiable;
        if (abstractIdentifiable.getType().equals(IdentifiableType.NETWORK) && abstractIdentifiable != this) {
            return false;
        }
        if (abstractIdentifiable.getResource().getParentNetwork() == null) {
            System.out.println("Null parent");
        }
        return abstractIdentifiable == this ||
                abstractIdentifiable != null && abstractIdentifiable.getResource().getParentNetwork().equals(this.getId());
    }

    static SubnetworkImpl createSubnetwork(NetworkObjectIndex index, Resource<SubnetworkAttributes> resource) {
        return new SubnetworkImpl(index, resource);
    }

    @Override
    public SubstationAdder newSubstation() {
        return new SubstationAdderImpl(index, this.getId());
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return getNetwork().getSubstationStream().filter(this::contains);
    }

    @Override
    public Substation getSubstation(String s) {
        Substation substation = getNetwork().getSubstation(s);
        return contains(substation) ? substation : null;
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return getVoltageLevelStream().toList();
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return index.getVoltageLevels().stream().filter(this::contains);
    }

    @Override
    public int getVoltageLevelCount() {
        return (int)getVoltageLevelStream().count();
    }

    @Override
    public VoltageLevel getVoltageLevel(String id) {
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(id);
        return contains(voltageLevel) ? voltageLevel : null;
    }

    @Override
    public VoltageLevelAdder newVoltageLevel() {
        return new VoltageLevelAdderImpl(index, null, this.getId());
    }

    @Override
    public LineAdder newLine() {
        return new LineAdderImpl(index, this.getId());
    }

    @Override
    public Iterable<Line> getLines() {
        return getLineStream().toList();
    }

    @Override
    public Stream<Line> getLineStream() {
        return index.getLines().stream().filter(this::contains);
    }

    @Override
    public int getLineCount() {
        return (int) getLineStream().count();
    }

    @Override
    public Line getLine(String id) {
        Line line = getNetwork().getLine(id);
        return contains(line) ? line : null;
    }

    @Override
    public Iterable<TieLine> getTieLines() {
        return getTieLineStream().toList();
    }

    @Override
    public Stream<TieLine> getTieLineStream() {
        return index.getTieLines().stream().filter(this::contains);
    }

    @Override
    public int getTieLineCount() {
        return (int) getTieLineStream().count();
    }

    @Override
    public TieLine getTieLine(String id) {
        Optional<TieLineImpl> tieLine = index.getTieLine(id);
        if (tieLine.isPresent() && contains(tieLine.get())) {
            return tieLine.get();
        } else {
            return null;
        }
    }

    @Override
    public TieLineAdder newTieLine() {
        return new TieLineAdderImpl(index, this.getId());
    }

    @Override
    public List<Branch> getBranches() {
        return ImmutableList.<Branch>builder()
                .addAll(getLines())
                .addAll(getTwoWindingsTransformers())
                .addAll(getTieLines())
                .build();
    }

    @Override
    public Branch getBranch(String id) {
        Branch<?> b = index.getBranch(id);
        if (b != null && contains(b)) {
            return b;
        } else {
            return null;
        }
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return getBranches().stream();
    }

    @Override
    public int getBranchCount() {
        return (int) getBranchStream().count();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getTwoWindingsTransformerStream().toList();
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return index.getTwoWindingsTransformers().stream().filter(this::contains);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return (int) getTwoWindingsTransformerStream().count();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        Optional<TwoWindingsTransformerImpl> transformer = index.getTwoWindingsTransformer(id);
        if (transformer.isPresent() && contains(transformer.get())) {
            return transformer.get();
        } else {
            return null;
        }
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getThreeWindingsTransformerStream().toList();
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return index.getThreeWindingsTransformers().stream().filter(this::contains);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return (int) getThreeWindingsTransformerStream().count();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        Optional<ThreeWindingsTransformerImpl> transformer = index.getThreeWindingsTransformer(id);
        if (transformer.isPresent() && contains(transformer.get())) {
            return transformer.get();
        } else {
            return null;
        }
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getGeneratorStream().toList();
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return index.getGenerators().stream().filter(this::contains);
    }

    @Override
    public int getGeneratorCount() {
        return (int) getGeneratorStream().count();
    }

    @Override
    public Generator getGenerator(String id) {
        return index.getGenerator(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getBatteryStream().toList();
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return index.getBatteries().stream().filter(this::contains);
    }

    @Override
    public int getBatteryCount() {
        return (int) getBatteryStream().count();
    }

    @Override
    public Battery getBattery(String id) {
        return index.getBattery(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<Load> getLoads() {
        return getLoadStream().toList();
    }

    @Override
    public Stream<Load> getLoadStream() {
        return index.getLoads().stream().filter(this::contains);
    }

    @Override
    public int getLoadCount() {
        return (int) getLoadStream().count();
    }

    @Override
    public Load getLoad(String id) {
        return index.getLoad(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getShuntCompensatorStream().toList();
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return index.getShuntCompensators().stream().filter(this::contains);
    }

    @Override
    public int getShuntCompensatorCount() {
        return (int) getShuntCompensatorStream().count();
    }

    @Override
    public ShuntCompensator getShuntCompensator(String id) {
        return index.getShuntCompensator(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream(danglingLineFilter).toList();
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        return index.getDanglingLines().stream().filter(danglingLineFilter.getPredicate()).filter(this::contains);
    }

    @Override
    public int getDanglingLineCount() {
        return (int) index.getDanglingLines().stream().filter(this::contains).count();
    }

    @Override
    public DanglingLine getDanglingLine(String id) {
        return index.getDanglingLine(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getStaticVarCompensatorStream().toList();
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return index.getStaticVarCompensators().stream().filter(this::contains);
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return (int) getStaticVarCompensatorStream().count();
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(String id) {
        return index.getStaticVarCompensator(id).filter(this::contains).orElse(null);
    }

    @Override
    public Switch getSwitch(String id) {
        return index.getSwitch(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return getSwitchStream().toList();
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return index.getSwitches().stream().filter(this::contains);
    }

    @Override
    public int getSwitchCount() {
        return (int) getSwitchStream().count();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        return index.getBusbarSection(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return getBusbarSectionStream().toList();
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return index.getBusbarSections().stream().filter(this::contains);
    }

    @Override
    public int getBusbarSectionCount() {
        return (int) getBusbarSectionStream().count();
    }

    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return getHvdcConverterStationStream().toList();
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        List<HvdcConverterStation<?>> hvdcConverterStationsList = new ArrayList<>();
        hvdcConverterStationsList.addAll(index.getLccConverterStations());
        hvdcConverterStationsList.addAll(index.getVscConverterStations());
        return hvdcConverterStationsList.stream().filter(this::contains);
    }

    @Override
    public int getHvdcConverterStationCount() {
        return (int) getHvdcConverterStationStream().count();
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(String id) {
        return index.getHvdcConverterStation(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().toList();
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return index.getLccConverterStations().stream().filter(this::contains);
    }

    @Override
    public int getLccConverterStationCount() {
        return (int) getLccConverterStationStream().count();
    }

    @Override
    public LccConverterStation getLccConverterStation(String id) {
        return index.getLccConverterStation(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getVscConverterStationStream().toList();
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return index.getVscConverterStations().stream().filter(this::contains);
    }

    @Override
    public int getVscConverterStationCount() {
        return (int) getVscConverterStationStream().count();
    }

    @Override
    public VscConverterStation getVscConverterStation(String id) {
        return index.getVscConverterStation(id).filter(this::contains).orElse(null);
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return getHvdcLineStream().toList();
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return index.getHvdcLines().stream().filter(this::contains);
    }

    @Override
    public int getHvdcLineCount() {
        return (int) getHvdcLineStream().count();
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        return index.getHvdcLine(id).filter(this::contains).orElse(null);
    }

    @Override
    public HvdcLineAdder newHvdcLine() {
        return new HvdcLineAdderImpl(index, this.getId());
    }

    @Override
    public HvdcLine getHvdcLine(HvdcConverterStation converterStation) {
        return getHvdcLineStream()
                .filter(l -> l.getConverterStation1() == converterStation || l.getConverterStation2() == converterStation)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        Identifiable<?> identifiable = index.getIdentifiable(id);
        return contains(identifiable) ? identifiable : null;
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return index.getIdentifiables().stream().filter(this::contains).toList();
    }

    @Override
    public VoltageAngleLimitAdder newVoltageAngleLimit() {
        return null;
    }

    @Override
    public Iterable<VoltageAngleLimit> getVoltageAngleLimits() {
        return null;
    }

    @Override
    public Stream<VoltageAngleLimit> getVoltageAngleLimitsStream() {
        return null;
    }

    @Override
    public VoltageAngleLimit getVoltageAngleLimit(String s) {
        return null;
    }

    @Override
    public Network createSubnetwork(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Network detach() {
        Set<Identifiable<?>> boundaryElements = getBoundaryElements();
        checkDetachable(boundaryElements, true);

        long start = System.currentTimeMillis();

        // Remove tie-lines
        boundaryElements.stream()
                .filter(DanglingLine.class::isInstance)
                .map(DanglingLine.class::cast)
                .map(DanglingLine::getTieLine)
                .filter(Optional::isPresent)
                .forEach(t -> t.get().remove(true));

        // Create a new NetworkImpl and transfer the extensions to it
        NetworkImpl detachedNetwork = (NetworkImpl) Network.create(getId(), getSourceFormat());

        // TODO
        //transferExtensions(this, detachedNetwork);

        // Memorize the network identifiables/voltageAngleLimits before moving references (to use them latter)
        //Collection<Identifiable<?>> identifiables = getIdentifiables();
        //Iterable<VoltageAngleLimit> vals = getVoltageAngleLimits();

        // Move the substations and voltageLevels to the new network
        //ref.setRef(new RefObj<>(null));
        for(Substation substation : getSubstations()) {
            SubstationImpl impl = (SubstationImpl) substation;
            Resource< SubstationAttributes> attributes = impl.getResource();
            detachedNetwork.index.createSubstation(attributes);
            index.removeSubstation(attributes.getId());
        }
        for(VoltageLevel vl : getVoltageLevels()) {
            VoltageLevelImpl impl = (VoltageLevelImpl) vl;
            Resource<VoltageLevelAttributes> attributes = impl.getResource();
            detachedNetwork.index.createVoltageLevel(attributes);
            index.removeVoltageLevel(attributes.getId());
        }

        // Remove all the identifiers from the parent's index and add them to the detached network's index
        for (Identifiable<?> i : index.getIdentifiables()) {
            AbstractIdentifiableImpl<?, ?> impl = (AbstractIdentifiableImpl) i;
            if (impl.getResource().getParentNetwork() != null && impl.getResource().getParentNetwork().equals(this.getId())) {
                Resource<? extends IdentifiableAttributes> resource = impl.getResource();
                index.removeResource(impl.getResource());
                detachedNetwork.index.createResource(resource);
            }
        }
        /*for (VoltageAngleLimit val : vals) {
            previousRootNetwork.getVoltageAngleLimitsIndex().remove(val.getId());
            detachedNetwork.getVoltageAngleLimitsIndex().put(val.getId(), val);
        }*/

        // Remove the old subnetwork from the subnetworks list of the current parent network
        index.removeSubnetwork(this.getId());

        // We don't control that regulating terminals and phase/ratio regulation terminals are in the same subnetwork
        // as their network elements (generators, PSTs, ...). It is unlikely that those terminals and their elements
        // are in different subnetworks but nothing prevents it. For now, we ignore this case, but it may be necessary
        // to handle it later. If so, note that there are 2 possible cases:
        // - the element is in the subnetwork to detach and its regulating or phase/ratio regulation terminal is not
        // - the terminal is in the subnetwork, but not its element (this is trickier)

        //LOGGER.info("Detaching of {} done in {} ms", id, System.currentTimeMillis() - start);
        return detachedNetwork;
    }

    /**
     * {@inheritDoc}
     * <p>For now, only tie-lines can be split (HVDC lines may be supported later).</p>
     */
    @Override
    public boolean isDetachable() {
        return checkDetachable(getBoundaryElements(), false);
    }

    private boolean checkDetachable(Set<Identifiable<?>> boundaryElements, boolean throwsException) {
        if (getNetwork().getVariantManager().getVariantIds().size() != 1) {
            if (throwsException) {
                throw new PowsyblException("Detaching from multi-variants network is not supported");
            }
            return false;
        }
        if (boundaryElements.stream().anyMatch(Predicate.not(SubnetworkImpl::isSplittable))) {
            if (throwsException) {
                throw new PowsyblException("Un-splittable boundary elements prevent the subnetwork to be detached: "
                        + boundaryElements.stream().filter(Predicate.not(SubnetworkImpl::isSplittable)).map(Identifiable::getId).collect(Collectors.joining(", ")));
            }
            return false;
        }
        // TODO implement when voltage angle limit are there
        /*if (getNetwork().getVoltageAngleLimitsStream().anyMatch(this::isBoundary)) {
            if (throwsException) {
                throw new PowsyblException("VoltageAngleLimits prevent the subnetwork to be detached: "
                        + getNetwork().getVoltageAngleLimitsStream().filter(this::isBoundary).map(VoltageAngleLimit::getId).collect(Collectors.joining(", ")));
            }
            return false;
        }*/
        return true;
    }

    private static boolean isSplittable(Identifiable<?> identifiable) {
        return identifiable.getType() == IdentifiableType.DANGLING_LINE;
    }

    @Override
    public Set<Identifiable<?>> getBoundaryElements() {
        Stream<Line> lines = getNetwork().getLineStream().filter(i -> i.getParentNetwork() == getNetwork());
        Stream<DanglingLine> danglingLineStream = getDanglingLineStream();
        Stream<HvdcLine> hvdcLineStream = getNetwork().getHvdcLineStream().filter(i -> i.getParentNetwork() == getNetwork());

        return Stream.of(lines, danglingLineStream, hvdcLineStream)
                .flatMap(Function.identity())
                .map(o -> (Identifiable<?>) o)
                .filter(this::isBoundaryElement)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isBoundaryElement(Identifiable<?> identifiable) {
        return switch (identifiable.getType()) {
            case LINE, TIE_LINE -> isBoundary((Branch<?>) identifiable);
            case HVDC_LINE -> isBoundary((HvdcLine) identifiable);
            case DANGLING_LINE -> isBoundary((DanglingLine) identifiable);
            default -> false;
        };
    }

    private boolean isBoundary(Branch<?> branch) {
        return isBoundary(branch.getTerminal1(), branch.getTerminal2());
    }

    private boolean isBoundary(HvdcLine hvdcLine) {
        return isBoundary(hvdcLine.getConverterStation1().getTerminal(),
                hvdcLine.getConverterStation2().getTerminal());
    }

    private boolean isBoundary(DanglingLine danglingLine) {
        return danglingLine.getTieLine()
                .map(this::isBoundary)
                .orElse(true);
    }

    private boolean isBoundary(VoltageAngleLimit val) {
        return isBoundary(val.getTerminalFrom(), val.getTerminalTo());
    }

    private boolean isBoundary(Terminal terminal1, Terminal terminal2) {
        boolean containsVoltageLevel1 = contains(terminal1.getVoltageLevel());
        boolean containsVoltageLevel2 = contains(terminal2.getVoltageLevel());
        return containsVoltageLevel1 && !containsVoltageLevel2 ||
                !containsVoltageLevel1 && containsVoltageLevel2;
    }

    @Override
    public ContainerType getContainerType() {
        return null;
    }

    @Override
    public boolean hasProperty() {
        return false;
    }

    @Override
    public boolean hasProperty(String s) {
        return false;
    }

    @Override
    public String getProperty(String s) {
        return null;
    }

    @Override
    public String getProperty(String s, String s1) {
        return null;
    }

    @Override
    public String setProperty(String s, String s1) {
        return null;
    }

    @Override
    public boolean removeProperty(String s) {
        return false;
    }

    @Override
    public Set<String> getPropertyNames() {
        return null;
    }

    @Override
    public <E extends Extension<Network>> void addExtension(Class<? super E> aClass, E e) {

    }

    @Override
    public <E extends Extension<Network>> E getExtension(Class<? super E> aClass) {
        return null;
    }

    @Override
    public <E extends Extension<Network>> E getExtensionByName(String s) {
        return null;
    }

    @Override
    public <E extends Extension<Network>> boolean removeExtension(Class<E> aClass) {
        return false;
    }

    @Override
    public <E extends Extension<Network>> Collection<E> getExtensions() {
        return null;
    }

    @Override
    public String getMessageHeader() {
        return null;
    }

    @Override
    public void addListener(NetworkListener listener) {
        throw new PowsyblException("Listeners are not managed at subnetwork level." +
                " Add this listener to the parent network '" + getNetwork().getId() + "'");
    }

    @Override
    public void removeListener(NetworkListener listener) {
        throw new PowsyblException("Listeners are not managed at subnetwork level." +
                " Remove this listener to the parent network '" + getNetwork().getId() + "'");
    }

    @Override
    public VariantManager getVariantManager() {
        return getParentNetwork().getVariantManager();
    }

    @Override
    public Set<Country> getCountries() {
        return getCountryStream().collect(Collectors.toSet());
    }

    @Override
    public int getCountryCount() {
        return (int) getCountryStream().count();
    }

    private Stream<Country> getCountryStream() {
        return getSubstationStream()
                .map(s -> s.getCountry().orElse(null))
                .filter(Objects::nonNull);
    }

    @Override
    public <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        return index.getIdentifiables().stream().filter(clazz::isInstance).filter(this::contains).map(clazz::cast);
    }

    @Override
    public <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        return getConnectableStream(clazz).collect(Collectors.toList());
    }

    @Override
    public <C extends Connectable> int getConnectableCount(Class<C> clazz) {
        return Ints.checkedCast(getConnectableStream(clazz).count());
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return getConnectables(Connectable.class);
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return getConnectableStream(Connectable.class);
    }

    @Override
    public int getConnectableCount() {
        return Ints.checkedCast(getConnectableStream().count());
    }
}
