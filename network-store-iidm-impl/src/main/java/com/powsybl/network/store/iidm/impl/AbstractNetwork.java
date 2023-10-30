package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import org.joda.time.DateTime;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

public abstract class AbstractNetwork<D extends IdentifiableAttributes> extends AbstractIdentifiableImpl<Network, D> implements Network, Validable {

    private final BusBreakerView busBreakerView = new BusBreakerViewImpl();

    private final BusView busView = new BusViewImpl();

    protected AbstractNetwork(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
    }

    @Override
    public DateTime getCaseDate() {
        return index.getNetwork().getCaseDate();
    }

    @Override
    public Network setCaseDate(DateTime dateTime) {
        return null;
    }

    @Override
    public int getForecastDistance() {
        return 0;
    }

    @Override
    public Network setForecastDistance(int i) {
        return null;
    }

    @Override
    public String getSourceFormat() {
        return null;
    }

    @Override
    public Iterable<Substation> getSubstations() {
        return getSubstationStream().toList();
    }

    @Override
    public int getSubstationCount() {
        return (int) getSubstationStream().count();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return getSubstations(Optional.ofNullable(country).map(Country::getName).orElse(null), tsoId, geographicalTags);
    }

    @Override
    public Iterable<Substation> getSubstations(String country, String tsoId, String... geographicalTags) {
        return getSubstationStream().filter(substation -> {
            if (country != null && !country.equals(substation.getCountry().map(Country::getName).orElse(""))) {
                return false;
            }
            if (tsoId != null && !tsoId.equals(substation.getTso())) {
                return false;
            }
            for (String tag : geographicalTags) {
                if (!substation.getGeographicalTags().contains(tag)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    class BusBreakerViewImpl implements BusBreakerView {

        @Override
        public Iterable<Bus> getBuses() {
            return getBusStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getBusStream());
        }

        @Override
        public int getBusCount() {
            return getVoltageLevelStream().mapToInt(vl -> vl.getBusBreakerView().getBusCount()).sum();
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return getSwitchStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getSwitchStream());
        }

        @Override
        public int getSwitchCount() {
            return (int) getSwitchStream().count();
        }

        @Override
        public Bus getBus(String id) {
            Optional<Bus> busInBusBreakerTopo = index.getConfiguredBus(id).map(Function.identity()); // start search in BB topo
            return busInBusBreakerTopo.or(() -> getVoltageLevelStream().map(vl -> vl.getBusBreakerView().getBus(id)) // fallback to search in NB topo
                            .filter(Objects::nonNull)
                            .findFirst())
                    .orElse(null);
        }
    }

    class BusViewImpl implements Network.BusView {

        @Override
        public Iterable<Bus> getBuses() {
            return getBusStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusView().getBusStream());
        }

        @Override
        public Bus getBus(String id) {
            return getBusStream().filter(b -> b.getId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public Collection<Component> getConnectedComponents() {
            return getBusStream().map(Bus::getConnectedComponent)
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingInt(Component::getNum))), ArrayList::new));
        }

        @Override
        public Collection<Component> getSynchronousComponents() {
            return getBusStream().map(Bus::getSynchronousComponent)
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingInt(Component::getNum))), ArrayList::new));
        }
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusView getBusView() {
        return busView;
    }

}
