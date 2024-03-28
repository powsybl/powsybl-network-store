package com.powsybl.network.store.model;

import com.powsybl.commons.PowsyblException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class LoaderTest {
    @Test
    public void testLoaderNotFound() {
        PowsyblException exception = assertThrows(PowsyblException.class, () -> ExtensionLoaders.findLoader("unknown"));
        assertEquals(exception.getMessage(), "ExtensionLoader not found");
    }

    @Test
    public void testDuplicatedLoader() {
        PowsyblException exception = assertThrows(PowsyblException.class, () -> ExtensionLoaders.findLoader("loader"));
        assertEquals(exception.getMessage(), "Multiple ExtensionLoaders configuration providers found");
    }
}
