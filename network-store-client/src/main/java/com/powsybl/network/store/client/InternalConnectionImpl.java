package com.powsybl.network.store.client;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.InternalConnectionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;
import java.util.Set;

public class InternalConnectionImpl implements VoltageLevel.NodeBreakerView.InternalConnection, Identifiable<InternalConnectionImpl> {

    private final Resource<InternalConnectionAttributes> resource;

    private final NetworkObjectIndex index;

    public InternalConnectionImpl(NetworkObjectIndex index, Resource<InternalConnectionAttributes> resource) {
        this.resource = resource;
        this.index = index;
    }

    static InternalConnectionImpl create(NetworkObjectIndex index, Resource<InternalConnectionAttributes> resource) {
        return new InternalConnectionImpl(index, resource);
    }

    @Override
    public int getNode1() {
        return resource.getAttributes().getNode1();
    }

    @Override
    public int getNode2() {
        return resource.getAttributes().getNode2();
    }

    @Override
    public Network getNetwork() {
        return index.getNetwork();
    }

    @Override
    public String getId() {
        return resource.getId();
    }

    @Override
    public String getName() {
        return resource.getAttributes().getName();
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
    public Set<String> getPropertyNames() {
        return null;
    }

    @Override
    public <E extends Extension<InternalConnectionImpl>> void addExtension(Class<? super E> aClass, E e) {

    }

    @Override
    public <E extends Extension<InternalConnectionImpl>> E getExtension(Class<? super E> aClass) {
        return null;
    }

    @Override
    public <E extends Extension<InternalConnectionImpl>> E getExtensionByName(String s) {
        return null;
    }

    @Override
    public <E extends Extension<InternalConnectionImpl>> boolean removeExtension(Class<E> aClass) {
        return false;
    }

    @Override
    public <E extends Extension<InternalConnectionImpl>> Collection<E> getExtensions() {
        return null;
    }
}
