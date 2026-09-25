package com.powsybl.network.store.iidm.impl.tck;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.Set;

// FIXME: Keep this workaround limited to TCK methods covering unsupported network-store features.
public class ExcludeTestsExtension implements InvocationInterceptor {
    private static final Set<String> EXCLUDED_TESTS = Set.of(
            "testSetMinimumAcceptableValidationLevelOnInvalidatedNetwork",
            "testSameLine",
            "testNullLine",
            "testDuplicateCoupling",
            "testFindSymmetric",
            "testRemoveByLines",
            "testRemoveByMutualCoupling",
            "testSetters",
            "testInvalidLineSegment",
            "testInvalidRAndX",
            "testListener",
            "testConnectDisconnectWithFictitiousBreaker"
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
