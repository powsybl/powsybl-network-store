package com.powsybl.network.store.iidm.impl.tck;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.Set;

// FIXME: Workarounds for package-private TCK tests that cannot be overridden by network-store tests.
// Should be deleted with powsybl-core 7.5.0. See https://github.com/powsybl/powsybl-core/pull/4258
public class ExcludeTestsExtension implements InvocationInterceptor {
    private static final Set<String> EXCLUDED_TESTS = Set.of(
            // FIXME: to be removed when asymetric connect/disconnect is fixed
            // Supposed to be fixed in https://github.com/powsybl/powsybl-core/pull/3885
            // But still not working
            "testConnectDisconnectWithFictitiousBreaker",
            // Replaced by value-based multi-variant tests
            "testNewVoltageRegulationInMultiVariants",
            // Replaced by value-based notification tests
            "testNotifyUpdate"
    );

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        String methodName = invocationContext.getExecutable().getName();
        if (EXCLUDED_TESTS.contains(methodName)) {
            throw new TestAbortedException("Test not applicable for network-store implementation");
        }
        invocation.proceed();
    }
}
